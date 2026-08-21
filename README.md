# Interview Lab

## Project Objective

This project is a hands-on interview preparation lab where I will implement and practice important backend development concepts using Java, Spring Boot, JPA, MySQL, Spring Security, JWT, AOP, caching, testing, multithreading, asynchronous programming, external API communication and resilience patterns.

The goal is not just to build a CRUD application, but to understand how these concepts work internally and how they are used in production.

---

# Tech Stack

* Java 21
* Spring Boot 4.x
* Spring Data JPA
* Hibernate
* MySQL
* Maven
* Lombok
* Spring Web
* Spring Validation
* JUnit 5
* Mockito
* Spring Security
* JWT
* Ehcache
* Feign
* Resilience4j

---

# Learning Roadmap

```text
Project Foundation
        ↓
CRUD + REST APIs
        ↓
Validation
        ↓
Custom Exception + Global Exception Handling
        ↓
JPA + Entity Mapping
        ↓
Transactions
        ↓
Pagination + Sorting + Filtering
        ↓
Caching
        ↓
Spring Security + JWT
        ↓
AOP
        ↓
JUnit + Mockito
        ↓
Multithreading
        ↓
ExecutorService + ThreadPoolExecutor
        ↓
Future + CompletableFuture
        ↓
@Async
        ↓
RestTemplate + Feign
        ↓
Resilience Patterns
        ↓
Java 8 → 21 Features
        ↓
Production Hardening
```

---

# STEP 1 — Project Foundation

## What I Did

Created a Spring Boot Maven project using Java 21.

Initial project configuration:

* Maven
* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* MySQL Driver
* Validation
* Lombok
* Actuator

Created the initial package structure:

```text
com.interviewlab
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
└── config
```

Created the first API:

```text
GET /api/health
```

Response:

```text
Interview Lab is running
```

## Basic Request Flow Learned

```text
Client
   ↓
Tomcat
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Response
```

Application startup concept:

```text
main()
   ↓
SpringApplication.run()
   ↓
ApplicationContext
   ↓
Bean creation
   ↓
Embedded Tomcat
   ↓
Application Ready
```

## Database Setup

Created MySQL database:

```sql
CREATE DATABASE interview_lab;
```

---

# STEP 2 — Order Domain + CRUD Foundation

## Architecture

Implemented layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
MySQL
```

Created:

```text
Order
OrderStatus
CreateOrderRequest
OrderResponse
OrderRepository
OrderService
OrderServiceImp
OrderController
```

## Order Domain

Order contains:

```text
id
customerName
customerEmail
amount
status
createdAt
updatedAt
```

Order status:

```text
PENDING
CONFIRMED
CANCELLED
```

## Database Table

Hibernate created:

```text
orders
```

Columns:

```text
id
amount
created_at
customer_email
customer_name
status
updated_at
```

---

# Mistakes / Problems Encountered

## 1. Entity name `Order` caused SQL error

Initially:

```java
@Entity
public class Order
```

Hibernate generated:

```sql
create table order (...)
```

MySQL rejected this because `ORDER` is a SQL reserved keyword.

Error:

```text
SQLSyntaxErrorException
```

### Fix

Explicitly specify a safe table name:

```java
@Entity
@Table(name = "orders")
public class Order
```

### Learning

Java class names and database table names do not have to be identical.

Avoid database identifiers that are SQL reserved keywords.

Examples:

```text
order
user
group
key
index
table
```

Prefer names such as:

```text
orders
users
order_items
customer_orders
```

---

## 2. `@Enumerated` default behavior

Initially:

```java
@Enumerated
private OrderStatus status;
```

Hibernate generated:

```text
status tinyint
```

This means enum ordinal values were being stored:

```text
PENDING   → 0
CONFIRMED → 1
CANCELLED → 2
```

This is risky because changing the order of enum constants can change the meaning of existing database values.

### Better

```java
@Enumerated(EnumType.STRING)
private OrderStatus status;
```

Database then stores:

```text
PENDING
CONFIRMED
CANCELLED
```

This is more readable and safer.

---

## 3. `Double` vs `BigDecimal`

Initially amount was:

```java
private Double amount;
```

For monetary values this is not ideal because floating-point numbers can have precision issues.

Changed to:

```java
private BigDecimal amount;
```

Database mapping:

```java
@Column(precision = 15, scale = 2)
private BigDecimal amount;
```

Database:

```text
decimal(15,2)
```

### Learning

Use `BigDecimal` for monetary/financial values rather than `Double` or `float`.

---

## 4. `@RequestBody` was initially missing

Initially:

```java
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(
        CreateOrderRequest orderReq)
