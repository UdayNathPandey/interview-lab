# Transaction Deep-Dive — Interview Revision

## 1. Core Mental Model

`@Transactional` does **not** mean a transaction object is permanently created around the method.

At runtime, Spring roughly follows:

```text
@Transactional
     ↓
Spring Proxy
     ↓
TransactionInterceptor
     ↓
TransactionManager
     ↓
Check current transaction context
     ↓
Propagation decision
     ↓
Create / Join / Suspend / Reject
     ↓
Method execution
     ↓
Commit / Rollback
     ↓
Cleanup
```

---

# 2. Internal Composition — Don't Mix These Concepts

The following are related, but they are **not the same thing**:

```text
Thread
 └── thread-specific transaction context/resources
        ├── DB resource / connection holder
        ├── transaction synchronization state
        └── transaction-related state

Transaction
 └── logical/physical unit of database work
        ├── commit boundary
        ├── rollback boundary
        └── isolation/transaction semantics

Persistence Context
 └── Hibernate/JPA managed entity state
        ├── managed entities
        ├── identity map / first-level cache behavior
        └── dirty checking

EntityManager
 └── JPA API used to interact with the persistence context
```

### Important

```text
ThreadLocal ≠ Transaction
Transaction ≠ Persistence Context
Persistence Context ≠ EntityManager
EntityManager ≠ DB Connection
```

They cooperate, but represent different concepts.

---

# 3. Thread

A **Thread** is the execution path on which Java code runs.

Example:

```text
HTTP Request
    ↓
Thread-1
    ↓
Controller
    ↓
Service
    ↓
Repository
```

For a normal synchronous Spring transaction, the same thread typically executes the transactional method and accesses the transaction-associated resources.

---

# 4. ThreadLocal

`ThreadLocal<T>` is a Java mechanism for **thread-specific storage**.

Mental model:

```text
Thread-1
┌─────────────────────┐
│ My thread-local data│
│ T1 / resources      │
└─────────────────────┘

Thread-2
┌─────────────────────┐
│ My thread-local data│
│ T5 / resources      │
└─────────────────────┘
```

The same ThreadLocal mechanism can have different values associated with different threads.

### In Spring transactions

Spring's transaction infrastructure uses thread-bound resource/context mechanisms (notably `TransactionSynchronizationManager`) so that code executing on the current thread can discover transaction-associated resources and synchronization state.

### Remember

> ThreadLocal is a **storage mechanism**; it is not itself a transaction.

---

# 5. Transaction

A transaction is a unit of database work with a boundary:

```text
BEGIN
  ↓
SQL operations
  ↓
COMMIT
```

or:

```text
BEGIN
  ↓
SQL operations
  ↓
ROLLBACK
```

Conceptually:

```text
Transaction T1
 ├── INSERT
 ├── UPDATE
 ├── SELECT
 └── COMMIT / ROLLBACK
```

The transaction is primarily a **database consistency/atomicity concept**.

---

# 6. Persistence Context

A JPA Persistence Context is the set of entity instances that the JPA provider currently manages.

Example:

```java
Order order = entityManager.find(Order.class, 1L);
```

Conceptually:

```text
Persistence Context
┌────────────────────────┐
│ Order#1 → managed      │
│ Order#2 → managed      │
└────────────────────────┘
```

Hibernate tracks managed entities.

If:

```java
order.setAmount(new BigDecimal("2000"));
```

changes a managed entity, Hibernate can detect the change through **dirty checking** and synchronize it with the database during flush.

### Persistence Context gives us

- managed entity lifecycle
- first-level cache / identity guarantee within the context
- dirty checking
- entity state tracking

---

# 7. EntityManager

`EntityManager` is the **JPA API/interface through which application code interacts with the persistence context**.

Example:

```java
entityManager.find(...)
entityManager.persist(...)
entityManager.remove(...)
entityManager.flush(...)
```

Think:

```text
Application
    ↓
EntityManager
    ↓
Persistence Context
    ↓
Hibernate
    ↓
Database
```

### Important distinction

```text
EntityManager
    = API / gateway

Persistence Context
    = managed entity state/context
```

An EntityManager is therefore not synonymous with the Persistence Context.

---

# 8. DB Connection

A DB connection is the communication/resource channel used to talk to the database.

Simplified:

```text
Transaction T1
      ↓
DB resource / Connection
      ↓
Database
```

Don't memorize:

```text
Transaction = Connection
```

They are different concepts.

A transaction defines the transactional unit/semantics; a connection is a database resource used to execute database work.

---

# 9. How They Work Together

A useful simplified picture:

```text
Thread-1
   │
   ▼
Spring transaction context
   │
   ├── Transaction T1
   │
   ├── DB resource / connection participation
   │
   └── synchronization state
           │
           ▼
      Persistence Context
           │
           ▼
       EntityManager
           │
           ▼
        Hibernate
           │
           ▼
        Database
```

This is a **mental model**, not a claim that all components are literally one object or stored in one place.

---

# 10. @Transactional Internal Flow

