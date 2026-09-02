# LEVEL 8.2 --- Pagination + Sorting + Filtering

> Interview Lab --- Spring Data JPA hands-on reference\
> Goal: Understand how `Pageable`, `Page<T>`, sorting and filtering
> become SQL.

------------------------------------------------------------------------

# 1. Why Pagination?

Suppose the `orders` table has 1,00,000 rows.

Without pagination:

``` text
GET /orders
    ↓
SELECT * FROM orders
    ↓
1,00,000 rows
    ↓
Huge response
```

With pagination:

``` text
GET /orders?page=0&size=10
    ↓
Only 10 rows
```

The basic idea:

``` text
Database
    ↓
Only required slice
    ↓
Application
    ↓
Client
```

Pagination reduces:

-   Database result size
-   Network payload
-   Memory usage
-   Serialization work
-   Response time

------------------------------------------------------------------------

# 2. Mental Model

The most important mental model:

``` text
HTTP Request
    ↓
Controller
    ↓
Pageable
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
SQL
    ↓
Database
```

For:

``` text
?page=1&size=10
```

Spring Data conceptually translates this into:

``` sql
LIMIT 10
OFFSET 10
```

Because page numbering is zero-based:

``` text
page = 0, size = 10
OFFSET = 0

page = 1, size = 10
OFFSET = 10

page = 2, size = 10
OFFSET = 20
```

Formula:

``` text
OFFSET = page × size
```

------------------------------------------------------------------------

# 3. What is `Pageable`?

`Pageable` is Spring Data's request object representing pagination and
sorting information.

Example:

``` java
Pageable pageable
```

It can contain:

``` text
page number
page size
sort field
sort direction
```

Example request:

``` text
/orders?page=1&size=5&sort=amount,desc
```

Conceptually:

``` text
Pageable
├── page = 1
├── size = 5
└── sort = amount DESC
```

------------------------------------------------------------------------

# 4. What is `Page<T>`?

`Page<T>` is the result container returned by Spring Data.

Example:

``` java
Page<Order> result = orderRepository.findAll(pageable);
```

A `Page` contains more than just the current records.

Useful information:

``` java
result.getContent()
result.getNumber()
result.getSize()
result.getTotalElements()
result.getTotalPages()
result.hasNext()
result.hasPrevious()
```

Mental model:

``` text
Page<Order>
│
├── content
├── current page
├── page size
├── total elements
├── total pages
├── hasNext
└── hasPrevious
```

------------------------------------------------------------------------

# 5. Internal Working of Pagination

Request:

``` text
GET /orders?page=1&size=5
```

Flow:

``` text
Browser
   ↓
Controller
   ↓
Spring MVC creates Pageable
   ↓
Pageable(page=1,size=5)
   ↓
Service
   ↓
Repository.findAll(pageable)
   ↓
Spring Data JPA
   ↓
Hibernate
   ↓
SQL
```

SQL is conceptually:

``` sql
SELECT ...
FROM orders
LIMIT 5 OFFSET 5;
```

The database performs the actual limiting.

Important:

> Spring does not normally fetch all rows and then manually remove rows
> from the list. The pagination information is passed down to the JPA
> provider/database.

------------------------------------------------------------------------

# 6. Why Does a `Page` Often Cause a COUNT Query?

A `Page<T>` provides:

``` java
getTotalElements()
getTotalPages()
```

To calculate those values, Spring Data commonly needs the total number
of matching records.

Therefore a paginated repository call can produce two SQL queries:

``` sql
SELECT ...
FROM orders
LIMIT 5 OFFSET 5;
```

and:

``` sql
SELECT COUNT(*)
FROM orders;
```

Mental model:

``` text
Page request
   ↓
Content query
   ↓
5 records

+

Count query
   ↓
Total records
```

Example:

``` text
Total records = 23
Page size     = 5

Total pages = 5
```

Because:

``` text
23 / 5 = 4.6
→ 5 pages
```

------------------------------------------------------------------------

# 7. `Page` vs `Slice`

This distinction is interview-important.

## `Page<T>`

Provides:

``` text
current data
+
total elements
+
total pages
```

It may require a count query.

## `Slice<T>`

Provides:

``` text
current data
+
whether another slice exists
```

It does not need the total count in the same way.

Mental model:

``` text
Page
→ "How many pages exist?"

Slice
→ "Is there another page?"
```