```

The request parameter was not explicitly marked as coming from the HTTP request body.

### Fix

```java
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(
        @RequestBody CreateOrderRequest orderReq)
```

### Request flow

```text
JSON Request
    ↓
@RequestBody
    ↓
Jackson
    ↓
Java DTO
```

---

## 5. DTO constructor issue

`CreateOrderRequest` initially had:

```java
@AllArgsConstructor
```

but no no-argument constructor.

For JSON → Java object deserialization, Jackson commonly needs a no-args construction path.

Added:

```java
@NoArgsConstructor
@AllArgsConstructor
```

---

## 6. Order timestamps and status were initially not populated

Initially the entity was built without:

```text
status
createdAt
updatedAt
```

Therefore these values could become `NULL`.

During creation they were explicitly initialized:

```java
.status(OrderStatus.PENDING)
.createdAt(LocalDateTime.now())
.updatedAt(LocalDateTime.now())
```

Later this will be replaced/improved using JPA/Spring auditing mechanisms.

---

# Important Spring Boot Concepts Learned

## Dependency Injection

Initially field injection was used:

```java
@Autowired
OrderService orderService;
```

Better production approach:

```java
private final OrderService orderService;

public OrderController(OrderService orderService) {
    this.orderService = orderService;
}
```

This is called constructor injection.

We will later discuss why constructor injection is generally preferred.

---

# Important REST Concept

For successful resource creation:

```text
POST /api/orders
```

we should normally return:

```text
201 CREATED
```

rather than simply:

```text
200 OK
```

because a new resource was created.

---

# Hibernate / Database Observation

During startup Hibernate generated DDL automatically because:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Important distinction:

```text
Application started successfully
        ≠
Database schema update succeeded
```

The application initially started even though creation of the `order` table failed.

Therefore startup logs must be checked carefully.

---

# MySQL Verification

Database:

```sql
USE interview_lab;
```

Tables:

```sql
SHOW TABLES;
```

Current tables:

```text
order_seq
orders
orders_seq
```

Order structure:

```sql
DESC orders;
```

Data verification:

```sql
SELECT * FROM orders;
```

Successfully created order:

```text
id = 1
amount = 2500.00
customer_email = uday@gmail.com
customer_name = Uday
status = PENDING
```

---

# Git Learning

Git basic workflow:

```text
Working Directory
      ↓
git add
      ↓
Staging Area
      ↓
git commit
      ↓
Local Repository
      ↓
git push
      ↓
GitHub
```

Basic commands:

```bash
git status

git add .

git commit -m "meaningful commit message"

git log

git push
```

Important learning:

`git add` does not commit changes.

`git commit` creates a local Git snapshot.

`git push` sends committed changes to the remote repository.

---

# Git Mistake Encountered

While committing earlier, Git reported:

```text
Author identity unknown
```

because Git username and email were not configured.

Git requires author information for commits.

Configuration:

```bash
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

Then:

```bash
git commit -m "Initial commit"
```

---

# Current Architecture

```text
Client
   ↓
OrderController
   ↓
OrderService
   ↓
OrderRepository
   ↓
Hibernate / JPA
   ↓
MySQL
```

Current API:

```text
GET  /api/health
POST /api/orders
```

---

# Interview Questions From Steps 1–2

1. What happens internally when `SpringApplication.run()` executes?
2. What is IoC?
3. What is Dependency Injection?
4. Why is constructor injection preferred?
5. What is `@RestController`?
6. What is `@RequestMapping`?
7. What is `@RequestBody`?
8. How does JSON get converted into a Java object?
9. What is Jackson?
10. What is the difference between Entity and DTO?
11. Why shouldn't entities normally be exposed directly through REST APIs?
12. What is `JpaRepository`?
13. What does Hibernate do?
14. What is the difference between JPA and Hibernate?
15. How does Hibernate determine a table name?
16. Why did the `Order` entity cause a MySQL syntax error?
17. What is `@Enumerated(EnumType.STRING)`?
18. Why should `BigDecimal` be used for monetary values?
19. What does `ddl-auto=update` do?
20. What is the difference between `200 OK` and `201 CREATED`?
21. What is the difference between `git add`, `git commit`, and `git push`?
22. Why did Git ask for user.name and user.email?
23. Why can an application start even when Hibernate DDL generation has failed?
# STEP 3 — Custom Exception + Global Exception Handling

## Objective

Implement production-style exception handling so that controllers do not need repetitive try-catch blocks.

---

## What I Implemented

Created:

```text
exception/
├── ResourceNotFoundException
├── BadRequestException
└── GlobalExceptionHandler

dto/
└── ErrorResponse
```

Implemented:

```text
GET /api/orders/{id}
```

---

## Request Flow

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
Optional.empty()
   ↓
ResourceNotFoundException
   ↓
@RestControllerAdvice
   ↓
@ExceptionHandler
   ↓
ErrorResponse
   ↓
404 NOT_FOUND
```

---

## Custom Exception

Created:

```java
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### Why RuntimeException?

`RuntimeException` is an unchecked exception, so callers are not forced to explicitly catch or declare it.

Business/resource-related exceptions can be propagated to the global exception handler.

---

## Global Exception Handler

Used:

```java
@RestControllerAdvice
```

with:

```java
@ExceptionHandler(ResourceNotFoundException.class)
```

The global handler converts exceptions into a consistent API response.

Example:

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Order not found with id: 999",
  "path": "/api/orders/999"
}
```

---

## `@RestControllerAdvice`

`@RestControllerAdvice` provides centralized exception handling for REST controllers.

Instead of:

```text
Controller 1 → try/catch
Controller 2 → try/catch
Controller 3 → try/catch
```

we use:

```text
All Controllers
      ↓
GlobalExceptionHandler
```

This improves maintainability and separation of concerns.

---

## `@ExceptionHandler`

Example:

```java
@ExceptionHandler(ResourceNotFoundException.class)
```

This tells Spring that the method should handle `ResourceNotFoundException`.

---

## URI vs URL

Used:

```java
request.getRequestURI()
```

For:

```text
http://localhost:8080/api/orders/999
```

URL:

```text
http://localhost:8080/api/orders/999
```

URI:

```text
/api/orders/999
```

For an API error response, the URI/path is generally more useful.

---

## Optional + `orElseThrow()`

Repository:

```java
orderRepository.findById(id)
```

returns:

```text
Optional<Order>
```

Used:

```java
Order order = orderRepository.findById(id)
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Order not found with id: " + id));
```

Flow:

```text
findById()
     ↓
Optional<Order>
     ↓
Order exists?
   /       \
 YES       NO
  ↓         ↓
Order    orElseThrow()
            ↓
     ResourceNotFoundException