Given:

```java
@Transactional
public void placeOrder() {
    saveOrder();
}
```

External call:

```text
Controller
   ↓
Spring Proxy
```

The proxy/interceptor evaluates the transactional metadata.

```text
Proxy
  ↓
TransactionInterceptor
  ↓
TransactionManager
  ↓
Is transaction already present?
```

Then propagation determines what happens.

---

# 11. Propagation

Propagation answers:

> "When this method is called, what should happen with an existing transaction?"

Main modes:

| Propagation | Existing transaction |
|---|---|
| REQUIRED | Join it |
| REQUIRES_NEW | Suspend it and create new |
| SUPPORTS | Join if present; otherwise non-transactional |
| MANDATORY | Must already exist; otherwise exception |
| NOT_SUPPORTED | Suspend it and execute without transaction |
| NEVER | Must not exist; otherwise exception |

---

# 12. REQUIRED — Internal Flow

`@Transactional` defaults to:

```text
Propagation.REQUIRED
```

If no transaction exists:

```text
methodA
 ↓
Proxy
 ↓
No current transaction
 ↓
Create T1
 ↓
Execute A
 ↓
Commit/Rollback T1
```

If T1 already exists:

```text
T1
 │
 ├── methodA
 │
 └── methodB(REQUIRED)
          ↓
       JOIN T1
```

### Result

A and B participate in the **same physical transaction**.

```text
T1
├── A
└── B
```

---

# 13. REQUIRES_NEW — Internal Flow

Suppose:

```java
@Transactional
void A() {
    B();
}

@Transactional(propagation = REQUIRES_NEW)
void B() {}
```

Flow:

```text
Thread-1
   ↓
T1 active
   ↓
A
   ↓
B
   ↓
Suspend T1
   ↓
Create T2
   ↓
B executes in T2
   ↓
T2 commit/rollback
   ↓
Resume T1
   ↓
A continues
   ↓
T1 commit/rollback
```

Mental model:

```text
T1 ACTIVE
   │
   ├── A
   │
   └── SUSPEND
          ↓
        T2 ACTIVE
          │
          └── B
          ↓
        T2 complete
          ↓
        RESUME T1
```

### Core interview phrase

> `REQUIRES_NEW = SUSPEND → NEW → EXECUTE → COMPLETE → RESUME`

---

# 14. Multiple REQUIRES_NEW Calls

One thread does not need one transaction only.

Example:

```text
Thread-1

T1 active
  ↓
T1 suspended
  ↓
T2 active
  ↓
T2 complete
  ↓
T1 resumed
  ↓
T1 suspended
  ↓
T3 active
  ↓
T3 complete
  ↓
T1 resumed
```

The key is **timeline**, not simultaneous execution of all transactions.

---

# 15. Thread + ThreadLocal + REQUIRES_NEW

Visualize the current transactional context:

```text
Thread-1

CURRENT
┌──────────────┐
│ T1 + resources│
└──────────────┘
```

When REQUIRES_NEW starts:

```text
Thread-1

SUSPENDED
┌──────────────┐
│ T1 + resources│
└──────────────┘

CURRENT
┌──────────────┐
│ T2 + resources│
└──────────────┘
```

After T2 completes:

```text
Thread-1

CURRENT
┌──────────────┐
│ T1 + resources│
└──────────────┘
```

For revision, this is best understood as a **current-context + suspended-context** model. Do not assume Spring literally stores `[T1,T2,T3]` as a simple Java stack inside one ThreadLocal.

---

# 16. Logical vs Physical Transaction

This is interview-important.

Nested transactional method scopes can be logical scopes:

```text
A() → logical transaction scope
B() → logical transaction scope
```

With `REQUIRED`:

```text
A ─┐
   ├── Physical transaction T1
B ─┘
```

With `REQUIRES_NEW`:

```text
A → Physical T1

B → Physical T2
```

So propagation controls how logical transactional scopes relate to physical transactions.

---

# 17. Rollback Behavior

### Runtime exception

By default, Spring rolls back for unchecked exceptions such as `RuntimeException`.

```text
T1
 ↓
RuntimeException
 ↓
ROLLBACK
```

### Checked exception

By default, checked exceptions do not trigger rollback in the same way.

```text
T1
 ↓
Checked Exception
 ↓
default rollback? → NO
```

Can explicitly configure:

```java
@Transactional(rollbackFor = Exception.class)
```

Then:

```text
Checked Exception
      ↓
rollbackFor
      ↓
ROLLBACK
```

---

# 18. rollbackFor / noRollbackFor

```java
@Transactional(
    rollbackFor = Exception.class,
    noRollbackFor = SomeException.class
)
```

Conceptually:

```text
Exception occurs
      ↓
Spring evaluates rollback rules
      ↓
rollbackFor / noRollbackFor
      ↓
rollback OR commit
```

The transaction boundary and exception policy are related but separate concepts.

---

# 19. REQUIRED + Inner Failure

```text
T1
├── A
└── B
     ↓
  RuntimeException
     ↓
T1 may become rollback-only
```