If an application does not need total-page information, `Slice` can
avoid the cost of calculating the complete count.

------------------------------------------------------------------------

# 8. Basic Pagination Implementation

Repository:

``` java
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

No custom method is required for basic pagination because
`JpaRepository` already provides:

``` java
Page<T> findAll(Pageable pageable);
```

Service:

``` java
@Transactional(readOnly = true)
public Page<Order> getOrders(Pageable pageable) {
    return orderRepository.findAll(pageable);
}
```

Controller:

``` java
@GetMapping
public Page<Order> getOrders(Pageable pageable) {
    return orderService.getOrders(pageable);
}
```

Request:

``` text
GET /api/orders?page=0&size=5
```

------------------------------------------------------------------------

# 9. Default Page Parameters

If the client sends:

``` text
GET /api/orders
```

Spring can use configured/default pagination values.

You can explicitly define defaults:

``` java
@GetMapping
public Page<Order> getOrders(
        @PageableDefault(size = 10, page = 0)
        Pageable pageable) {

    return orderService.getOrders(pageable);
}
```

------------------------------------------------------------------------

# 10. Sorting

Sorting can be sent through `Pageable`.

Example:

``` text
/orders?page=0&size=5&sort=amount,desc
```

Conceptually:

``` text
Pageable
├── page = 0
├── size = 5
└── sort = amount DESC
```

Hibernate can generate SQL similar to:

``` sql
SELECT ...
FROM orders
ORDER BY amount DESC
LIMIT 5 OFFSET 0;
```

Multiple sorting fields are also possible:

``` text
?sort=status,asc&sort=amount,desc
```

Conceptually:

``` sql
ORDER BY status ASC, amount DESC
```

------------------------------------------------------------------------

# 11. Sorting Mental Model

``` text
HTTP
 ↓
sort=amount,desc
 ↓
Spring Pageable
 ↓
Sort object
 ↓
Spring Data JPA
 ↓
Hibernate
 ↓
ORDER BY amount DESC
 ↓
Database
```

The important point:

> Sorting is ultimately performed by the database through `ORDER BY`,
> not by loading the entire table into Java and sorting it manually.

------------------------------------------------------------------------

# 12. Filtering

Pagination answers:

> "Which portion of the result?"

Sorting answers:

> "In which order?"

Filtering answers:

> "Which rows should qualify?"

Example:

``` text
/orders?customerName=Uday
```

could translate conceptually to:

``` sql
SELECT ...
FROM orders
WHERE customer_name = 'Uday';
```

Filtering can be implemented using:

-   Derived query methods
-   JPQL
-   Specifications
-   Criteria API
-   QueryDSL
-   Native SQL

For this project we stay with simple Spring Data derived queries + JPQL
where useful.

------------------------------------------------------------------------

# 13. Simple Derived Query Filtering

Repository:

``` java
List<Order> findByCustomerName(String customerName);
```

Spring Data derives:

``` sql
SELECT ...
FROM orders
WHERE customer_name = ?;
```

For partial matching:

``` java
List<Order> findByCustomerNameContainingIgnoreCase(
        String customerName);
```

Conceptually:

``` sql
WHERE LOWER(customer_name)
LIKE LOWER('%Uday%');
```

------------------------------------------------------------------------

# 14. Filtering + Pagination Together

This is the important real-project combination.

Repository:

``` java
Page<Order> findByCustomerNameContainingIgnoreCase(
        String customerName,
        Pageable pageable);
```

Request:

``` text
/orders/search
    ?customerName=Uday
    &page=0
    &size=5
    &sort=amount,desc
```

Mental model:

``` text
Filter
   ↓
WHERE customer_name LIKE ...
   ↓
Sort
   ↓
ORDER BY amount DESC
   ↓
Pagination
   ↓
LIMIT 5 OFFSET 0
```

Conceptually:

``` sql
SELECT ...
FROM orders
WHERE LOWER(customer_name) LIKE LOWER('%Uday%')
ORDER BY amount DESC
LIMIT 5 OFFSET 0;
```

And for `Page<Order>`, a count query can also be generated:

``` sql
SELECT COUNT(*)
FROM orders
WHERE LOWER(customer_name) LIKE LOWER('%Uday%');
```

------------------------------------------------------------------------

# 15. Combined Internal Flow

This is the interview-level flow to remember:

``` text
HTTP Request
    ↓
?page=1
&size=5
&sort=amount,desc
&customerName=Uday
    ↓