```

### Important Learning

Initially I missed that `orElseThrow()` accepts a lambda/Supplier.

```java
() -> new ResourceNotFoundException(...)
```

The exception is supplied when the Optional is empty.

---

## HTTP Status Codes

### 404 NOT_FOUND

The request is valid, but the requested resource does not exist.

Example:

```text
GET /api/orders/999
```

when order `999` does not exist.

### 400 BAD_REQUEST

The client request itself is invalid.

Examples:

```text
Invalid input
Invalid email
Negative amount
Invalid business request
```

### 500 INTERNAL_SERVER_ERROR

Unexpected server-side failure or programming/system error.

---

## Mistakes / Lessons

### 1. Initially considered try-catch inside Controller

Example:

```java
try {
    ...
} catch (RuntimeException ex) {
    ...
}
```

### Lesson

Do not duplicate exception handling in every controller.

Use a centralized `@RestControllerAdvice`.

---

### 2. Initially missed `orElseThrow()` lambda

Correct:

```java
.orElseThrow(() ->
        new ResourceNotFoundException(...));
```

The lambda acts as an exception supplier.

---

### 3. Error response field naming

Prefer:

```java
private LocalDateTime timestamp;
```

instead of:

```java
private LocalDateTime timeStamp;
```

`timestamp` is the conventional naming.

---

### 4. HTTP status representation

Keep:

```java
private int status;
```

so the JSON contains:

```json
"status": 404
```

rather than:

```json
"status": "404"
```

---

## Current APIs

```text
GET  /api/health
POST /api/orders
GET  /api/orders/{id}
```

---

## Interview Questions

1. What is `@RestControllerAdvice`?
2. What is the difference between `@ControllerAdvice` and `@RestControllerAdvice`?
3. How does `@ExceptionHandler` work?
4. Why shouldn't every controller contain try-catch?
5. Why extend `RuntimeException`?
6. What is the difference between checked and unchecked exceptions?
7. What does `findById()` return?
8. Why use `Optional`?
9. How does `orElseThrow()` work?
10. Why does `orElseThrow()` accept a lambda?
11. What is a `Supplier`?
12. What is the difference between 400, 404 and 500?
13. What is the difference between URI and URL?
14. Where should exception handling happen in a layered Spring Boot application?
15. What happens when an exception propagates from Service to Controller?
# STEP 4 — Request Validation

## Objective

Implement request validation at the API boundary so invalid client input is rejected before reaching the Service/Database layer.

## Concepts

* Jakarta Bean Validation
* `@Valid`
* `@NotNull`
* `@NotBlank`
* `@Email`
* `@Size`
* `@Positive`
* `@PositiveOrZero`
* `@Min`
* `@Max`
* `@Pattern`
* `@Past`
* `@Future`
* Validation error handling
* `MethodArgumentNotValidException`
* Global validation exception handling
* Custom validation
* Class-level validation

## Validation Flow

```text
HTTP Request
    ↓
@RequestBody
    ↓
@Valid
    ↓
Bean Validation
    ↓
Valid?
 ┌──┴──┐
NO    YES
 ↓      ↓
400   Service
       ↓
     Database
```

## Important Principle

Validation should happen as early as possible, at the API boundary.

Invalid requests should not unnecessarily reach the Service or Database layer.
## Learning
Constraint annotations generally do NOT imply @NotNull.

@NotNull → checks presence
@NotBlank → checks non-null + non-empty + non-whitespace String
@NotEmpty → checks non-null + non-empty String/Collection/Map

Value constraints such as:
@Email
@Positive
@Size
@Min/@Max
@DecimalMin/@DecimalMax

generally validate the value when it is present.

Therefore, for a mandatory field, combine:
@NotNull + value constraint

Example:
@NotNull
@Positive
private BigDecimal amount;
## Interview Questions

* What does `@Valid` do?
* What is Bean Validation?
* Difference between `@Valid` and `@Validated`?
* What is `MethodArgumentNotValidException`?
* Difference between `@NotNull`, `@NotEmpty` and `@NotBlank`?
* Why is `@NotBlank` applicable only to character sequences?
* How does Spring convert validation errors into HTTP responses?
* How do we implement custom validation?
* What is class-level validation?
* Where should validation happen?