Even if A catches the exception:

```text
A
 ├── call B
 │     ↓
 │   failure
 │
 └── catch exception
```

the shared transaction may already be marked rollback-only.

At the end:

```text
commit requested
       ↓
rollback-only
       ↓
ROLLBACK
```

This can result in `UnexpectedRollbackException`.

---

# 20. REQUIRES_NEW + Inner Failure

```text
T1
│
├── A
│
└── suspend
       ↓
      T2
      │
      └── B fails
           ↓
        T2 ROLLBACK
       ↓
resume T1
       ↓
A continues
```

T2 is independent.

Therefore:

```text
T2 rollback ≠ automatic T1 rollback
```

Likewise:

```text
T1 rollback ≠ automatic T2 rollback
```

---

# 21. Self-Invocation Trap

Spring's transaction behavior normally relies on proxy interception.

This works:

```text
Controller
   ↓
OrderService Proxy
   ↓
@Transactional method
```

But:

```java
this.methodB();
```

inside the same object is a direct Java call.

Conceptually:

```text
methodA()
   ↓
this.methodB()
   ↓
bypass proxy
   ↓
TransactionInterceptor may not run for B
```

Therefore a `REQUIRES_NEW` annotation on B may not take effect through self-invocation.

Typical solution:

```text
OrderService
    ↓
PaymentService
    ↓
@Transactional(REQUIRES_NEW)
```

and invoke through the Spring-managed bean.

---

# 22. Quick Comparison

| Concept | What it represents |
|---|---|
| Thread | Execution path |
| ThreadLocal | Thread-specific storage mechanism |
| Transaction | DB unit of work / boundary |
| DB Connection | Resource/channel used to communicate with DB |
| Persistence Context | JPA/Hibernate managed entity state/context |
| EntityManager | JPA API used to interact with persistence context |
| TransactionManager | Spring abstraction that coordinates transaction lifecycle |
| TransactionInterceptor | Intercepts transactional method invocation |
| Proxy | Entry point through which Spring can apply transaction advice |

---

# 23. Similarities / Relationship

They cooperate around one unit of work:

```text
Thread
  ↓
Spring transaction context
  ↓
Transaction
  ↓
DB resource
  ↓
Persistence Context
  ↓
EntityManager / Hibernate
  ↓
Database
```

But they have different responsibilities.

### Easy analogy

```text
Thread
= delivery boy

ThreadLocal
= delivery boy's personal notebook

Transaction
= one order/bill that must complete or cancel together

Connection
= road to the database

Persistence Context
= notebook containing tracked entities

EntityManager
= counter/API through which you manage that notebook

TransactionManager
= supervisor managing transaction boundaries
```

---

# 24. Interview 30-Second Answer

> "`@Transactional` is implemented through Spring's proxy-based transaction infrastructure. A transactional method invocation reaches the proxy and TransactionInterceptor, which uses the TransactionManager to inspect the current transaction context and apply the configured propagation behavior. Spring associates transaction-related resources and synchronization state with the executing thread. With REQUIRED, an existing transaction is joined; with REQUIRES_NEW, the existing transactional context is suspended and an independent transaction is created. JPA's Persistence Context tracks managed entities and dirty changes, while EntityManager is the JPA API used to interact with that context. Transaction, ThreadLocal, Persistence Context, EntityManager, and DB Connection are different concepts that cooperate during a transactional unit of work."

---

# 25. Final Mental Model

```text
                 @Transactional
                       ↓
                 Spring Proxy
                       ↓
            TransactionInterceptor
                       ↓
              TransactionManager
                       ↓
             Current Thread Context
                       ↓
            ┌──────────┴──────────┐
            │                     │
       Existing T?              No T
            │                     │
            ↓                     ↓
     Propagation rule          Create T1
            │
     ┌──────┼──────────┐
     ↓      ↓          ↓
 REQUIRED  REQUIRES_NEW  ...
     ↓      ↓
   JOIN   SUSPEND
     │      ↓
     │    NEW T2
     │      ↓
     └──────┴────→ Method execution
                       ↓
             Persistence Context
                       ↓
                  Hibernate
                       ↓
                  DB Resource
                       ↓
                Commit / Rollback
                       ↓
                    Cleanup
```

## Revision Golden Rules

1. **Thread ≠ Transaction**
2. **ThreadLocal ≠ Transaction**
3. **Transaction ≠ Connection**
4. **Transaction ≠ Persistence Context**
5. **EntityManager ≠ Persistence Context**
6. `REQUIRED` → **JOIN**
7. `REQUIRES_NEW` → **SUSPEND → NEW → COMPLETE → RESUME**
8. Propagation controls **transaction participation**, not entity mapping.
9. Persistence Context handles **managed entity state + dirty checking**.
10. Spring's transaction infrastructure uses **thread-bound context/resources** in normal synchronous execution.
11. Self-invocation can bypass Spring's proxy and therefore transactional interception.
12. Always distinguish **logical transactional scopes** from **physical database transactions**.