Spring MVC
    ↓
Pageable
    +
Filter parameter
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
SQL
    ↓
WHERE customer_name LIKE ...
ORDER BY amount DESC
LIMIT 5 OFFSET 5
    ↓
Database
    ↓
Page<T>
    ↓
JSON Response
```

------------------------------------------------------------------------

# 16. Important Ordering of SQL Operations

Conceptually, the database applies:

``` text
FROM
 ↓
WHERE
 ↓
ORDER BY
 ↓
LIMIT / OFFSET
```

Therefore:

``` text
Filtering
    ↓
Sorting
    ↓
Pagination
```

is the useful mental model.

Example:

``` text
100 orders
    ↓
WHERE customer = Uday
    ↓
30 matching orders
    ↓
ORDER BY amount DESC
    ↓
30 sorted orders
    ↓
LIMIT 5 OFFSET 5
    ↓
5 returned rows
```

This is why filtering and sorting should be pushed to the database
rather than done manually in Java.

------------------------------------------------------------------------

# 17. DTO + Pagination

In a real project, returning entities directly from the controller is
often not ideal.

Instead:

``` java
Page<OrderResponse> response =
        orderRepository.findAll(pageable)
                .map(this::mapToResponse);
```

`Page.map()` transforms each element while preserving page metadata.

Mental model:

``` text
Page<Order>
    ↓ map()
Page<OrderResponse>
```

Metadata remains:

``` text
totalElements
totalPages
page
size
```

while content changes:

``` text
Order → OrderResponse
```

------------------------------------------------------------------------

# 18. Why `Pageable` Is Powerful

One parameter can carry:

``` text
page
size
sort
```

Example:

``` java
Pageable pageable
```

instead of manually creating:

``` text
pageNumber
pageSize
sortField
sortDirection
```

Spring MVC can automatically bind HTTP parameters into `Pageable`.

------------------------------------------------------------------------

# 19. Security / API Design Consideration

Never blindly allow clients to request:

``` text
size=1000000
```

A large page can create:

-   Large DB result
-   Large memory usage
-   Large response
-   High network usage

Use a maximum page size.

Example configuration/custom validation can enforce a safe limit.

Interview point:

> Pagination protects the application only if page size is also
> controlled.

------------------------------------------------------------------------

# 20. Offset Pagination Limitation

Traditional pagination uses:

``` sql
LIMIT 20 OFFSET 100000;
```

For very large datasets, large offsets can become increasingly expensive
because the database may still need to walk through many rows before
returning the requested portion.

For huge/high-throughput datasets, another strategy is:

``` text
Cursor / Keyset Pagination
```

Example concept:

``` text
WHERE id > lastSeenId
ORDER BY id
LIMIT 20
```

This is outside the current hands-on scope but important to recognize in
interviews.

------------------------------------------------------------------------

# 21. Practical Repository Examples for Interview Lab

Basic:

``` java
Page<Order> findAll(Pageable pageable);
```

Filter:

``` java
Page<Order> findByCustomerNameContainingIgnoreCase(
        String customerName,
        Pageable pageable);
```

Exact status:

``` java
Page<Order> findByStatus(
        OrderStatus status,
        Pageable pageable);
```

Amount range:

``` java
Page<Order> findByAmountBetween(
        BigDecimal min,
        BigDecimal max,
        Pageable pageable);
```

Multiple conditions:

``` java
Page<Order> findByStatusAndAmountBetween(
        OrderStatus status,
        BigDecimal min,
        BigDecimal max,
        Pageable pageable);
```

------------------------------------------------------------------------

# 22. Dynamic Filtering

Derived methods are fine when filters are known:

``` text
findByStatusAndAmountBetween(...)
```

But imagine a search screen where users can optionally provide:

``` text
customerName
status
minAmount
maxAmount
createdAfter
createdBefore
```

You would not want dozens of methods:

``` text
findByStatus(...)
findByCustomerName(...)
findByStatusAndCustomerName(...)
findByStatusAndAmount(...)
...
```

For dynamic filtering, common solutions are:

``` text
JpaSpecificationExecutor
        ↓
Specification
        ↓
dynamic WHERE clauses
```

or:

``` text
QueryDSL
Criteria API
```

For this project, we only need the interview-level concept.

------------------------------------------------------------------------

# 23. Example Dynamic Filtering Architecture

``` text
Search Request
    ↓
