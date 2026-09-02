# LEVEL 8.2.B — Sorting + Pageable + Open Session in View

## Goal

Demonstrate dynamic sorting through Spring Data JPA `Pageable`, while consolidating the JPA/Hibernate lessons discovered during pagination.

```text
HTTP
 ↓
Spring MVC
 ↓
Pageable
 ├── page
 ├── size
 └── sort
 ↓
Controller
 ↓
Service
 ↓
Spring Data JPA
 ↓
Hibernate
 ↓
SQL ORDER BY + LIMIT/OFFSET
 ↓
Page<Order>
 ↓
DTO mapping
 ↓
Page<OrderResponse>
 ↓
Jackson
 ↓
JSON
```

## 1. spring.jpa.open-in-view

Configuration:

```properties
spring.jpa.open-in-view=false
```

Open Session in View / Open EntityManager in View controls whether the JPA `EntityManager` / persistence context can remain associated with a web request into later web-layer processing.

It matters mainly when LAZY relationships are accessed after repository/service work.

### Persistence Context

```text
Database
 ↓
Hibernate
 ↓
EntityManager
 ↓
Persistence Context
 ↓
Managed Entity
```

The persistence context participates in entity management, dirty checking, identity management and lazy-association initialization.

### OIV=false

```text
HTTP Request
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Hibernate / EntityManager
 ↓
Database
 ↓
Transaction/persistence work finishes
 ↓
Persistence context unavailable
 ↓
Controller response
 ↓
Jackson serialization
```

If Jackson then accesses a LAZY relationship:

```text
LAZY proxy
 ↓
needs persistence context
 ↓
not available
 ↓
LazyInitializationException
```

### OIV=true

```text
HTTP Request
 ↓
EntityManager / Persistence Context available
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
Hibernate
 ↓
Controller response
 ↓
Jackson serialization
 ↓
LAZY relationship accessed
 ↓
Hibernate can potentially initialize it
 ↓
Additional SQL may execute
 ↓
JSON
 ↓
Request ends
 ↓
EntityManager closes
```

`open-in-view=true` does NOT mean the entire HTTP request is one database transaction. It concerns persistence-context/EntityManager availability.

### Why OIV=true can hide problems

With 20 Orders and LAZY Customers:

```text
1 query → Orders
+
N queries → Customers
```

Potentially producing an N+1 pattern during response serialization.

Therefore OIV=true can make an API appear to work while allowing unexpected database access in the web layer.

The cleaner approach is usually to fetch required data intentionally inside the appropriate transactional boundary and map to DTOs.

---

## 2. Entity Relationships Used

### Order

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;
```

### Customer

```java
@OneToMany(
    mappedBy = "customer",
    orphanRemoval = true,
    fetch = FetchType.LAZY
)
private List<Order> orders;
```

Mental model:

```text
Order ──LAZY──→ Customer
Customer ──LAZY──→ Orders
```

LAZY does not mean “never load”. It means the related data can be loaded when needed while the appropriate persistence context is available.

---

## 3. Challenge — Incorrect Pagination Mapping

Incorrect:

```java
@GetMapping("/orders?{page}&{size}")
```

Correct:

```java
@GetMapping("/orders")
```

Request:

```http
GET /api/orders?page=0&size=3
```

Spring MVC binds the query parameters into `Pageable`.

```text
Pageable
 ├── page = 0
 └── size = 3
```

The path and query string are separate:

```text
/orders
   ↑
mapped path

?page=0&size=3
 ↑
query string
```

---

## 4. Challenge — Pagination Was Working

Hibernate generated:

```sql
SELECT ...
FROM orders
LIMIT ?, ?
```

This proved:

```text
Pageable → Spring Data JPA → Hibernate → LIMIT/OFFSET
```

was working.

For:

```text
page = 0
size = 3
```

the conceptual SQL is:

```sql
LIMIT 3 OFFSET 0
```

For:

```text
page = 1
size = 3
```

the conceptual SQL is:

```sql
LIMIT 3 OFFSET 3
```

`Page<T>` also commonly triggers:

```sql
SELECT COUNT(id)
FROM orders;
```

because total-elements/total-pages metadata requires the total matching row count.

---

## 5. Challenge — LazyInitializationException

When the endpoint returned:

```java
Page<Order>
```

and `Order.customer` was LAZY:

```text
Order
 ↓
Customer = Hibernate proxy
 ↓
Persistence context becomes unavailable
 ↓
Jackson serializes Order
 ↓
Jackson accesses Customer
 ↓
Hibernate tries to initialize proxy
 ↓
No Session
 ↓
LazyInitializationException
```

Observed error:

```text
Could not initialize proxy [Customer#1] - no session
```

Root cause:

```text
LAZY proxy
+
persistence context unavailable
+
relationship accessed
=
LazyInitializationException
```

---

## 6. Challenge — EAGER/EAGER Recursive JSON

To understand the behavior, both sides were made EAGER.

`Order.customer` was EAGER because `@ManyToOne` defaults to EAGER when fetch is not specified.

`Customer.orders` was explicitly made EAGER.

The object graph became:

```text
Order
 ↓
Customer
 ↓
orders
 ↓
Order
 ↓
Customer
 ↓
orders
 ↓
...
```

Jackson recursively serialized the bidirectional graph.

So:

> The JPA relationship itself is valid. The problem is directly exposing a cyclic entity graph to JSON serialization.

---

## 7. DTO Solution

Instead of:

```text
Order Entity
 ↓
Jackson
 ↓
Customer
 ↓
orders
 ↓
Order
```

use:

```text
Order
 ↓
Service
 ↓
OrderResponse
 ↓
Jackson
 ↓
JSON
```

DTO:

```java
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderResponse {
    private Long id;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal discount;
}
```

The DTO contains no `Customer` object and no `List<Order>`, so the API response cannot recursively traverse the JPA relationship.

---

## 8. Final Service

```java
@Override
@Transactional(readOnly = true)
public Page<OrderResponse> getAllOrders(Pageable pageable) {

    return orderRepository.findAll(pageable)
            .map(order ->
                    OrderResponse.builder()
                            .id(order.getId())
                            .customerName(order.getCustomerName())
                            .customerEmail(order.getCustomerEmail())
                            .amount(order.getAmount())
                            .status(order.getStatus())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .discount(order.getDiscount())
                            .build()
            );
}
```

Important Spring Data feature:

```java
Page<Order>.map(...)
```

converts:

```text
Page<Order>
 ↓
Page<OrderResponse>
```

while retaining pagination metadata.

---

## 9. Final Controller

```java
@GetMapping("/orders")
public ResponseEntity<Page<OrderResponse>> getAllOrders(
        Pageable pageable
) {
    return ResponseEntity.ok(
            orderService.getAllOrders(pageable)
    );
}
```

The same endpoint can now support:

```text
GET /api/orders

GET /api/orders?page=0&size=3

GET /api/orders?sort=amount,desc

GET /api/orders?page=0&size=3&sort=amount,desc
```

---

# 10. Sorting

Spring Data supports dynamic sorting through `Pageable`.

Request:

```http
GET /api/orders?sort=amount,desc
```

Conceptually Spring creates:

```text
Pageable
 └── Sort
      ├── property = amount
      └── direction = DESC
```

Hibernate generates conceptually:

```sql
SELECT ...
FROM orders
ORDER BY amount DESC
```

---

## 11. Sorting Examples

### Amount descending

```http
/api/orders?sort=amount,desc
```

```sql
ORDER BY amount DESC
```

### Amount ascending

```http
/api/orders?sort=amount,asc
```

```sql
ORDER BY amount ASC
```

### Created date descending

```http
/api/orders?sort=createdAt,desc
```

Hibernate maps the entity property:

```text
createdAt
```

to the database column:

```text
created_at
```

and generates:

```sql
ORDER BY created_at DESC
```

---

## 12. Multiple Sorting Fields

Request:

```http
/api/orders?sort=status,asc&sort=amount,desc
```

Conceptually:

```sql
ORDER BY status ASC, amount DESC
```

Meaning:

```text
First sort by status.

For records having the same status,
sort by amount descending.
```

---

# 13. Pagination + Sorting

Request:

```http
GET /api/orders?page=0&size=3&sort=amount,desc
```

Spring creates:

```text
Pageable
 ├── page = 0
 ├── size = 3
 └── sort = amount DESC
```

Hibernate conceptually generates:

```sql
SELECT ...
FROM orders
ORDER BY amount DESC
LIMIT 3 OFFSET 0;
```

And for `Page<T>`:

```sql
SELECT COUNT(id)
FROM orders;
```

may also execute for page metadata.

Complete flow:

```text
HTTP
 ↓
?page=0
&size=3
&sort=amount,desc
 ↓
Pageable
 ├── page
 ├── size
 └── Sort
      └── amount DESC
 ↓
Repository
 ↓
Hibernate
 ↓
ORDER BY amount DESC
 ↓
LIMIT 3 OFFSET 0
 ↓
Page<Order>
 ↓
Page.map(...)
 ↓
Page<OrderResponse>
 ↓
Jackson
 ↓
JSON
```

---

# 14. Why Sorting Should Happen in the Database

Avoid for normal database-backed APIs:

```text
Database
 ↓
Fetch ALL records
 ↓
Java List
 ↓
Java sorting
```

Prefer:

```text
Database
 ↓
ORDER BY
 ↓
Required result
 ↓
Application
```

This becomes especially important when combining:

```text
WHERE
ORDER BY
LIMIT/OFFSET
```

The database can perform these operations before returning the result set.

---

# 15. Pagination vs Sorting

Pagination answers:

> Which chunk of records do I want?

```text
page
size
```

Sorting answers:

> In which order should records be returned?

```text
sort
```

Therefore:

```text
Pageable
 ├── page
 ├── size
 └── sort
```

is the key mental model.

---

# 16. Why Not Blindly Use EAGER?

Changing:

```java
fetch = FetchType.LAZY
```

to:

```java
fetch = FetchType.EAGER
```

just to remove a lazy-loading exception is not a general solution.

EAGER relationships can cause data to be loaded even when the API does not need it.

Prefer:

```text
LAZY
+
intentional fetching
+
DTO
```

Depending on the use case, intentional fetching can use JOIN FETCH, EntityGraph, DTO projection, or other explicit query strategies.

---

# 17. PageImpl Warning

Spring Data also produced a warning that serializing `PageImpl` directly does not guarantee a stable JSON structure.

This is separate from the lazy-loading failure.

Remember:

```text
PageImpl serialization warning
        ↓
⚠️ Warning

LazyInitializationException
        ↓
❌ Actual lazy-loading problem
```

---

# 18. Three Important Boundaries

### Pagination boundary

```text
Pageable
 ↓
LIMIT/OFFSET + COUNT
```

### Persistence boundary

```text
Transaction
 ↓
EntityManager / Persistence Context
 ↓
Hibernate
```

### Serialization boundary

```text
Java object
 ↓
Jackson
 ↓
JSON
```

The lazy-loading problem happened because a LAZY association was accessed after crossing the persistence boundary.

---

# 19. Complete Architecture

```text
                     HTTP
                      │
                      ↓
                Spring MVC
                      │
                      ↓
                   Pageable
              ┌───────┼────────┐
              ↓       ↓        ↓
            page     size     sort
                              │
                              ↓
                         amount DESC
                      │
                      ↓
                  Controller
                      │
                      ↓
                    Service
                      │
                      ↓
                  Repository
                      │
                      ↓
                   Hibernate
                      │
                      ↓
             ORDER BY amount DESC
                      │
                      ↓
                LIMIT/OFFSET
                      │
                      ↓
                  Page<Order>
                      │
                      ↓
                 Page.map()
                      │
                      ↓
              Page<OrderResponse>
                      │
                      ↓
                   Jackson
                      │
                      ↓
                     JSON
```

---

# 20. Interview Mental Model

### What does `spring.jpa.open-in-view` do?

It controls whether the JPA EntityManager/persistence context can remain associated with the web request into later web-layer processing.

### Does OIV=true mean the entire HTTP request is one transaction?

No. It concerns persistence-context/EntityManager availability, not automatically one giant database transaction.

### Why does LazyInitializationException occur?

A LAZY association is represented by a Hibernate proxy and needs an available persistence context when initialized. If accessed after that context is unavailable, Hibernate throws `LazyInitializationException`.

### Why did it happen during JSON serialization?

Jackson accessed the entity's LAZY relationship while converting the entity to JSON.

### What happens with OIV=true?

The persistence context can remain available during response processing, so the LAZY relationship may initialize successfully, potentially causing additional SQL.

### Why can OIV=true hide N+1?

Because lazy relationships can still trigger database queries while the response is being serialized.

### Why use DTOs?

DTOs control the API response shape and prevent Jackson from blindly traversing the JPA entity graph.

### Why did EAGER/EAGER cause recursion?

Because:

```text
Order → Customer → Orders → Order → Customer → ...
```

created a cyclic object graph that Jackson recursively serialized.

### What does Pageable contain?

Conceptually:

```text
page
size
sort
```

### Why does Page commonly trigger a count query?

Because total-elements and total-pages metadata require the total number of matching rows.

---

# 21. Debugging Checklist

When seeing `LazyInitializationException`:

1. Find the LAZY relationship.
2. Find who is accessing it: Service, Controller, Mapper, or Jackson.
3. Check `spring.jpa.open-in-view`.
4. Ask whether the API actually needs that relationship.
5. If needed, fetch it intentionally inside the appropriate transactional boundary.
6. Prefer DTOs for API responses.

---

# 22. Key Takeaways

```text
Pagination
 ↓
Pageable
 ↓
LIMIT/OFFSET
```

```text
Sorting
 ↓
Pageable
 ↓
Sort
 ↓
ORDER BY
```

```text
Page<T>
 ↓
COUNT query
 ↓
totalElements / totalPages
```

```text
LAZY
 ↓
Proxy
 ↓
Needs Persistence Context when initialized
```

```text
open-in-view=false
 ↓
LAZY loading should be handled intentionally
```

```text
open-in-view=true
 ↓
LAZY loading may happen during response processing
 ↓
but unexpected SQL/N+1 can be hidden
```

```text
Entity → DTO → JSON
```

prevents the API from blindly exposing/traversing the JPA entity graph.

---

# 23. Experiment Status

```text
Basic Pagination                  ✅
Pageable                          ✅
LIMIT/OFFSET                      ✅
COUNT query                       ✅

LAZY loading behavior             ✅
LazyInitializationException       ✅
open-in-view=false                ✅
open-in-view=true                 ✅
EAGER/EAGER recursion             ✅
DTO solution                      ✅

Sorting                           ✅
ASC/DESC                          ✅
Multiple sort fields              ✅
Pagination + Sorting              ✅

8.2.B SORTING → COMPLETE ✅
```

Next:

```text
8.2.C — Filtering
```

Expected mental model:

```text
WHERE
 ↓
ORDER BY
 ↓
LIMIT/OFFSET
```