Filter DTO
    ↓
Specification
    ↓
WHERE predicates
    ↓
Pageable
    ↓
Repository
    ↓
Hibernate
    ↓
SQL
```

The key distinction:

``` text
Specification
→ dynamic WHERE

Pageable
→ page + size + sorting
```

They solve different problems but work together.

------------------------------------------------------------------------

# 24. SQL Test Data

Use the following MySQL data to make pagination, sorting and filtering
easy to observe.

## Customers

``` sql
INSERT INTO customer
    (customer_name, customer_email)
VALUES
    ('Alice', 'alice@gmail.com'),
    ('Bob', 'bob@gmail.com'),
    ('Charlie', 'charlie@gmail.com'),
    ('David', 'david@gmail.com'),
    ('Emma', 'emma@gmail.com');
```

## Orders

Adjust `customer_id` if your generated customer IDs differ.

If the above inserts create IDs 20--24:

``` sql
INSERT INTO orders
    (
        customer_name,
        customer_email,
        amount,
        status,
        created_at,
        updated_at,
        discount,
        customer_id,
        version
    )
VALUES
    ('Alice',   'alice@gmail.com',   1000.00, 'PENDING',
     '2026-09-01 09:00:00', '2026-09-01 09:00:00', 50.00, 20, 0),

    ('Bob',     'bob@gmail.com',     5000.00, 'CONFIRMED',
     '2026-09-01 09:05:00', '2026-09-01 09:05:00', 100.00, 21, 0),

    ('Charlie', 'charlie@gmail.com', 1500.00, 'PENDING',
     '2026-09-01 09:10:00', '2026-09-01 09:10:00', 0.00, 22, 0),

    ('David',   'david@gmail.com',   9000.00, 'CONFIRMED',
     '2026-09-01 09:15:00', '2026-09-01 09:15:00', 200.00, 23, 0),

    ('Emma',    'emma@gmail.com',    2500.00, 'CANCELLED',
     '2026-09-01 09:20:00', '2026-09-01 09:20:00', 25.00, 24, 0),

    ('Alice',   'alice@gmail.com',   7000.00, 'CONFIRMED',
     '2026-09-01 09:25:00', '2026-09-01 09:25:00', 150.00, 20, 0),

    ('Bob',     'bob@gmail.com',     800.00, 'PENDING',
     '2026-09-01 09:30:00', '2026-09-01 09:30:00', 0.00, 21, 0),

    ('Charlie', 'charlie@gmail.com', 3200.00, 'CONFIRMED',
     '2026-09-01 09:35:00', '2026-09-01 09:35:00', 75.00, 22, 0),

    ('David',   'david@gmail.com',   4500.00, 'PENDING',
     '2026-09-01 09:40:00', '2026-09-01 09:40:00', 100.00, 23, 0),

    ('Emma',    'emma@gmail.com',    12000.00, 'CONFIRMED',
     '2026-09-01 09:45:00', '2026-09-01 09:45:00', 300.00, 24, 0);
```

Important:

> Do not blindly use `20–24` as customer IDs. First run:

``` sql
SELECT id, customer_name, customer_email
FROM customer
ORDER BY id;
```

Then replace the `customer_id` values in the order INSERT according to
your actual IDs.

------------------------------------------------------------------------

# 25. Suggested Hands-on Experiments

## Experiment A --- Basic Pagination

Request:

``` text
GET /api/orders?page=0&size=3
```

Expected:

``` text
3 orders
```

Then:

``` text
GET /api/orders?page=1&size=3
```

Expected:

``` text
next 3 orders
```

Check Hibernate SQL for:

``` text
LIMIT
OFFSET
```

------------------------------------------------------------------------

## Experiment B --- Pagination Metadata

Print/inspect:

``` java
page.getNumber()
page.getSize()
page.getTotalElements()
page.getTotalPages()
page.hasNext()
```

Prove:

``` text
Page content
+
total count
+
page information
```

------------------------------------------------------------------------

## Experiment C --- Sorting

Request:

``` text
GET /api/orders?page=0&size=10&sort=amount,desc
```

Expected:

``` text
Highest amount first
```

Check SQL:

``` sql
ORDER BY amount DESC
```

------------------------------------------------------------------------

## Experiment D --- Filtering

Repository:

``` java
Page<Order> findByCustomerNameContainingIgnoreCase(
        String customerName,
        Pageable pageable);
```

Request:

``` text
GET /api/orders/search?customerName=Ali&page=0&size=5
```

Expected:

``` text
Only Alice's orders
```

Check SQL:

``` sql
WHERE ...
```

------------------------------------------------------------------------

## Experiment E --- Filtering + Sorting + Pagination

Request:

``` text
GET /api/orders/search
    ?customerName=Ali
    &page=0
    &size=5
    &sort=amount,desc
```

Expected flow:

``` text
Filter
 ↓
Alice orders
 ↓
Sort amount DESC
 ↓
Take page 0
 ↓
size 5
```

SQL should conceptually contain:

``` sql
WHERE ...
ORDER BY amount DESC
LIMIT 5 OFFSET 0
```

This is the most important combined experiment.

------------------------------------------------------------------------

# 26. Common Mistakes

### Mistake 1

Thinking:

``` text
page=1
```

means first page.

It does not.

Spring Data pagination is zero-based:

``` text
page=0 → first page
page=1 → second page
```

------------------------------------------------------------------------

### Mistake 2

Thinking pagination happens in Java.

Usually:

``` text
Hibernate
 ↓
Database
 ↓
LIMIT/OFFSET
```

The database performs the actual row limiting.

------------------------------------------------------------------------

### Mistake 3

Thinking `Page<T>` only contains records.

It also contains pagination metadata.

------------------------------------------------------------------------

### Mistake 4

Using huge page sizes.

Always consider maximum page size.

------------------------------------------------------------------------

### Mistake 5

Returning entities directly from every API.

Prefer DTOs for controlled API contracts.

------------------------------------------------------------------------

### Mistake 6

Creating dozens of derived methods for every possible filter
combination.

For highly dynamic filtering, use:

``` text
Specification
or
QueryDSL
```

------------------------------------------------------------------------

# 27. Final Interview Mental Model

``` text
                 SEARCH REQUEST
                       ↓
        ┌──────────────┼──────────────┐
        ↓              ↓              ↓
     FILTER          SORT         PAGINATION
        ↓              ↓              ↓
      WHERE         ORDER BY     LIMIT/OFFSET
        └──────────────┼──────────────┘
                       ↓
                  Spring Data
                       ↓
                    Hibernate
                       ↓
                     SQL
                       ↓
                   Database
                       ↓
                    Page<T>
                       ↓
                     DTO
                       ↓
                  JSON Response
```

Remember:

``` text
Filtering  → WHERE
Sorting    → ORDER BY
Pagination → LIMIT + OFFSET
```

------------------------------------------------------------------------

# 28. One-Liner Interview Answers

### What is `Pageable`?

> A Spring Data abstraction that carries pagination and sorting
> information such as page number, page size and sort order.

### What is `Page<T>`?

> A result wrapper containing the current page content plus metadata
> such as total elements, total pages and navigation information.

### Does pagination happen in Java?

> Normally no. Spring Data passes pagination to the JPA provider, which
> generates database-level pagination such as LIMIT/OFFSET.

### Why does `Page<T>` often execute a count query?

> Because total elements and total pages require knowing how many
> records match the query.

### `Page` vs `Slice`?

> `Page` provides total-count metadata; `Slice` focuses on whether
> another slice exists and can avoid the full count operation.

### How does sorting work?

> `Pageable` carries a `Sort`, Spring Data passes it to Hibernate, and
> Hibernate generates an SQL `ORDER BY`.

### How does filtering work?

> Filtering becomes database predicates such as SQL `WHERE`. It can be
> implemented using derived queries, JPQL, Specifications, Criteria API
> or QueryDSL.

### What happens when filtering, sorting and pagination are combined?

``` text
WHERE
 ↓
ORDER BY
 ↓
LIMIT/OFFSET
```

------------------------------------------------------------------------

# 29. Level 8 --- Current Progress

``` text
8.1 Caching
    ↓
    @Cacheable
    @CachePut
    @CacheEvict
    Cache invalidation
    TTL
    Ehcache
    Redis architecture

8.2 Pagination + Sorting + Filtering
    ↓
    Pageable
    Page<T>
    Slice<T>
    LIMIT/OFFSET
    ORDER BY
    WHERE
    Filtering + Sorting + Pagination
    DTO mapping
    Dynamic filtering concept
```

Caching + Pagination + Sorting + Filtering foundation is now complete at
the intended interview/project level.
