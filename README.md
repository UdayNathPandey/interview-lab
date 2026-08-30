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
# STEP 4 — PART 2: Custom / Class-Level Validation

## Objective

Implement custom validation for business rules that cannot be validated by a single field.

## Why Custom Validation?

Standard annotations can validate individual fields:

```text id="a8f2bc"
@NotBlank
@Email
@Positive
@Size
@DecimalMin
@DecimalMax
```

But some business rules involve multiple fields.

Examples:

```text id="i5q4e8"
startDate < endDate
password == confirmPassword
minimumAmount <= maximumAmount
discount < orderAmount
```

These require class-level validation.

## Architecture

```text id="h5t9e3"
DTO
 ↓
Custom Annotation
 ↓
ConstraintValidator
 ↓
Business validation
 ↓
Validation Result
 ↓
MethodArgumentNotValidException
 ↓
GlobalExceptionHandler
 ↓
400 BAD_REQUEST
```

## Important Concepts

* Custom Constraint Annotation
* `@Constraint`
* `ConstraintValidator`
* `isValid()`
* Class-level validation
* `ConstraintValidatorContext`
* `@Target`
* `@Retention`
* `validatedBy`

## Interview Questions

* Why do we need custom validation?
* What is `ConstraintValidator`?
* Difference between field-level and class-level validation?
* What does `@Constraint(validatedBy = ...)` do?
* What is `ConstraintValidatorContext`?
* Why is `isValid()` returning boolean?
* How does Spring discover custom validation annotations?
## STEP 4 — PART 2: Custom / Class-Level Validation

### Why Custom Validation?

Standard constraints validate individual values:

```text id="m7k2p4"
@NotBlank
@Email
@Positive
@DecimalMin
@DecimalMax
```

But business rules may depend on multiple fields.

Example:

```text id="q3v8n1"
discount < amount
```

This cannot be expressed cleanly using a single standard field-level annotation.

---

### Custom Validation Architecture

```text id="h4m9s2"
@ValidOrderDiscount
        ↓
OrderDiscountValidator
        ↓
ConstraintValidator
        ↓
isValid()
        ↓
true / false
```

---

### Custom Annotation

Created:

```java id="x8q2m5"
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderDiscountValidator.class)
@Documented
public @interface ValidOrderDiscount {

    String message() default "Discount must be less than amount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

---

### Annotation Meta-Annotations

#### `@Target(ElementType.TYPE)`

The custom annotation can be applied to a class/type.

Required because the validation compares multiple fields.

#### `@Retention(RetentionPolicy.RUNTIME)`

The annotation remains available at runtime so the validation framework can inspect it.

#### `@Constraint(validatedBy = ...)`

Connects the custom annotation with its validation implementation.

```text id="q5n7r2"
@ValidOrderDiscount
       ↓
OrderDiscountValidator
```

#### `@Documented`

Controls whether usage of the annotation is included in generated JavaDoc.

It is NOT required for validation.

---

### ConstraintValidator

Implemented:

```java id="n6p3k8"
ConstraintValidator<ValidOrderDiscount, CreateOrderRequest>
```

Meaning:

```text id="w9m4q1"
ValidOrderDiscount
       ↓
validates
       ↓
CreateOrderRequest
```

`isValid()` returns:

```text id="b2k7v5"
true  → validation passes
false → validation fails
```

---

### Null Handling

Custom validator does not need to enforce null checks when fields already have:

```java id="c8r2m6"
@NotNull
```

Therefore:

```java id="p7n3q9"
if (request == null) return true;

if (amount == null || discount == null) return true;
```

The custom validator focuses on the cross-field business rule.

---

### `BigDecimal.compareTo()`

For:

```text id="j4m8v2"
discount < amount
```

use:

```java id="y6q3p9"
discount.compareTo(amount) < 0
```

Do not use `<` / `>` operators with `BigDecimal`.

`compareTo()` returns:

```text id="f2n7k4"
negative → first value is smaller
zero     → values are equal
positive → first value is greater
```

---

### `ConstraintValidatorContext`

A class-level validation error does not automatically belong to a particular field.

To associate the error with `discount`:

```java id="z5m8q3"
context.disableDefaultConstraintViolation();

context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate()
)
.addPropertyNode("discount")
.addConstraintViolation();
```

This allows the global exception handler to produce:

```json id="r7p2m9"
{
  "validationErrors": {
    "discount": "Discount must be less than amount"
  }
}
```

---

### Three Validation Layers

```text id="k8m3q5"
1. Presence Validation
   @NotNull
   @NotBlank
   @NotEmpty

2. Value Validation
   @Email
   @Size
   @Positive
   @DecimalMin
   @DecimalMax

3. Business / Cross-field Validation
   @ValidOrderDiscount
   ConstraintValidator
```

---

### Mistake / Learning

Initially the validator was adding a constraint violation before checking whether the data was valid.

Incorrect flow:

```text id="g4q8m2"
Add violation
    ↓
Check validity
```

Correct flow:

```text id="v6n2p9"
Check validity
    ↓
Valid → return true

Invalid
    ↓
Add violation
    ↓
return false
```

This is important because adding a violation manually can make even valid input fail validation.

---

### `@Documented` Learning

`@Documented` has no effect on validation logic.

It controls whether the annotation is included in generated JavaDoc.

```text id="m9q3r6"
@Retention → runtime availability
@Documented → JavaDoc documentation
```

---

### Jakarta Validation Package

Spring Boot 4 uses:

```java id="y3p7n1"
jakarta.validation.*
```

Older Spring applications may use:

```java id="s8m4q2"
javax.validation.*
```

This is a common migration/interview point.

---

### Interview Questions

1. Why do we need custom validation?
2. What is `ConstraintValidator`?
3. What does `@Constraint` do?
4. Why use `@Target(TYPE)`?
5. What does `@Retention(RUNTIME)` mean?
6. What does `@Documented` do?
7. What is `ConstraintValidatorContext`?
8. Why use `addPropertyNode()`?
9. Why use `BigDecimal.compareTo()`?
10. Why should custom validators generally return `true` for null when `@NotNull` handles presence?
11. Difference between field-level and class-level validation?
12. How does a custom validation error reach `GlobalExceptionHandler`?
# STEP 5 — Complete CRUD

## Objective

Complete the Order REST API with:

* GET all orders
* GET order by ID
* POST order
* PUT order
* PATCH order
* DELETE order

## REST API

```text
GET     /api/orders
GET     /api/orders/{id}
POST    /api/orders
PUT     /api/orders/{id}
PATCH   /api/orders/{id}
DELETE  /api/orders/{id}
```

## Important Concepts

* REST CRUD
* `ResponseEntity`
* HTTP status codes
* PUT vs PATCH
* Idempotency
* Partial update
* Resource existence validation
* DTO separation
* Reusing `ResourceNotFoundException`

## Expected Status Codes

```text
POST   → 201 CREATED
GET    → 200 OK
PUT    → 200 OK
PATCH  → 200 OK
DELETE → 204 NO_CONTENT
Missing resource → 404 NOT_FOUND
Invalid request → 400 BAD_REQUEST
```

## PUT vs PATCH

PUT generally represents replacement/update of the resource representation.

PATCH represents a partial modification of the resource.

Example:

```text
PUT
Client sends the complete update representation.

PATCH
Client sends only the fields that need modification.
```

## Idempotency

PUT is intended to be idempotent.

Repeating the same PUT request should result in the same final resource state.

PATCH may or may not be idempotent depending on the operation/design.

## Architecture

```text
Controller
    ↓
Validation
    ↓
Service
    ↓
Repository
    ↓
Database
```

## Interview Questions

* PUT vs PATCH?
* Is POST idempotent?
* Is PUT idempotent?
* Is PATCH idempotent?
* Why use 204 for DELETE?
* Should DELETE return the deleted object?
* What should happen if the resource doesn't exist?
* Why use DTOs instead of entities?
# STEP 5 — Full CRUD

## Objective

Completed complete CRUD operations for Order.

```text
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}
PATCH  /api/orders/{id}
DELETE /api/orders/{id}
```

---

## HTTP Status Codes

```text
POST   → 201 CREATED
GET    → 200 OK
PUT    → 200 OK
PATCH  → 200 OK
DELETE → 204 NO_CONTENT
```

Missing resource:

```text
404 NOT_FOUND
```

Invalid request:

```text
400 BAD_REQUEST
```

---

## GET All + Stream API

Initially considered a traditional `for` loop.

Then implemented:

```java
orderRepository.findAll()
        .stream()
        .map(...)
        .toList();
```

### Learning

```text
stream()
    ↓
map()
    ↓
transform Entity → DTO
    ↓
toList()
```

`Stream.toList()` was introduced in Java 16.

Before Java 16, common syntax was:

```java
.collect(Collectors.toList())
```

---

## Entity vs DTO

The API does not directly expose the JPA Entity.

```text
Database
   ↓
Order Entity
   ↓
Service
   ↓
OrderResponse DTO
   ↓
Controller
   ↓
Client
```

This keeps persistence models separated from API contracts.

---

## PUT

Implemented:

```text
PUT /api/orders/{id}
```

PUT uses `UpdateOrderRequest` where required fields are validated.

Typical representation:

```json
{
  "customerName": "Uday",
  "customerEmail": "uday@gmail.com",
  "amount": 3000,
  "discount": 100,
  "status": "CONFIRMED"
}
```

PUT represents a complete update/replacement-style operation.

---

## PATCH

Implemented:

```text
PATCH /api/orders/{id}
```

PATCH uses `PatchOrderRequest` where fields are optional.

Example:

```json
{
  "amount": 3000
}
```

Only the supplied field is updated.

### Important Difference

```text
PUT
→ complete representation/update

PATCH
→ partial modification
```

---

## PUT vs PATCH Validation

PUT:

```text
@NotNull
@NotBlank
...
```

required fields.

PATCH:

```text
@Email
@Size
@DecimalMin
...
```

without presence constraints, because omitted fields should remain unchanged.

---

## DELETE

Implemented:

```text
DELETE /api/orders/{id}
```

Successful deletion returns:

```text
204 NO_CONTENT
```

Controller:

```java
orderService.deleteOrder(id);
return ResponseEntity.noContent().build();
```

### `void` vs `Void`

Initially used:

```java
Void deleteOrder()
```

and returned:

```java
return null;
```

Learning:

```text
void
→ method returns nothing

Void
→ reference type representing absence of a value
```

For a normal service method with no return value, prefer:

```java
void deleteOrder(Long id)
```

---

## Resource Not Found

PUT, PATCH and DELETE first verify that the order exists.

```java
orderRepository.findById(id)
        .orElseThrow(() ->
                new ResourceNotFoundException(...));
```

Therefore:

```text
PUT/PATCH/DELETE non-existing ID
        ↓
ResourceNotFoundException
        ↓
GlobalExceptionHandler
        ↓
404
```

---

## Mistakes / Lessons

### 1. Forgot `@Valid` on PUT

Without:

```java
@Valid @RequestBody
```

Bean Validation does not run for the request DTO.

Correct:

```java
@Valid @RequestBody UpdateOrderRequest request
```

---

### 2. Initially used 200 for POST

Changed:

```text
200 OK
```

to:

```text
201 CREATED
```

because POST successfully created a new resource.

---

### 3. Initially used `200 OK` for DELETE

Changed:

```text
200 OK
```

to:

```text
204 NO_CONTENT
```

because successful DELETE does not need to return a response body.

---

### 4. Initially used a `for` loop for GET all

Later realized collection transformation is a good use case for Stream API.

Implemented:

```java
.stream()
.map(...)
.toList()
```

---

### 5. Initially didn't know `Void` requires `null`

Learning:

Prefer `void` for methods that don't return a value.

---

## Important PATCH Limitation

Current PATCH implementation uses:

```java
if (request.getAmount() != null) {
    ...
}
```

Therefore:

```text
field omitted
```

and:

```text
field explicitly set to null
```

cannot be distinguished.

This is acceptable for the current learning implementation.

Advanced PATCH semantics can later be explored using JSON Patch / JSON Merge Patch.

---

## Business Validation Observation

`@ValidOrderDiscount` works well for DTO-level validation when both values are present in the request.

For PATCH, a rule such as:

```text
discount < existingOrder.amount
```

may require Service/business-layer validation because the current database value may not be included in the PATCH request.

Important principle:

```text
Bean Validation
→ request/input validation

Service/business validation
→ rules involving existing state/business context
```

---

## Interview Questions

1. What are the CRUD HTTP methods?
2. Why POST returns 201?
3. Why DELETE commonly returns 204?
4. PUT vs PATCH?
5. Is PUT idempotent?
6. Is POST idempotent?
7. Is PATCH idempotent?
8. Why shouldn't entities be directly exposed from REST APIs?
9. Why use DTOs?
10. What is `Stream.map()`?
11. When was `Stream.toList()` introduced?
12. `Collectors.toList()` vs `Stream.toList()`?
13. `void` vs `Void`?
14. How do you handle DELETE of a non-existing resource?
15. Why is `@Valid` required on `@RequestBody`?
16. How would you implement PATCH when `null` has semantic meaning?
17. Which validations belong to Bean Validation vs Service layer?
# STEP 6 — JPA / Database Deep Dive

## Objective

Understand how Spring Data JPA + Hibernate interact with the database.

## Topics

1. Entity mapping
2. @Table / @Column
3. Primary key generation
4. Enum mapping
5. @Temporal / Java date-time mapping
6. Entity relationships
    - @OneToOne
    - @OneToMany
    - @ManyToOne
    - @ManyToMany
7. Fetch strategies
    - LAZY
    - EAGER
8. Persistence Context
9. Entity lifecycle
10. Dirty Checking
11. @Transactional
12. Commit / Rollback
13. Transaction propagation
14. Isolation levels
15. Cascade
16. orphanRemoval
17. N+1 query problem
18. Fetch Join
19. EntityGraph
20. Optimistic / Pessimistic locking

## Current Database

MySQL

## Current ORM

Spring Data JPA + Hibernate

## Important Interview Goal

Don't just learn annotations.

Understand:

Java Entity
↓
Persistence Context
↓
Hibernate
↓
SQL
↓
MySQL

and how changes to an Entity become database changes.
## STEP 6.1 — Entity Mapping

### @Entity
Marks a Java class as a JPA persistent entity.

### @Table
Explicitly defines the database table name.

@Entity
@Table(name = "orders")

### @Column

Used to control database column mapping.

@Column(
name = "customer_name",
nullable = false,
length = 50
)

Result:

customer_name varchar(50) NOT NULL

### precision / scale

For BigDecimal:

precision = total number of digits
scale = number of digits after decimal

Example:

precision = 15
scale = 2

→ DECIMAL(15,2)

Useful for monetary values.

### @Enumerated(EnumType.STRING)

Stores enum names instead of ordinal numbers.

Prefer:

PENDING
CONFIRMED
CANCELLED

over:

0
1
2

because adding/reordering enum constants can make ordinal persistence unsafe.

### GenerationType.IDENTITY

With MySQL:

@GeneratedValue(strategy = GenerationType.IDENTITY)

maps naturally to:

AUTO_INCREMENT

### @NotNull vs @Column(nullable=false)

@NotNull
→ application/request validation

@Column(nullable=false)
→ database schema constraint

Both can be useful.

### ddl-auto=update

Hibernate can attempt to synchronize entity changes with an existing schema.

Useful for development.

Do NOT rely on ddl-auto=update as the production database migration strategy.

Production normally uses migration tools such as Flyway/Liquibase.

### Experiment

Changing:

customer_name varchar(255) NULL
↓
@Column(length=50, nullable=false)
↓
customer_name varchar(50) NOT NULL

Changing:

discount decimal(38,2)
↓
precision=15, scale=2
↓
discount decimal(15,2)

Changing:

GenerationType.AUTO
↓
GenerationType.IDENTITY
↓
AUTO_INCREMENT

### Mistake / Learning

Initially did not know precision and scale.

Initially used AUTO generation strategy.

Learned to inspect actual DB schema using:

DESC orders;

and:

SHOW CREATE TABLE orders;
## STEP 6.1 — Entity Mapping

### @Entity
Marks a Java class as a JPA persistent entity.

### @Table
Explicitly defines the database table name.

@Entity
@Table(name = "orders")

### @Column

Used to control database column mapping.

@Column(
name = "customer_name",
nullable = false,
length = 50
)

Result:

customer_name varchar(50) NOT NULL

### precision / scale

For BigDecimal:

precision = total number of digits
scale = number of digits after decimal

Example:

precision = 15
scale = 2

→ DECIMAL(15,2)

Useful for monetary values.

### @Enumerated(EnumType.STRING)

Stores enum names instead of ordinal numbers.

Prefer:

PENDING
CONFIRMED
CANCELLED

over:

0
1
2

because adding/reordering enum constants can make ordinal persistence unsafe.

### GenerationType.IDENTITY

With MySQL:

@GeneratedValue(strategy = GenerationType.IDENTITY)

maps naturally to:

AUTO_INCREMENT

### @NotNull vs @Column(nullable=false)

@NotNull
→ application/request validation

@Column(nullable=false)
→ database schema constraint

Both can be useful.

### ddl-auto=update

Hibernate can attempt to synchronize entity changes with an existing schema.

Useful for development.

Do NOT rely on ddl-auto=update as the production database migration strategy.

Production normally uses migration tools such as Flyway/Liquibase.

### Experiment

Changing:

customer_name varchar(255) NULL
↓
@Column(length=50, nullable=false)
↓
customer_name varchar(50) NOT NULL

Changing:

discount decimal(38,2)
↓
precision=15, scale=2
↓
discount decimal(15,2)

Changing:

GenerationType.AUTO
↓
GenerationType.IDENTITY
↓
AUTO_INCREMENT

### Mistake / Learning

Initially did not know precision and scale.

Initially used AUTO generation strategy.

Learned to inspect actual DB schema using:

DESC orders;

and:

SHOW CREATE TABLE orders;
# STEP 6.2 — Entity Relationships

## Relationship

One Customer can have many Orders.

One Order belongs to one Customer.

Customer
1
|
N
Order

JPA:

Customer
@OneToMany
↓
Orders

Order
@ManyToOne
↓
Customer


## Foreign Key

A foreign key is a column that references the primary key of another table.

Example:

customer.id
↑
|
orders.customer_id

For Customer → Order (1:N), the foreign key is normally stored on the MANY side:

orders.customer_id


## @ManyToOne

Order has:

@ManyToOne
private Customer customer;

Meaning:

Many Order records can reference the same Customer.

Database representation:

orders.customer_id → customer.id


## @JoinColumn

@JoinColumn(name = "customer_id")

Defines the database FK column used by the relationship.

Java:

Order.customer

maps to:

Database:

orders.customer_id


## Owning Side

The owning side controls the relationship/FK mapping.

In Customer → Order:

Order is the owning side because it contains:

@ManyToOne
@JoinColumn(name = "customer_id")

The FK is stored in orders.customer_id.


## Inverse / Non-Owning Side

Customer is the inverse/non-owning side.

@OneToMany(mappedBy = "customer")

It does not independently control the FK.

It points to the relationship already defined by:

Order.customer


## mappedBy

mappedBy identifies the Java entity field that owns the relationship.

Example:

Order:

private Customer customer;

Customer:

@OneToMany(mappedBy = "customer")
private List<Order> orders;


IMPORTANT:

mappedBy uses the Java field name.

Correct:

mappedBy = "customer"

Incorrect:

mappedBy = "customer_id"


@JoinColumn(name = "customer_id") uses the database column name.


## What Problem Does mappedBy Solve?

In a bidirectional relationship both entities contain references to each other.

Customer → Orders
Order → Customer

JPA needs to know which side owns the database relationship.

mappedBy tells JPA:

"The other side owns this relationship; don't create/manage another independent relationship mapping from this side."

This avoids redundant relationship mappings and, depending on mapping type, can avoid an unnecessary join table.


## Bidirectional Relationship

Both entities can navigate to each other.

Customer → Orders
Order → Customer

Example:

Customer.orders
Order.customer


## Unidirectional Relationship

Only one entity knows about the relationship.

Order → Customer

Customer does not have:

List<Order>


## Does One-to-Many create an extra mapping table?

Normally:

@OneToMany + @ManyToOne

does NOT require a separate join table.

The FK is stored on the MANY side:

orders.customer_id


## When is a Join Table commonly required?

Many-to-Many relationships normally require a join/association table.

Example:

Student
N
|
student_course
|
N
Course

student_course:

student_id
course_id


## Important Distinction

mappedBy:
→ Java entity field/property name

@JoinColumn:
→ database FK column name


## Database Observation

After adding the relationship:

orders gained:

customer_id BIGINT

and SHOW CREATE TABLE showed:

FOREIGN KEY (customer_id)
REFERENCES customer(id)

Therefore:

Order is the owning side
and
orders.customer_id is the FK.

## Current Schema Observation

The intended Customer table should contain:

id
customer_name
customer_email

Current schema unexpectedly contains:

id
customer_email
customer_id

This should be corrected by reviewing Customer.java before manually modifying the database.

## Interview Questions

1. What is an owning side?
2. What is an inverse/non-owning side?
3. What does @JoinColumn do?
4. What does mappedBy do?
5. Is mappedBy a database column name?
6. Why is @ManyToOne usually the owning side?
7. Where is the FK stored in a 1:N relationship?
8. What problem does mappedBy solve?
9. What is a bidirectional relationship?
10. What is a unidirectional relationship?
11. Does @OneToMany always create a join table?
12. When do we commonly need a join table?
13. Difference between mappedBy and @JoinColumn?
14. Can both sides of a bidirectional relationship be owning sides?
### Important ddl-auto=update Observation

An accidental column was created because:

@Column(name = "customer_id")
private String name;

was used.

Hibernate created:

customer.customer_id

After correcting the entity mapping, the old column remained.

Important lesson:

`ddl-auto=update` is NOT a complete schema migration/reconciliation mechanism.

Removing a Java field should NOT be assumed to automatically remove the existing database column.

For production schema evolution, use migration tools such as Flyway or Liquibase.

In this learning project, accidental schema artifacts can be manually removed when appropriate.

### Debugging Lesson

Always distinguish:

orders.customer_id
→ correct FK to customer.id

customer.customer_id
→ accidental column created due to incorrect @Column mapping
## Relationship Save Experiment

Flow:

Customer
↓
customerRepository.save(customer)
↓
Customer gets persisted/managed
↓
Order.customer = savedCustomer
↓
orderRepository.save(order)
↓
orders.customer_id = customer.id


### Database Relationship

Customer:

id = 1

Order:

customer_id = 1

The complete Customer object is NOT duplicated into the Order table.

The relationship is represented through the foreign key:

orders.customer_id → customer.id


### Important

@ManyToOne + @JoinColumn creates the FK relationship.

The Order side owns the relationship.

Customer.orders is the inverse side because:

@OneToMany(mappedBy = "customer")


### Upcoming Experiment

Try saving an Order with a newly-created Customer that has NOT been persisted:

new Customer()
↓
Order.customer
↓
orderRepository.save(order)

Observe the Hibernate exception when no cascade is configured.

This demonstrates the difference between:

Transient Entity
and
Managed/Persistent Entity.
## Relationship Save Experiment — Result

Created and saved a Customer first:

Customer
id = 2

Then assigned it to an Order:

order.setCustomer(savedCustomer)

Then saved the Order.

Hibernate generated:

INSERT INTO customer ...

INSERT INTO orders
(..., customer_id, ...)
VALUES (..., ?, ...)

Database result:

customer.id = 2
orders.customer_id = 2

Therefore:

orders.customer_id → customer.id

The Customer object is not duplicated as a separate relationship table.

The relationship is represented using the foreign key.


## Object Reference → Foreign Key

Java:

order.customer
↓
Customer object
↓
id = 2

Database:

orders.customer_id = 2


## Important Observation

Order currently contains both:

customerName
customerEmail

and:

Customer customer

Therefore customer information is currently duplicated/redundant in the database.

Eventually the normalized design should keep customer information in the Customer table and use only:

orders.customer_id

in the Order table.


## Entity Identity

Two Customer objects with the same email can still represent different entities:

Customer #1 → id = 1
Customer #2 → id = 2

JPA does not automatically assume that equal business fields represent the same entity.

Entity identity is primarily based on the primary key.

If email must be unique, that business/database constraint must be explicitly implemented.


## save() vs SQL Execution

Conceptually:

save()
↓
Persistence Context / managed state
↓
flush
↓
SQL
↓
Database

Important:

Calling save() should not be mentally treated as "SQL is definitely executed immediately."

JPA/Hibernate can delay SQL until flush/transaction synchronization.

This will be studied in detail under Persistence Context + Entity Lifecycle.
## Step 6.2 — Transient Entity Experiment

### Experiment

Created a Customer:

Customer customer = new Customer();

Then assigned it to Order:

order.setCustomer(customer);

But did NOT call:

customerRepository.save(customer);

Then:

orderRepository.save(order);


### Result

Hibernate threw:

TransientPropertyValueException

Meaning:

Persistent Order references an unsaved transient Customer.


### Why?

Customer was still TRANSIENT:

Customer
id = null
not persisted
not managed

But Order was being persisted and contains:

Order.customer → transient Customer


Hibernate needs:

orders.customer_id → customer.id

But the Customer does not have a persisted database identity yet.


### Correct Flow Without Cascade

Customer
↓
customerRepository.save(customer)
↓
Customer becomes managed/persistent
↓
database generates Customer ID
↓
Order.customer = savedCustomer
↓
orderRepository.save(order)
↓
flush
↓
orders.customer_id = customer.id


### Cascade Observation

By default @ManyToOne does not cascade persist.

Therefore:

orderRepository.save(order)

does NOT automatically persist a new Customer.

Cascade can change this behavior, e.g.:

@ManyToOne(cascade = CascadeType.PERSIST)

But cascade should not be added blindly.

If Customer is an independent/master entity, automatically creating a Customer while creating an Order may be undesirable.


### Important Production Lesson

Do not blindly use:

cascade = CascadeType.ALL

on every relationship.

Cascade behavior should be chosen based on the lifecycle/ownership relationship between the entities.


### save() vs flush()

save() should not be mentally understood as:

"SQL definitely executes immediately."

Conceptually:

save()
↓
Persistence Context
↓
managed entity
↓
flush
↓
Hibernate synchronizes state with DB
↓
SQL

The transient reference exception was detected during flush/transaction completion.


### Entity State Learned

new Customer()
↓
TRANSIENT

customerRepository.save(customer)
↓
MANAGED/PERSISTENT

Managed entity can participate in the persistence context and be synchronized with the database.
# STEP 6.3 — Persistence Context + Entity Lifecycle

## Persistence Context

Persistence Context is the JPA/Hibernate context that manages and tracks entity instances.

Conceptually:

Entity
↓
Persistence Context
↓
Hibernate
↓
Database


## Entity Lifecycle

### 1. TRANSIENT

Created using:

new Customer();

Entity is not managed.

No database row exists yet.

Example:

Customer customer = new Customer();

State:

TRANSIENT


### 2. MANAGED / PERSISTENT

Entity becomes associated with the persistence context.

Example:

customerRepository.save(customer);

State:

MANAGED

Hibernate tracks changes to managed entities.


### 3. DETACHED

Entity was previously managed but is no longer associated with the persistence context.

Examples:

- persistence context closed
- transaction/session ended
- entity explicitly detached

Changes to a detached entity are not automatically tracked by Hibernate.


### 4. REMOVED

Managed entity is marked for deletion.

Example:

repository.delete(entity);

Conceptually:

MANAGED
↓
REMOVED
↓
flush
↓
DELETE SQL


## Entity Lifecycle Summary

TRANSIENT
↓
persist/save
↓
MANAGED
↓
detach/close
↓
DETACHED

MANAGED
↓
remove/delete
↓
REMOVED


## Dirty Checking

Dirty checking is Hibernate's mechanism for detecting changes made to managed entities.

Example:

@Transactional
public void update(Long id) {

    Order order = orderRepository.findById(id)
            .orElseThrow();

    order.setAmount(new BigDecimal("7777.00"));

    // save() not required for dirty checking
}

Conceptually:

Database/Snapshot
amount = 2500

Managed Entity
amount = 7777

       ↓

Dirty Checking

       ↓

UPDATE orders
SET amount = 7777
WHERE id = ...


## Important

If an entity is already managed inside the persistence context, changing its fields can be automatically synchronized with the database during flush.

Explicit repository.save() is not required merely to trigger dirty checking.


## @Transactional

Typical flow:

@Transactional
↓
Transaction begins
↓
Persistence Context active
↓
Entity fetched
↓
Entity becomes MANAGED
↓
Entity modified
↓
Method completes
↓
Flush
↓
SQL
↓
Commit


## save() vs flush()

Do not mentally equate:

save()
=
immediate SQL execution

Conceptually:

save/persist
↓
Persistence Context
↓
flush
↓
SQL
↓
Database


## First-Level Cache

Persistence Context also acts as JPA's first-level cache.

Within one persistence context, entity identity is maintained.

Example:

find(Order, 5)
find(Order, 5)

Both refer to the same managed entity identity within the same persistence context.

## Doubt : is @Transactional required for dirty checking ?
Haan bhai, automatic dirty checking ke liye @Transactional lagana bilkul zaruri hai! 🔥

Agar tum @Transactional hata doge, toh dirty checking kaam nahi karegi aur database mein koi UPDATE query fire nahi hogi. Chalo dekhte hain kyun:

1. Jab tum @Transactional use karte ho (Current Code)
   Transaction Start: Method ke start hote hi Spring ek naya transaction aur Hibernate ka Persistence Context (Session) khol deta hai.

    MANAGED State: Jab tum orderRepository.findById(id) call karte ho, entity us persistence context mein MANAGED state mein aa jati hai.

    Tracking: Jab tum order.setAmount(...) karte ho, Hibernate is change ko track kar raha hota hai.

    Flush & Commit: Method khatam hote hi transaction commit hota hai. Hibernate automatically flush karta hai, dirty checking chalata hai, aur bina save() call kiye UPDATE query fire kar deta hai.

2. Agar tum @Transactional HATA doge (What if removed)
   No Active Transaction: Tumhare testDirtyChecking method ke upar koi transaction nahi hoga.

    Tiny Transaction in Repo: Spring Data JPA ka findById() apna ek chota sa default transaction banayega sirf data fetch karne ke liye. Jaise hi findById() complete hoga, wo chota transaction aur uska persistence context close ho jayega.

    DETACHED State: Kyunki persistence context close ho gaya, jo Order object tumhe mila wo ab DETACHED state mein chala jayega. Hibernate ab usko track nahi kar raha hai.

    No Tracking, No Update: Ab jab tum order.setAmount(...) karoge, toh sirf Java memory mein object change hoga. Method khatam hone par koi transaction commit nahi ho raha aur koi flush nahi ho raha. Result: Database mein kuch update nahi hoga.

## Important Interview Questions

1. What is Persistence Context?
2. What are the JPA entity lifecycle states?
3. What is a transient entity?
4. What is a managed entity?
5. What is a detached entity?
6. What is a removed entity?
7. What is dirty checking?
8. Why can Hibernate update a managed entity without repository.save()?
9. What is flush?
10. save() vs flush()?
11. What is the first-level cache?
12. What role does @Transactional play in dirty checking?
13. What happens when a detached entity is modified?
14. What happens when a transient entity is referenced by a persistent entity?
15. What is the difference between Persistence Context and database?
# STEP 6.3 — Persistence Context + Entity Lifecycle

## Persistence Context

Persistence Context is the JPA/Hibernate context that manages and tracks entity instances.

It provides:

- Entity management
- Entity identity
- Dirty checking
- First-level cache
- Unit-of-work behavior


## Entity Lifecycle

TRANSIENT
↓
persist/save
↓
MANAGED
↓
detach / persistence context closes
↓
DETACHED

MANAGED
↓
remove/delete
↓
REMOVED


## 1. TRANSIENT

Example:

Customer customer = new Customer();

The entity is newly created and is not managed by the Persistence Context.

Usually no database row exists yet.


## 2. MANAGED / PERSISTENT

An entity associated with the active Persistence Context.

Example:

Order order = orderRepository.findById(id)
.orElseThrow();

Inside an active transaction, the returned Order is managed.

Hibernate tracks changes to managed entities.


## 3. DETACHED

An entity that was previously managed but is no longer associated with the Persistence Context.

Example:

entityManager.detach(order);

After detach:

entityManager.contains(order) == false

Changes to the detached object are not automatically tracked by Hibernate.


## 4. REMOVED

A managed entity marked for deletion.

repository.delete(entity)

Conceptually:

MANAGED
↓
REMOVED
↓
flush
↓
DELETE SQL


# Dirty Checking

Dirty checking is Hibernate's mechanism for detecting changes in managed entities.

Example:

@Transactional
public void updateOrder(Long id) {

    Order order = orderRepository.findById(id)
            .orElseThrow();

    order.setAmount(new BigDecimal("7777.00"));

    // No save() required here
}

Flow:

findById()
↓
MANAGED entity
↓
setAmount()
↓
Dirty Checking
↓
flush
↓
UPDATE SQL
↓
COMMIT


## Hands-On Experiment

Initial amount:

1000.00

Changed Java entity:

order.setAmount(7777.00)

No repository.save() was called.

Hibernate generated UPDATE SQL and database became:

7777.00

Conclusion:

A managed entity does not require save() after every field modification.


# Experiment A1 — Without @Transactional

Without a service-level @Transactional boundary:

findById()
↓
repository operation
↓
repository transaction ends
↓
entity no longer remains managed for the service operation
↓
field modification
↓
no dirty-checking UPDATE


# Experiment A2 — With @Transactional

@Transactional
↓
Transaction begins
↓
Persistence Context active
↓
findById()
↓
MANAGED entity
↓
modify entity
↓
Dirty Checking
↓
flush
↓
UPDATE
↓
COMMIT


## Important @Transactional Lesson

@Transactional is not simply a rollback annotation.

For JPA, it defines a transaction/unit-of-work boundary in which entities can remain managed and changes can be synchronized with the database during flush.


# Experiment B — Explicit Detach

EntityManager was used:

entityManager.detach(order);

Before detach:

entityManager.contains(order) == true

After detach:

entityManager.contains(order) == false

Then:

order.setAmount(...)

Result:

No automatic dirty-checking UPDATE.

Conclusion:

Managed entity:
→ tracked by Persistence Context
→ dirty checking works

Detached entity:
→ not tracked
→ dirty checking does not automatically synchronize changes


# save() vs Dirty Checking

Do not think:

save()
=
UPDATE SQL immediately

Conceptually:

Repository operation
↓
Persistence Context
↓
Managed entity
↓
Dirty Checking
↓
flush
↓
SQL
↓
Database


If an entity is already managed inside an active transaction, changing its fields is enough for Hibernate to detect the modification.


# First-Level Cache

Persistence Context also acts as JPA's first-level cache.

Entity identity is maintained within the same Persistence Context.

Same entity identity should not be treated as multiple independently managed entity instances inside one persistence context.


# Important Interview Questions

1. What is Persistence Context?
2. What are the JPA entity lifecycle states?
3. What is a transient entity?
4. What is a managed entity?
5. What is a detached entity?
6. What is a removed entity?
7. What is dirty checking?
8. Why can Hibernate update an entity without repository.save()?
9. What is flush?
10. save() vs flush()?
11. What role does @Transactional play in JPA?
12. What happens to an entity after EntityManager.detach()?
13. What does EntityManager.contains() tell us?
14. What is first-level cache?
15. What happens when a transient entity is referenced by a managed entity?
## EntityManager.merge()

merge() is used to copy the state of a detached entity onto a managed entity.

Important:

merge() does NOT make the original detached object managed.

Example:

Order order = ...;              // managed
entityManager.detach(order);    // detached

order.setAmount(5555);

Order mergedOrder =
entityManager.merge(order);


After merge:

entityManager.contains(order)
→ false

entityManager.contains(mergedOrder)
→ true


Therefore:

order        → DETACHED
mergedOrder  → MANAGED


### Important

The managed instance returned by merge() is the object that Hibernate tracks.

The original detached object remains detached.


### Practical Proof

original contains = false
merged contains   = true
same object       = false


### Mental Model

Detached entity
↓
merge()
↓
Hibernate copies entity state
↓
Managed instance returned
↓
Persistence Context tracks managed instance
↓
Dirty Checking
↓
flush
↓
UPDATE


### Important Interview Trap

Q: Does merge() reattach the same object?

A: No.

It copies the state of the supplied entity onto a managed instance and returns that managed instance. The original object remains detached.


### persist() vs merge()

persist():

TRANSIENT
↓
persist()
↓
MANAGED


merge():

DETACHED
↓
merge()
↓
MANAGED INSTANCE returned


### Practical Observation

merge() caused Hibernate to execute another SELECT for the existing entity before synchronizing the merged state.

This demonstrates that merge() is not simply a flag change on the original Java object.
## merge() — Final Hands-On Proof

Experiment result:

Before detach:
contains = true

After detach:
contains = false

After merge:
original contains = false
merged contains = true

same object = false


Therefore:

original entity
→ DETACHED

merged entity
→ MANAGED

and:

original != merged


### Proof 2 — Modifying Detached Object

After merge:

order.setAmount(6666.00);

The original detached object changed to 6666.00,
but mergedOrder remained unchanged.

Reason:

order
→ DETACHED
→ not tracked by Persistence Context


### Proof 3 — Modifying Managed Object

mergedOrder.setAmount(1111.00);

Hibernate generated UPDATE SQL.

Reason:

mergedOrder
→ MANAGED
→ tracked by Persistence Context
→ Dirty Checking
→ flush
→ UPDATE


### Final Mental Model

Detached Entity
↓
merge()
↓
Managed Instance returned
↓
Persistence Context tracks managed instance
↓
modify managed instance
↓
Dirty Checking
↓
flush
↓
UPDATE


Important:

merge() does NOT reattach the original object.

It returns a managed instance containing the state of the supplied entity.
# STEP 6.4 — Cascade Types

## What is Cascade?

Cascade controls whether a JPA persistence operation performed on one entity is propagated to its associated entity.

Example:

Order
↓
Customer

With:

@ManyToOne(cascade = CascadeType.PERSIST)

persisting Order can also persist its Customer.


## Cascade vs Foreign Key

Foreign Key:
orders.customer_id → customer.id

Purpose:
Database relationship and referential integrity.

Cascade:
JPA/Hibernate operation propagation.

Purpose:
Controls ORM persistence operations.

They are different concepts.


## Cascade Types

PERSIST
MERGE
REMOVE
REFRESH
DETACH
ALL


## CascadeType.PERSIST

Propagates persist operation.

Order
↓ persist
Customer

Without cascade:

Order → transient Customer
↓
TransientPropertyValueException

With CascadeType.PERSIST:

Order.persist()
↓
Customer.persist()
↓
Customer INSERT
↓
Order INSERT


## CascadeType.MERGE

Propagates merge operation.

Detached Order
↓
merge()
↓
managed Order

With MERGE cascade:
associated Customer state can also be merged.


## CascadeType.REMOVE

Propagates remove/delete operation.

Parent delete
↓
associated entity delete

Use carefully.

Putting REMOVE on @ManyToOne can be dangerous when the associated entity is independently shared/referenced.


## CascadeType.ALL

Equivalent to cascading all JPA cascade operations:

PERSIST
MERGE
REMOVE
REFRESH
DETACH


Important:

CascadeType.ALL does NOT mean database ON DELETE CASCADE.

JPA cascade and database cascade are different layers.


## Cascade Direction

Cascade is directional.

If cascade is configured on:

Customer.orders

it does not automatically mean operations on Order will cascade to Customer.

Cascade configuration is independent of owning side.


## Owning Side ≠ Cascade Side

Owning side determines which side controls the relationship/FK mapping.

Cascade determines which persistence operations propagate.

These are separate concepts.


## Lifecycle Design Rule

Ask:

"If I delete/save/merge A, should B automatically be affected?"

Cascade should be chosen according to the lifecycle relationship between entities.

Do not blindly use:

cascade = CascadeType.ALL


## Upcoming

CascadeType.PERSIST experiment
CascadeType.MERGE experiment
CascadeType.REMOVE experiment
orphanRemoval experiment
## CascadeType.PERSIST — Hands-On Result

Mapping:

@ManyToOne(cascade = CascadeType.PERSIST)
@JoinColumn(name = "customer_id")
private Customer customer;


Experiment:

Created a new Customer:

Customer customer = new Customer();

Created an Order:

Order order = new Order();
order.setCustomer(customer);

Then:

orderRepository.save(order);

Customer was NOT explicitly saved.


### Result

Hibernate automatically executed:

INSERT INTO customer ...

Then:

INSERT INTO orders (..., customer_id, ...)


Database:

customer.id = 3

orders.customer_id = 3


### Conclusion

CascadeType.PERSIST propagated the persist operation:

Order
↓
PERSIST
↓
Customer


Without CascadeType.PERSIST:

Order
↓
transient Customer
↓
TransientPropertyValueException


With CascadeType.PERSIST:

Order
↓
cascade persist
↓
Customer INSERT
↓
Customer ID generated
↓
Order INSERT
↓
orders.customer_id = Customer.id


### Important

Cascade does NOT create the foreign key column.

@JoinColumn / relationship mapping creates the FK mapping.

Cascade controls JPA operation propagation.


### Cascade vs mappedBy

mappedBy:
→ relationship ownership / mapping

cascade:
→ persistence operation propagation


### Cascade Direction

Cascade is directional.

Cascade configured on:

Order.customer

means:

Order operation
↓
Customer operation

It does NOT automatically imply:

Customer operation
↓
Order operation.
## CascadeType.MERGE — Hands-On

Mapping:

@ManyToOne(cascade = CascadeType.MERGE)
@JoinColumn(name = "customer_id")
private Customer customer;


Concept:

CascadeType.MERGE propagates the JPA merge operation from one entity to its associated entity.


Experiment:

Order       → DETACHED
Customer    → DETACHED

Modified:

Order.amount = 6000
Customer.name = "Merged Customer"
Customer.email = "merged@gmail.com"


Then:

Order mergedOrder = entityManager.merge(order);


With CascadeType.MERGE:

Detached Order
↓
merge(Order)
↓
Managed Order
↓
cascade MERGE
↓
Customer state also merged


Without CascadeType.MERGE:

merge(Order)
↓
Order merge operation
↓
Customer merge is NOT cascaded


Important:

CascadeType.MERGE does not mean "always generate UPDATE SQL."

It means the MERGE operation is propagated.

Actual SQL depends on whether the resulting managed entity state is dirty.


### PERSIST vs MERGE

PERSIST:

TRANSIENT
↓
persist()
↓
MANAGED
↓
INSERT


MERGE:

DETACHED
↓
merge()
↓
MANAGED INSTANCE
↓
Dirty Checking
↓
UPDATE if required


### Important Concept

Cascade controls:

"Does this JPA operation propagate?"

Dirty Checking controls:

"Does Hibernate need to synchronize changed state with SQL?"
## CascadeType.MERGE — Hands-On Result

Mapping:

@ManyToOne(cascade = CascadeType.MERGE)
@JoinColumn(name = "customer_id")
private Customer customer;


Experiment:

Initially:

Order managed = true
Customer managed = true


After:

entityManager.detach(order);
entityManager.detach(customer);


Result:

Order after detach = false
Customer after detach = false


Both entities became DETACHED.


Then modified both:

order.setAmount(6000.00);

customer.setName("Merged Customer");
customer.setEmail("merged@gmail.com");


Then:

Order mergedOrder = entityManager.merge(order);


Because CascadeType.MERGE was configured:

merge(Order)
↓
Cascade MERGE
↓
merge(Customer)


Observed:

Merged Customer managed = true

Merged Customer name = Merged Customer

Original Customer == Merged Customer
→ false


Therefore:

Original Customer
→ DETACHED

mergedOrder.getCustomer()
→ MANAGED


Hibernate generated UPDATE statements for both Customer and Order.


### Important

CascadeType.MERGE means the MERGE operation propagates to the associated entity.

It does NOT mean:

"always execute UPDATE."

The propagated merge creates/obtains managed state, and dirty checking determines whether SQL synchronization is required.


### Strong Interview Point

Q: If Order has CascadeType.MERGE on Customer, what happens when merge(Order) is called?

A:

The merge operation is cascaded to the associated Customer. The state of the detached Customer is copied into a managed Customer instance.

The original detached Customer object itself does not become managed.
# STEP 6.4 — Cascade REMOVE vs orphanRemoval

## CascadeType.REMOVE

CascadeType.REMOVE propagates a REMOVE operation from an entity to its associated entities.

Example:

Customer
|
+── Order 1
+── Order 2
+── Order 3

Delete Customer
↓
Cascade REMOVE
↓
Delete Orders
↓
Delete Customer


Main idea:

Parent DELETE
↓
Child DELETE


## orphanRemoval

orphanRemoval=true is used when a child entity should be deleted when it is removed from the parent's relationship.

Example:

Customer
|
+── Order 1
+── Order 2

customer.getOrders().remove(order1);

With:

orphanRemoval = true

Order 1 becomes an orphan
↓
Hibernate can DELETE Order 1


Main idea:

Remove child from relationship
↓
Child becomes orphan
↓
DELETE child


## Cascade REMOVE vs orphanRemoval

CascadeType.REMOVE:

Parent is removed
↓
REMOVE operation cascades to children


orphanRemoval:

Child is removed from parent's relationship
↓
Child can be deleted


They are different concepts.


## Important

orphanRemoval != CascadeType.REMOVE

They can be configured independently.


## Typical use case

Order → OrderItem

Order
|
+── Item A
+── Item B
+── Item C

If Item B is removed from Order:

Order
|
+── Item A
+── Item C

With orphanRemoval=true:

Item B row can be deleted.

This is useful when the child lifecycle is owned by the parent.


## Lifecycle / Domain Design

Do NOT blindly use:

cascade = CascadeType.ALL
or
orphanRemoval = true

Ask:

"If the parent is deleted, should the children also be deleted?"

"If the child is removed from the relationship, should its database row disappear?"

Cascade and orphanRemoval should reflect business/entity lifecycle.


## Bidirectional Relationship

Customer:

@OneToMany(mappedBy = "customer")
List<Order> orders;


Order:

@ManyToOne
@JoinColumn(name = "customer_id")
Customer customer;


Order is the owning side because it contains the FK:

orders.customer_id


mappedBy:
→ identifies the inverse/non-owning side

@JoinColumn:
→ maps the FK column


Cascade:
→ operation propagation

orphanRemoval:
→ lifecycle of child removed from relationship


## Helper Methods

For bidirectional relationships:

public void addOrder(Order order) {
orders.add(order);
order.setCustomer(this);
}

public void removeOrder(Order order) {
orders.remove(order);
order.setCustomer(null);
}

This keeps both sides of the Java relationship synchronized.
# STEP 6.4 — Cascade Types

## Cascade

Cascade controls whether a JPA persistence operation on one entity
is propagated to its associated entity.

Important:

Cascade is an ORM/JPA concept.

Foreign Key is a database concept.

Cascade != Foreign Key.


## Cascade Types

PERSIST
MERGE
REMOVE
REFRESH
DETACH
ALL


## CascadeType.PERSIST

Propagates persist operation.

Order
↓ persist
Customer

With:

@ManyToOne(cascade = CascadeType.PERSIST)

saving/persisting Order can also persist the Customer.


Hands-on result:

Order was saved without explicitly saving Customer.

Hibernate generated:

INSERT INTO customer ...

INSERT INTO orders ...

The generated Customer ID was used as:

orders.customer_id


Important:

CascadeType.PERSIST did NOT create customer_id.

@JoinColumn / relationship mapping defines the FK.

Cascade only propagated the persist operation.


## CascadeType.MERGE

Propagates merge operation.

Detached Order
↓
merge(Order)
↓
Cascade MERGE
↓
Customer state also merged


Hands-on result:

Order → DETACHED
Customer → DETACHED

After:

Order mergedOrder = entityManager.merge(order);

Observed:

Original Customer → DETACHED
Merged Customer → MANAGED

Original Customer == Merged Customer
→ false

Hibernate generated UPDATE statements for the dirty managed state.


Important:

merge() does not make the original detached object managed.

CascadeType.MERGE causes the merge operation to propagate.


## CascadeType.REMOVE

Propagates REMOVE operation.

Hands-on result:

Customer #10
|
+── Order 101
+── Order 102
+── Order 103

customerRepository.delete(customer);

Hibernate generated DELETE statements for:

Order 101
Order 102
Order 103
Customer #10


Database verification:

Customer #10 → deleted
Orders 101, 102, 103 → deleted


Main idea:

DELETE PARENT
↓
Cascade REMOVE
↓
DELETE CHILDREN


## orphanRemoval

orphanRemoval=true ties the child's lifecycle to its relationship with the parent.

Hands-on result:

Customer #11
|
+── Order 201
+── Order 202
+── Order 203

Removed Order 202 from Customer.orders.

Customer was NOT deleted.

Hibernate generated:

DELETE FROM orders
WHERE id = ?


Database:

Customer #11 → remains
Order 201 → remains
Order 202 → deleted
Order 203 → remains


Main idea:

Remove child from parent's relationship
↓
Child becomes orphan
↓
DELETE child


## CascadeType.REMOVE vs orphanRemoval

CascadeType.REMOVE:

Parent/entity REMOVE
↓
REMOVE operation propagated


orphanRemoval:

Child removed from parent's relationship
↓
Child can be deleted


They are different concepts.


## Important Distinction

mappedBy
↓
relationship ownership / inverse side

@JoinColumn
↓
FK mapping

cascade
↓
operation propagation

orphanRemoval
↓
child lifecycle when relationship is removed


## Cascade Direction

Cascade is directional.

If:

Customer.orders
cascade = PERSIST

then:

persist(Customer)
↓
persist Orders

It does NOT automatically mean:

persist(Order)
↓
persist Customer


## Owning Side != Cascade Side

Owning side determines who controls the relationship/FK.

Cascade determines which JPA operations propagate.

These are independent concepts.


## Production Design Rule

Do not blindly use:

cascade = CascadeType.ALL
orphanRemoval = true

Ask:

1. If parent is deleted, should child also be deleted?
2. If child is removed from parent relationship, should DB row disappear?
3. Is child lifecycle actually owned by parent?
4. Can child exist independently?

Cascade/orphanRemoval should reflect business lifecycle.


## Typical Strong Parent-Child Example

Order
|
+── OrderItem
+── OrderItem

Possible mapping:

@OneToMany(
mappedBy = "order",
cascade = CascadeType.ALL,
orphanRemoval = true
)

Because OrderItem often belongs exclusively to Order.

## Significane of order.setCustomer(null)
Tumne apne code ke comment mein aadhi baat ekdum sahi likhi hai: *"ye bs java me h, lekin tbhi usko maintain krna recommended h"*.

Lekin JPA aur Hibernate ke context mein, `order.setCustomer(null)` ka significance sirf Java memory tak limited nahi hai, iske peeche kuch bohot important reasons hain. Chalo isko detail mein samajhte hain:

### 1. In-Memory Consistency (Persistence Context)

Hibernate hamesha objects ko apne **L1 Cache (Persistence Context)** mein rakhta hai jab tak transaction chal raha hota hai.

* Agar tum sirf `customer.getOrders().remove(order)` likhte ho, toh Customer ki list se order hat gaya.
* Lekin `order` object ke andar abhi bhi `customer` ka purana reference pada hua hai!
* Agar same transaction mein aage chalkar tum `order.getCustomer()` print karoge, toh wo wahi purana customer return karega, jo ki logically galat (inconsistent) state hai. Dono side sync mein honi chahiye.

### 2. The "Owning Side" Rule (JPA ka sabse bada rule)

Bidirectional relationship mein hamesha ek **"Owning Side"** aur ek **"Inverse Side"** hota hai.

* Jiske paas foreign key hota hai (yani `Order` class jisme `customer_id` hai), wo **Owning Side** hota hai.
* `Customer` class jisme `mappedBy="customer"` likha hota hai, wo **Inverse Side** hota hai.
* **Hibernate database mein changes (UPDATE queries) track karne ke liye hamesha Owning Side (`Order`) ko dekhta hai.**

Tumhare case mein, kyunki tumne `orphanRemoval = true` lagaya hai, toh Hibernate list se hatane par usko seedha `DELETE` kar dega. Lekin maan lo kal ko tum `orphanRemoval` hata dete ho aur sirf relation todna chahte ho (ki order rahe par kisi customer se linked na ho). Us case mein, bina `order.setCustomer(null)` ke database mein `customer_id` kabhi `NULL` update hi nahi hoga!

### 3. Best Practice: Helper Methods

Kyunki dono sides ko sync rakhna itna zaroori hai, Hibernate experts hamesha recommend karte hain ki controller ya service layer mein ye kaam manually mat karo. Iske bajaye, `Customer` entity ke andar hi **Helper Methods** bana lo:

```java
// Customer.java entity ke andar
public void removeOrder(Order order) {
    this.orders.remove(order); // inverse side update
    order.setCustomer(null);   // owning side update
}

```

Fir apni service mein tum sirf `customer.removeOrder(order);` call kar sakte ho, jisse error ke chances khatam ho jaate hain.

---
# STEP 6.5 — FetchType LAZY vs EAGER

## What is FetchType?

FetchType controls when associated entity/collection data is fetched.

Two strategies:

LAZY
EAGER


## LAZY

Associated data is not required to be loaded immediately.

It can be loaded when accessed.

Mental model:

Order
↓
Customer reference
↓
not necessarily initialized

order.getCustomer().getName()
↓
Customer data may be fetched now


Important:

LAZY does NOT mean:
"data will never be fetched."

It means:
"data is not required to be fetched immediately."


## EAGER

Associated entity is required to be eagerly available when the owning entity is loaded.

Important:

EAGER does NOT mean:
"Hibernate must use JOIN."

The exact SQL strategy is provider/query dependent.


## JPA Default Fetch Types

@ManyToOne → EAGER
@OneToOne  → EAGER

@OneToMany → LAZY
@ManyToMany → LAZY


## Current Project

Order → Customer

@ManyToOne
@JoinColumn(name = "customer_id")

Default:
EAGER


Customer → Orders

@OneToMany(mappedBy = "customer")

Default:
LAZY


## Important Difference

LAZY:

Entity loaded
↓
association may remain uninitialized
↓
access association
↓
SQL may execute


EAGER:

Entity loaded
↓
association required to be eagerly available


## Hibernate Proxy / Lazy Loading

Hibernate can use a lazy-loading mechanism/proxy/reference for an association.

Conceptually:

Order
↓
Customer proxy/reference
↓
actual Customer data loaded when required


## Persistence Context Connection

Lazy loading generally requires an active Persistence Context/session.

If the Persistence Context is closed before a lazy association is accessed:

**LazyInitializationException** can occur.


## @Transactional

@Transactional keeps the transaction/Persistence Context active during the service operation.

Therefore lazy associations can generally be initialized inside the transaction.


## Important

LAZY ≠ always better
EAGER ≠ always bad

Fetch strategy should depend on the use case and required data.


## EAGER does NOT solve N+1

EAGER does not guarantee one JOIN query.

EAGER associations can still result in inefficient SQL/query patterns.

N+1 is a query design/fetching problem, not simply a LAZY problem.


## Upcoming

1. LAZY vs EAGER SQL experiment
2. Lazy initialization
3. LazyInitializationException
4. N+1 problem
5. JOIN FETCH
6. EntityGraph
7. DTO projection
   JPA Experiments
   │
   ├── FetchType LAZY vs EAGER
   ├── N+1 Problem
   ├── JOIN FETCH
   ├── EntityGraph
   ├── DTO Projection
   └── ...
### Experiment: N+1 Problem

Dataset:
- 5 customers
- 10 orders
- Customer 1 → 4 orders
- Customer 2 → 3 orders
- Customer 3 → 1 order
- Customer 4 → 1 order
- Customer 5 → 1 order
#### Experiment A
Customer.orders = EAGER
Order.customer = LAZY

Result:
1 query for orders
5 additional customer queries
Total = 6 queries
#### Experiment B
Customer.orders = LAZY
Order.customer = LAZY

Result:
1 query for orders
5 additional customer queries
Total = 6 queries
### Conclusion

LAZY/EAGER does not inherently solve the N+1 problem.

N+1 occurs when:
1 query fetches the parent entities
+
additional queries are triggered while accessing associations.

Persistence Context prevents duplicate queries for the same entity
within the same persistence context, but it does not eliminate the
N+1 pattern.
# STEP 6.6 — Solving N+1

## Problem

Naive code:

List<Order> orders = orderRepository.findAll();

for (Order order : orders) {
order.getCustomer().getName();
}

Can cause:

1 query → Orders

N additional queries → Customers

Total:
1 + N


# Solution 1 — JOIN FETCH

@Query("""
select o
from Order o
join fetch o.customer
""")
List<Order> findAllWithCustomer();


JOIN FETCH:

- JPQL/HQL feature
- Explicitly fetches association
- `join fetch` normally means INNER JOIN
- Can control query conditions and joins explicitly

Concept:

SELECT Orders + Customers
in one fetch query.


Important:

JOIN FETCH ≠ normal JOIN

JOIN:
Used for query/join logic.

JOIN FETCH:
Used to join AND fetch the association into the entity graph.


# Solution 2 — LEFT JOIN FETCH

@Query("""
select o
from Order o
left join fetch o.customer
""")
List<Order> findAllWithCustomer();


JOIN FETCH:

INNER JOIN
↓
Orders without matching Customer excluded


LEFT JOIN FETCH:

LEFT OUTER JOIN
↓
Orders without Customer can remain.


Example:

Order 1 → Customer 1
Order 2 → Customer 2
Order 3 → NULL


JOIN FETCH:

Order 1
Order 2


LEFT JOIN FETCH:

Order 1
Order 2
Order 3


Important:

The current project's `orders.customer_id` can be NULL,
so INNER JOIN FETCH and LEFT JOIN FETCH can return different
numbers of Orders.


# Solution 3 — EntityGraph

@EntityGraph(attributePaths = "customer")
List<Order> findAll();


EntityGraph tells Spring Data JPA which associations should be
included in the fetch plan for this repository method.


Important:

EntityGraph does NOT mean:

"always generate LEFT JOIN."


It means:

"fetch this association as part of this query's fetch plan."


Hibernate decides the SQL implementation.

In the hands-on experiment Hibernate generated:

from orders
left join customer
on customer.id = orders.customer_id


No N+1 Customer queries occurred.


# EntityGraph Does NOT Globally Change Mapping

If entity has:

@ManyToOne(fetch = FetchType.LAZY)
Customer customer;


and repository method has:

@EntityGraph(attributePaths = "customer")
List<Order> findAll();


The Customer relationship is not globally changed to EAGER.

The EntityGraph applies to that repository query/use case.


# JOIN FETCH vs EntityGraph

JOIN FETCH:

- Explicit JPQL/HQL query
- Fetch strategy written inside query
- More control over query joins/filtering
- Useful for custom query logic


EntityGraph:

- Fetch plan
- Less query code
- Very useful with Spring Data repository methods
- Allows query-specific fetching


# Important Mental Model

JOIN FETCH
↓
Explicit query instruction


EntityGraph
↓
Fetch-plan instruction


@JoinColumn
↓
Defines FK mapping


FetchType
↓
Default association loading strategy


Cascade
↓
Operation propagation


These concepts are independent.


# Collection Fetch Join Warning

Fetching a collection:

Customer
|
+── Order 1
+── Order 2
+── Order 3


with:

left join fetch c.orders


can produce multiple SQL rows for the same Customer:

Customer | Order 1
Customer | Order 2
Customer | Order 3


Hibernate reconstructs the entity graph.

Collection fetch joins can also create issues with pagination
and duplicate root results.

DISTINCT may be required in some JPQL queries, but it is not a
universal solution.


# N+1 Solutions

N+1
↓
Identify required data
↓
JOIN FETCH
OR
EntityGraph
OR
DTO Projection


# Next Topic

DTO Projection

Goal:

Instead of loading:

Order entity
+
Customer entity

load only required fields:

orderId
amount
customerName

directly into a DTO.
# STEP 6.7 — DTO Projection

## Why DTO Projection?

Normally JPA entity-based approach:

Database
↓
Order Entity
↓
Customer Entity
↓
DTO / API Response

For read-only APIs, we may not need the complete entity/entity graph.

Example:

{
"orderId": 209,
"amount": 1000.00,
"customerName": "Customer 1"
}

In this case, loading the complete Order + Customer entities may be unnecessary.

DTO Projection:

Database
↓
Only required columns
↓
DTO / Projection

### Core Idea

"Don't load data that the use case doesn't need."

---

# 1. DTO Projection vs Normal DTO Mapping

These are different concepts.

### Normal DTO Mapping

DB
↓
Order Entity
↓
Java mapping
↓
OrderResponse DTO

The entity is loaded first and then mapped to DTO.

Example:

Order order = repository.findById(id);

return OrderResponse.builder()
.id(order.getId())
.amount(order.getAmount())
.build();


### DTO Projection

DB
↓
Only required columns
↓
DTO / Projection

The query itself determines which columns are required.

---

# 2. Constructor-based DTO Projection

DTO:

public class OrderSummaryDto {

    private Long orderId;
    private BigDecimal amount;
    private String customerName;
    private String customerEmail;

    public OrderSummaryDto(
            Long orderId,
            BigDecimal amount,
            String customerName,
            String customerEmail
    ) {
        this.orderId = orderId;
        this.amount = amount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }
}


Repository:

@Query("""
select new com.interviewlab.dto.OrderSummaryDto(
o.id,
o.amount,
c.name,
c.email
)
from Order o
join o.customer c
""")
List<OrderSummaryDto> findOrderSummaries();


## JPQL `new`

`select new ...` tells JPQL to construct the DTO using its constructor.

Conceptually:

DB row
↓
id, amount, customerName, customerEmail
↓
new OrderSummaryDto(...)
↓
OrderSummaryDto

Therefore:

- constructor parameter count must match
- parameter order must match
- parameter types must be compatible

---

# 3. SQL Generated by Constructor Projection

Our experiment generated:

select
o1_0.id,
o1_0.amount,
c1_0.customer_name,
c1_0.customer_email
from orders o1_0
join customer c1_0
on c1_0.id=o1_0.customer_id


Important observation:

We only requested:

- id
- amount
- customer_name
- customer_email

Other Order columns such as:

- created_at
- updated_at
- status
- discount

were not required for this projection.

---

# 4. `JOIN` vs `JOIN FETCH` in DTO Projection

DTO projection uses:

join o.customer c

not:

join fetch o.customer


Why?

`JOIN FETCH` is primarily about fetching an entity association into
the entity graph.

DTO projection is not trying to create:

Order Entity
+
Customer Entity

Instead:

Required DB columns
↓
DTO


Mental model:

JOIN FETCH
↓
Entity fetching


DTO Projection
↓
Selected columns
↓
DTO

Therefore `FETCH` is generally unnecessary for this DTO projection.

---

# 5. Interface-based Projection

Instead of creating a DTO class, we created an interface:

public interface OrderSummaryView {

    Long getOrderId();

    BigDecimal getAmount();

    String getCustomerName();

    String getCustomerEmail();
}


Repository:

@Query("""
select
o.id as orderId,
o.amount as amount,
c.name as customerName,
c.email as customerEmail
from Order o
join o.customer c
""")
List<OrderSummaryView> findOrderSummaryViews();


Spring Data projection mechanism exposes the query result through
the interface.

No implementation class was created manually.

---

# 6. Alias → Getter Mapping

This is a very important concept in interface projection.

JPQL alias                  Interface getter

o.id as orderId       →     getOrderId()

o.amount as amount    →     getAmount()

c.name as customerName
→     getCustomerName()

c.email as customerEmail
→     getCustomerEmail()


The aliases should match the projection property names.

Example:

o.id as orderId

matches:

Long getOrderId();

---

# 7. Constructor Projection vs Interface Projection

### Constructor Projection

DB
↓
Selected columns
↓
Constructor
↓
new OrderSummaryDto(...)
↓
Actual DTO object


Code:

select new OrderSummaryDto(...)


### Interface Projection

DB
↓
Selected columns
↓
Projection mechanism
↓
OrderSummaryView


No constructor is required.

---

# 8. Interface Projection is NOT an Entity

OrderSummaryView:

- is not an Entity
- does not represent a database table
- does not use @Entity
- is not a replacement for the Order entity
- is used to represent selected query results

It is a projection/view of query results.

---

# 9. Nested Interface Projection

We then created:

public interface CustomerView {

    String getName();

    String getEmail();
}


and:

public interface OrderView {

    Long getId();

    BigDecimal getAmount();

    CustomerView getCustomer();
}


The projection structure mirrors the entity relationship:

Order
|
+-- id
+-- amount
|
+-- customer
|
+-- name
+-- email


Expected response:

{
"id": 209,
"amount": 1000.00,
"customer": {
"name": "Customer 1",
"email": "customer1@gmail.com"
}
}

---

# 10. Why `customer` was initially NULL

Initially we were using a flat query:

select
o.id as orderId,
o.amount as amount,
c.name as customerName,
c.email as customerEmail


This matched:

OrderSummaryView
|
+-- orderId
+-- amount
+-- customerName
+-- customerEmail


But the nested projection expected:

OrderView
|
+-- id
+-- amount
+-- customer
|
+-- name
+-- email


`customerName` and `customerEmail` are not the same projection property
as `customer`.

Therefore the nested `customer` initially appeared as:

"customer": null

This helped demonstrate that the projection structure must match the
expected result structure.

---

# 11. Nested Interface Projection Experiment

For the nested projection we used Spring Data's repository method:

List<OrderView> findAllBy();


The result correctly returned:

Order
↓
CustomerView
↓
Single SQL query
↓
Nested projection


Hibernate fired one INNER JOIN query instead of one query per Customer.

Therefore our experiment did NOT produce N+1.

---

# 12. DTO Projection and N+1

DTO Projection should not simply be described as:

"DTO Projection automatically fixes N+1."

That is not always true.

The actual query/fetch design determines whether additional queries
are executed.

In our experiment:

Order + Customer data
↓
single JOIN query
↓
projection


So no additional Customer queries were required.

---

# 13. INNER JOIN vs LEFT JOIN

Our query used:

join o.customer c


This is an INNER JOIN.

Therefore Orders without a matching Customer may be excluded.

If Orders should still be returned when Customer is absent:

left join o.customer c


can be used.

Mental model:

JOIN
↓
INNER JOIN
↓
matching rows only


LEFT JOIN
↓
LEFT OUTER JOIN
↓
left-side rows retained

---

# 14. DTO Projection vs JOIN FETCH

### JOIN FETCH

DB
↓
Order Entity
+
Customer Entity
↓
Persistence Context


Use when we actually need the entities and their associations.

### DTO Projection

DB
↓
Required fields
↓
DTO / Projection


Use when we mainly need selected data for a read operation.

---

# 15. DTO Projection vs EntityGraph

### EntityGraph

@EntityGraph(attributePaths = "customer")
List<Order> findAll();


Returns:

Order Entity
+
Customer Entity


EntityGraph controls the fetch plan for the query.

### DTO Projection

Returns:

OrderSummaryDto

or:

OrderSummaryView


DTO Projection can select only the fields required by the use case.

Mental model:

EntityGraph
↓
Which associations should be fetched?


DTO Projection
↓
Which data should be returned?

---

# 16. Performance Consideration

Do NOT memorize:

"DTO Projection is always faster."

or:

"Interface Projection is always better."

Performance depends on:

- number of rows
- selected columns
- joins
- database execution plan
- network transfer
- entity hydration
- persistence-context requirements
- actual use case

Main advantage:

Avoid unnecessary entity/column loading when a read operation
requires only a subset of data.

---

# 17. When to Use DTO Projection?

Good candidates:

- Read-only APIs
- Search APIs
- Listing APIs
- Dashboards
- Reports
- Large result sets
- Public API responses
- Cases where only a few fields are required

Example:

Entity has 20 fields but API requires only 4 fields.

DTO projection can avoid loading unnecessary entity data.

---

# 18. When Entity is Better?

Use entities when we need:

- Data modification
- Dirty checking
- Entity lifecycle
- Relationships as entities
- Persistence-context behavior
- Business logic around entities
- Entity updates

---

# 19. Important Mental Model — Fetching Section

FetchType
|
+-- LAZY
+-- EAGER
↓
LazyInitializationException
↓
N+1 Problem
↓
+------+-----------+
|      |           |
↓      ↓           ↓
JOIN   EntityGraph   DTO
FETCH                 Projection
|                    |
|                    +-- Constructor Projection
|                    +-- Interface Projection
|                    +-- Nested Interface Projection
|
+-- LEFT JOIN FETCH


---

# 20. Interview Quick Revision

### What is DTO Projection?

Fetching selected data directly into a DTO/projection instead of
loading the complete entity graph.

### Why use DTO Projection?

To avoid unnecessary entity/column loading for read-only use cases.

### Constructor Projection?

JPQL:

select new com.example.OrderDto(...)

JPQL constructs the DTO using its constructor.

### Interface Projection?

Spring Data exposes selected query results through a projection
interface.

### What is alias mapping?

o.id as orderId
↓
getOrderId()


### What is Nested Projection?

A projection interface can contain another projection interface
representing an associated object.

Example:

OrderView
↓
CustomerView


### JOIN FETCH vs DTO Projection?

JOIN FETCH:

Loads entities and their associations into the entity graph.

DTO Projection:

Returns only the selected data required by the DTO/projection.

### Does DTO Projection automatically solve N+1?

No.

The query/fetch design determines whether additional queries occur.

### Does Interface Projection mean an Entity?

No.

It is a projection of query results, not a managed JPA entity.

---

# STEP 6.7 — Hands-on Status

Constructor DTO Projection       ✅
Interface Projection             ✅
Nested Interface Projection      ✅
Selected columns verified        ✅
JOIN vs JOIN FETCH understood    ✅
INNER vs LEFT JOIN understood    ✅
EntityGraph vs DTO understood    ✅
N+1 context connected            ✅
# STEP 6.8.A — Transaction Basics

## What is a Transaction?

A transaction is a unit of work containing one or more database
operations that should succeed or fail together.

Example:

Customer save
+
Order save
↓
Single Transaction

If everything succeeds:
→ COMMIT

If a rollback-triggering exception occurs:
→ ROLLBACK

@Transactional
↓
Transaction + Persistence Context
↓
Managed Entities
↓
Dirty Checking
↓
Flush
↓
Database
↓
COMMIT
## What does a transaction contain?
Transaction T1
│
├── DB connection/resource
│
├── Persistence Context
│
└── transaction synchronization
## what does current thread contains?
But ThreadLocal ko "transaction itself" mat samajhna

Interview mein ye distinction strong rakhna:

ThreadLocal
≠
Transaction

Spring uses thread-bound context/resource association.

Think:

Thread
↓
"Current transaction resources ka address"
↓
Actual resource/transaction

ThreadLocal is more like:

"Is thread ke liye current transactional resources/context kahan milenge?"

not:

"ThreadLocal hi database transaction hai."
## Transaction Internal working : 
@Transactional
↓
Spring creates/uses a proxy
↓
Method invocation reaches TransactionInterceptor
↓
TransactionInterceptor asks TransactionManager
↓
Is there an existing transaction bound to current thread?
↓
Propagation policy is evaluated
↓
REQUIRED?
├── existing → join
└── none → create

REQUIRES_NEW?
├── existing → suspend
└── create new
↓
Database transaction / connection participates
↓
Method executes
↓
Commit / rollback
↓
Suspended transaction is resumed if required


# STEP 6.8.B — Transaction Rollback Rules

## Default Spring Rollback Behavior

RuntimeException
↓
ROLLBACK by default

Checked Exception
↓
NO ROLLBACK by default


## `rollbackFor`

Used when a specific exception should trigger rollback even if Spring
does not roll it back by default.

Example:

@Transactional(rollbackFor = Exception.class)

Checked Exception
↓
ROLLBACK


## `noRollbackFor`

Used when a specific exception should NOT trigger rollback.

Example:

@Transactional(noRollbackFor = NoRollbackException.class)

RuntimeException
↓
NO ROLLBACK


## Experiments

B1 — RuntimeException
→ ROLLBACK ✅

B2 — Checked Exception
→ COMMIT by default ✅

B3.1 — rollbackFor
→ Checked Exception caused ROLLBACK ✅

B3.2 — noRollbackFor
→ Specified RuntimeException did NOT cause ROLLBACK ✅


## Important Interview Point

"Exception occurred" does NOT automatically mean "transaction
rolled back".

Rollback behavior depends on Spring's rollback rules and the
exception type/configuration.

## Propagation :

## Propagation types :
REQUIRED
→ join existing or create new

REQUIRES_NEW
→ suspend existing → create independent transaction → resume existing

SUPPORTS
→ join if present, otherwise no transaction

MANDATORY
→ transaction must already exist

NOT_SUPPORTED
→ suspend existing and execute without transaction

NEVER
→ transaction must NOT exist
# STEP 6.8.C — Transaction Propagation

Transaction Propagation defines how a transactional method behaves
when it is called while another transaction already exists.

---

## 1. REQUIRED ⭐⭐⭐

Default propagation.

Rule:

Existing Transaction?
YES → Join existing transaction
NO  → Create new transaction

Internal Flow:

Method Call
↓
TransactionInterceptor
↓
Existing Transaction?
├── YES → Join existing TX
└── NO  → Create new TX

Example:

Transaction A
↓
Service A
↓
Service B (REQUIRED)
↓
Same Transaction A

If Transaction A rolls back, work done by both services rolls back.

Experiment:
C1 passed.

---

## 2. REQUIRES_NEW ⭐⭐⭐

Always execute using an independent transaction.

If an existing transaction exists:

```declarative
Existing TX A
↓
SUSPEND A
↓
Create TX B
↓
Execute method
↓
Commit/Rollback B
↓
RESUME A

Important:

REQUIRES_NEW ≠ same transaction.

It creates an independent transaction.

Experiment:

Outer Transaction A
↓
Customer INSERT
↓
Suspend A
↓
Inner Transaction B
↓
Order INSERT
↓
COMMIT B
↓
Resume A
↓
RuntimeException
↓
ROLLBACK A
```
Result:

Outer Customer → ROLLBACK
Inner Order    → COMMIT

C2 passed.

Important observation:

Same thread does NOT necessarily mean same transaction.

Transaction context/resources can change while the same thread
continues execution.

---

## 3. SUPPORTS ⭐

Rule:

Existing Transaction?
YES → Join it
NO  → Execute without transaction

Internal Flow:

Method Call
↓
Existing TX?
├── YES → JOIN
└── NO  → NO TRANSACTION

Mental shortcut:

"Transaction hai to support karo, nahi hai to bhi chalo."

C3 passed.

---

## 4. MANDATORY

Requires an existing transaction.

Existing TX?
YES → Join
NO  → Exception

Flow:

Call
↓
Existing TX?
├── YES → Continue
└── NO  → Exception

---

## 5. NOT_SUPPORTED

The method must execute without a transaction.

If a transaction already exists:

Transaction A
↓
SUSPEND A
↓
Execute method WITHOUT transaction
↓
RESUME A

If no transaction exists:

Execute without transaction.

---

## 6. NEVER

The method must execute without a transaction.

Existing TX?
YES → Exception
NO  → Execute

---

## 7. NESTED

Uses nested execution inside the existing transaction,
typically using a database savepoint when supported.


Flow:
text
```
Transaction A
↓
NESTED method
↓
Create Savepoint
↓
Nested work
↓
Failure
↓
Rollback to Savepoint
```


Important difference:

REQUIRES_NEW
→ separate independent transaction

NESTED
→ same transaction + savepoint

Actual NESTED behavior depends on transaction manager/database support.

---

# Interview Comparison

| Propagation | Existing TX | No Existing TX |
|---|---|---|
| REQUIRED | Join | Create new |
| REQUIRES_NEW | Suspend + new TX | Create new |
| SUPPORTS | Join | No TX |
| MANDATORY | Join | Exception |
| NOT_SUPPORTED | Suspend + no TX | No TX |
| NEVER | Exception | No TX |
| NESTED | Savepoint/nested execution | Manager-dependent |

---

# Key Interview Points

1. REQUIRED is the default propagation.
2. REQUIRED joins an existing transaction or creates one.
3. REQUIRES_NEW suspends the existing transaction and creates an
   independent transaction.
4. SUPPORTS never creates a transaction by itself.
5. MANDATORY requires an existing transaction.
6. NOT_SUPPORTED suspends the existing transaction and executes
   without one.
7. NEVER rejects execution if a transaction already exists.
8. NESTED is based on nested execution/savepoints, not an entirely
   independent transaction.
9. Same thread does not mean same transaction.
10. Transaction propagation is handled by Spring's transaction
    infrastructure/interceptor around the proxied method.

---

# Experiments Completed

C1 — REQUIRED
→ Same transaction verified ✅

C2 — REQUIRES_NEW
→ Inner transaction committed while outer transaction rolled back ✅

C3 — SUPPORTS
→ Existing TX joined; without TX remained non-transactional ✅
# STEP 6.8.D — Spring AOP Proxy

Spring uses AOP proxies around beans when cross-cutting behavior
such as `@Transactional` needs to be applied.

## Internal Working

Caller
↓
Spring AOP Proxy
↓
TransactionInterceptor
↓
Start / Join Transaction
↓
Target Method
↓
Success → Commit
Failure → Rollback according to rollback rules

Therefore `@Transactional` is not simply a method-level switch.
Spring's proxy/interceptor infrastructure applies the transactional
behavior around the method invocation.

---

## AOP Proxy

A Spring-managed service can be wrapped by a proxy.

Conceptually:

Controller
↓
Proxy
↓
Actual Service
↓
Method

The proxy intercepts the method call and can execute additional
behavior before/after the target method.

Examples of cross-cutting concerns:

- Transactions
- Caching
- Security
- Logging
- Other AOP advice

---

## Self-Invocation Problem ⭐⭐⭐

Example:

public void outerMethod() {
this.innerMethod();
}

@Transactional
public void innerMethod() {
...
}

Flow:

Proxy
↓
outerMethod()
↓
this.innerMethod()
↓
Target object directly

The second call bypasses the Spring proxy.

Therefore the `@Transactional` advice on `innerMethod()` is not
applied through the proxy.

Important:

Method execution → YES
AOP interception → NO

---

## Cross-Bean Invocation

If another Spring bean calls the transactional method:

Bean A
↓
Bean B Proxy
↓
TransactionInterceptor
↓
@Transactional method

Then Spring's transactional advice can be applied.

---

## Key Interview Point

`@Transactional` works through Spring's proxy-based AOP mechanism.

Self-invocation can bypass the proxy, so annotations such as
`@Transactional` may not be applied to the internally invoked method.

---

## Experiments Completed

D1 — Spring AOP proxy existence verified ✅

D2 — Self-invocation bypasses proxy verified ✅

D3 — Cross-bean invocation passes through proxy verified ✅
# STEP 6.8.E — Transaction Isolation Levels

Isolation determines how concurrent transactions see each other's
changes.

Main problems:

1. Dirty Read
2. Non-repeatable Read
3. Phantom Read


## Dirty Read

Transaction A changes data but has not committed.

Transaction B reads that uncommitted value.

A later rolls back.

Therefore B saw data that was never committed.

Isolation levels READ_COMMITTED and stronger prevent dirty reads.


## Non-repeatable Read

Transaction A reads the same row:

First read  → amount = 1000

Transaction B updates and commits:

amount = 5000

Transaction A reads again:

Second read → amount = 5000

Same row, different value within the transaction.


## Phantom Read

Transaction A executes:

SELECT * FROM orders WHERE amount > 1000;

Suppose it gets 5 rows.

Transaction B inserts another matching row and commits.

Transaction A executes the same query again and gets 6 rows.

The newly appearing matching row is a phantom.


## Isolation Levels

| Isolation | Dirty Read | Non-repeatable Read | Phantom Read |
|---|---|---|---|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | No | Possible | Possible |
| REPEATABLE_READ | No | No | DB-dependent |
| SERIALIZABLE | No | No | No |

MySQL/InnoDB commonly uses REPEATABLE READ as its default isolation
level.

Exact behavior can depend on the database and type of read.


## Internal Working

Transaction
↓
Isolation Level
↓
Database concurrency control
↓
MVCC / Locks
↓
Determines visibility of concurrent changes


Spring example:

@Transactional(isolation = Isolation.READ_COMMITTED)


# STEP 6.8.F — Optimistic vs Pessimistic Locking

## Lost Update

Two transactions read the same old value.

A → reads 12000
B → reads 12000

A → updates to 15000
B → updates to 18000

Without concurrency protection, B can overwrite A's update.

This is the Lost Update problem.


## Optimistic Locking

Assumes conflicts are relatively uncommon.

JPA:

@Version
private Long version;


Database:

id | amount | version
227|12000   | 0


A and B both read version 0.

A updates:

UPDATE ...
SET amount=15000, version=1
WHERE id=227 AND version=0;

Success.

B tries using version=0:

UPDATE ...
SET amount=18000, version=1
WHERE id=227 AND version=0;

No row matches because version is now 1.

Therefore JPA detects the conflict and throws an optimistic locking
exception.

The second update does not silently overwrite the first update.


## Optimistic Internal Flow

Read entity
↓
Read version
↓
Modify entity
↓
UPDATE ... WHERE id=? AND version=?
↓
Version matches?
├── YES → Update + increment version
└── NO  → Optimistic locking failure


## Pessimistic Locking

Assumes concurrent conflict is possible and locks the database row
before modification.

Conceptually:

SELECT ...
FROM orders
WHERE id=227
FOR UPDATE;


Flow:

Transaction A
↓
Acquire row lock
↓
Modify row
↓
Transaction B tries same row
↓
B waits
↓
A commits/rolls back
↓
B can continue according to DB locking behavior.


JPA example:

@Lock(LockModeType.PESSIMISTIC_WRITE)


## Optimistic vs Pessimistic

| | Optimistic | Pessimistic |
|---|---|---|
| Immediate DB lock | No | Yes |
| Main mechanism | @Version | DB row lock |
| Conflict handling | Detect later | Prevent/block concurrent access |
| Typical use | Lower contention | Higher contention |
| JPA example | @Version | @Lock(PESSIMISTIC_WRITE) |


## Important Interview Difference

Isolation answers:

"What can concurrent transactions SEE?"

Locking answers:

"What happens when concurrent transactions try to
modify/access the same data?"


## Experiments

E — Isolation concepts / READ_COMMITTED visibility verified

F1 — @Version column + automatic version increment verified

F2 — Concurrent update / lost update protection verified
# STEP 6.8.E — Transaction Isolation Levels

Isolation determines how concurrent transactions see each other's
changes.

## Important Read Phenomena

### 1. Dirty Read

Transaction A modifies data but does not commit.

Transaction B reads that uncommitted value.

If A rolls back, B had read a value that was never committed.

Example:

A:
UPDATE amount = 7777
(no COMMIT)

B:
SELECT amount

If B sees 7777 → Dirty Read.

READ_COMMITTED and stronger isolation levels prevent dirty reads.

---

### 2. Non-repeatable Read

Transaction A reads the same row twice.

First read:
amount = 12000

Transaction B:
UPDATE amount = 8888
COMMIT

Second read by A:
amount = 8888

Same row, different value.

---

### 3. Phantom Read

Transaction A executes:

SELECT * FROM orders WHERE amount >= 1000;

Suppose it gets 10 rows.

Transaction B inserts another matching row and commits.

A executes the same query again and gets 11 rows.

The newly appearing matching row is a phantom.

---

## Non-repeatable vs Phantom

Non-repeatable Read:
→ Same row
→ Value changed

Phantom Read:
→ Same query/predicate
→ Matching row-set changed


## Standard Isolation Levels

| Isolation | Dirty Read | Non-repeatable Read | Phantom |
|---|---|---|---|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | No | Possible | Possible |
| REPEATABLE_READ | No | No | DB-dependent |
| SERIALIZABLE | No | No | No |

MySQL/InnoDB commonly uses REPEATABLE_READ by default.

Exact behavior depends on database and type of read.

---

## Internal Working

Transaction
↓
Isolation Level
↓
Database concurrency control
↓
MVCC / Locks
↓
Determines what concurrent changes
the transaction can see

Spring can request an isolation level using:

@Transactional(isolation = Isolation.READ_COMMITTED)


# STEP 6.8.F — Optimistic & Pessimistic Locking

## Lost Update

Two transactions read the same old value.

A → reads 12000
B → reads 12000

A → updates 15000
B → updates 18000

Without concurrency protection:

Final value may become 18000.

A's update was lost.

---

## Optimistic Locking

Assumes conflicts are relatively uncommon.

JPA:

@Version
private Long version;


Database:

id | amount | version
227|12000   | 0


Both transactions read version 0.

A:

UPDATE orders
SET amount=15000, version=1
WHERE id=227 AND version=0;

Success.

B:

UPDATE orders
SET amount=18000, version=1
WHERE id=227 AND version=0;

No row matches because DB version is now 1.

Hibernate detects the conflict and throws an optimistic locking
exception.

Therefore the second transaction cannot silently overwrite the
first update.

---

## Optimistic Internal Flow

Read entity
↓
Read version
↓
Modify entity
↓
UPDATE ... WHERE id=? AND version=?
↓
Version matches?
├── YES → Update + increment version
└── NO  → Optimistic locking failure


## Pessimistic Locking

Assumes concurrent modification is possible and obtains a database
lock before modification.

JPA:

@Lock(LockModeType.PESSIMISTIC_WRITE)


Conceptually:

SELECT ...
FROM orders
WHERE id=?
FOR UPDATE;


Internal Flow:

Transaction A
↓
Acquire row lock
↓
Modify
↓
Transaction B tries same row
↓
B waits
↓
A COMMIT / ROLLBACK
↓
Lock released
↓
B continues according to DB behavior


## Optimistic vs Pessimistic

| | Optimistic | Pessimistic |
|---|---|---|
| Lock immediately | No | Yes |
| Main mechanism | @Version | DB row lock |
| Conflict handling | Detect later | Block/prevent concurrent access |
| Good for | Lower contention | Higher contention |
| JPA example | @Version | @Lock(PESSIMISTIC_WRITE) |

---

## Important Interview Difference

Isolation asks:

"What can my transaction SEE?"

Locking asks:

"What happens when concurrent transactions
try to access/modify the same data?"

---

## Experiments

E1 — READ_COMMITTED prevented Dirty Read ✅

E2 — READ_COMMITTED demonstrated Non-repeatable Read ✅

E3 — Phantom Read demonstrated through changing matching row-set ✅

F1 — @Version column added and version increment verified ✅

F2 — Optimistic locking prevents lost update ✅

F3 — Pessimistic write lock / row locking verified ✅
# STEP 6.8.E — Transaction Isolation Levels

Isolation determines how concurrent transactions see each other's
changes.

## Important Read Phenomena

### 1. Dirty Read

Transaction A modifies data but does not commit.

Transaction B reads that uncommitted value.

If A rolls back, B had read a value that was never committed.

Example:

A:
UPDATE amount = 7777
(no COMMIT)

B:
SELECT amount

If B sees 7777 → Dirty Read.

READ_COMMITTED and stronger isolation levels prevent dirty reads.

---

### 2. Non-repeatable Read

Transaction A reads the same row twice.

First read:
amount = 12000

Transaction B:
UPDATE amount = 8888
COMMIT

Second read by A:
amount = 8888

Same row, different value.

---

### 3. Phantom Read

Transaction A executes:

SELECT * FROM orders WHERE amount >= 1000;

Suppose it gets 10 rows.

Transaction B inserts another matching row and commits.

A executes the same query again and gets 11 rows.

The newly appearing matching row is a phantom.

---

## Non-repeatable vs Phantom

Non-repeatable Read:
→ Same row
→ Value changed

Phantom Read:
→ Same query/predicate
→ Matching row-set changed


## Standard Isolation Levels

| Isolation | Dirty Read | Non-repeatable Read | Phantom |
|---|---|---|---|
| READ_UNCOMMITTED | Possible | Possible | Possible |
| READ_COMMITTED | No | Possible | Possible |
| REPEATABLE_READ | No | No | DB-dependent |
| SERIALIZABLE | No | No | No |

MySQL/InnoDB commonly uses REPEATABLE_READ by default.

Exact behavior depends on database and type of read.

---

## Internal Working

Transaction
↓
Isolation Level
↓
Database concurrency control
↓
MVCC / Locks
↓
Determines what concurrent changes
the transaction can see

Spring can request an isolation level using:

@Transactional(isolation = Isolation.READ_COMMITTED)


# STEP 6.8.F — Optimistic & Pessimistic Locking

## Lost Update

Two transactions read the same old value.

A → reads 12000
B → reads 12000

A → updates 15000
B → updates 18000

Without concurrency protection:

Final value may become 18000.

A's update was lost.

---

## Optimistic Locking

Assumes conflicts are relatively uncommon.

JPA:

@Version
private Long version;


Database:

id | amount | version
227|12000   | 0


Both transactions read version 0.

A:

UPDATE orders
SET amount=15000, version=1
WHERE id=227 AND version=0;

Success.

B:

UPDATE orders
SET amount=18000, version=1
WHERE id=227 AND version=0;

No row matches because DB version is now 1.

Hibernate detects the conflict and throws an optimistic locking
exception.

Therefore the second transaction cannot silently overwrite the
first update.

---

## Optimistic Internal Flow

Read entity
↓
Read version
↓
Modify entity
↓
UPDATE ... WHERE id=? AND version=?
↓
Version matches?
├── YES → Update + increment version
└── NO  → Optimistic locking failure


## Pessimistic Locking

Assumes concurrent modification is possible and obtains a database
lock before modification.

JPA:

@Lock(LockModeType.PESSIMISTIC_WRITE)


Conceptually:

SELECT ...
FROM orders
WHERE id=?
FOR UPDATE;


Internal Flow:

Transaction A
↓
Acquire row lock
↓
Modify
↓
Transaction B tries same row
↓
B waits
↓
A COMMIT / ROLLBACK
↓
Lock released
↓
B continues according to DB behavior


## Optimistic vs Pessimistic

| | Optimistic | Pessimistic |
|---|---|---|
| Lock immediately | No | Yes |
| Main mechanism | @Version | DB row lock |
| Conflict handling | Detect later | Block/prevent concurrent access |
| Good for | Lower contention | Higher contention |
| JPA example | @Version | @Lock(PESSIMISTIC_WRITE) |

---

## Important Interview Difference

Isolation asks:

"What can my transaction SEE?"

Locking asks:

"What happens when concurrent transactions
try to access/modify the same data?"

---

## Experiments

E1 — READ_COMMITTED prevented Dirty Read ✅

E2 — READ_COMMITTED demonstrated Non-repeatable Read ✅

E3 — Phantom Read demonstrated through changing matching row-set ✅

F1 — @Version column added and version increment verified ✅

F2 — Optimistic locking prevents lost update ✅

F3 — Pessimistic write lock / row locking verified ✅
# LEVEL 6 — Multiple Databases

## 6.1-A — Multiple DataSources

### Goal

Configure two databases in the same Spring Boot application:

MySQL → Order data
H2    → Audit data


### Dependency

Added H2 runtime dependency:

com.h2database:h2

MySQL dependency remains unchanged.


### Architecture

Spring Boot
↓
├── MySQL DataSource → MySQL DB
│
└── H2 DataSource → H2 DB


### DataSource

A DataSource represents/configures how the application obtains
database connections.

Flow:

Repository
↓
EntityManager
↓
EntityManagerFactory
↓
DataSource
↓
Database


### Configuration

MySQL:

spring.datasource.*

H2:

app.datasource.h2.*


@ConfigurationProperties maps external configuration properties
to the DataSource configuration.


### @Primary

When multiple beans of the same type exist, such as:

MySQL DataSource
H2 DataSource

@Primary marks one bean as the default choice when Spring has
multiple candidates and no @Qualifier is specified.


### Internal Working

application.properties
↓
@ConfigurationProperties
↓
DataSource Bean
↓
Connection Pool
↓
Database


### Important

At this stage we have only configured:

MySQL DataSource
H2 DataSource

JPA EntityManagerFactory and TransactionManager are still
separate configuration layers and will be connected next.
# 6.1-B — DataSource → EntityManagerFactory → Repository

## DataSource

DataSource is an abstraction for obtaining database connections.

Simple mental model:

DataSource
↓
database connection
↓
Database

In production, DataSource commonly works with a connection pool
such as HikariCP.


## EntityManagerFactory

EntityManagerFactory is the JPA/Hibernate factory responsible for
creating EntityManager instances and holding persistence/JPA
configuration such as entity mappings.

Mental model:

Entity classes + JPA configuration + DataSource
↓
EntityManagerFactory
↓
EntityManager


EntityManagerFactory:
→ long-lived application-level object
→ generally thread-safe

EntityManager:
→ used for persistence operations/unit of work
→ not thread-safe


## Repository

Spring Data JPA provides repository implementations dynamically.

Example:

interface OrderRepository
extends JpaRepository<Order, Long>

We do not implement save(), findById(), delete(), etc. manually.

Spring Data creates a repository proxy which internally uses
EntityManager.

Flow:

Repository Proxy
↓
Spring Data JPA
↓
EntityManager
↓
Hibernate
↓
SQL
↓
DataSource
↓
Database


## Default Single Database Setup

With one DataSource, Spring Boot auto-configuration can create:

application.properties
↓
DataSource
↓
EntityManagerFactory
↓
TransactionManager
↓
Spring Data JPA Repository
↓
Database

This worked automatically in the original project because there
was only one database/DataSource.


## Why Multiple DataSources Need Explicit Configuration

With two DataSources:

MySQL DataSource
H2 DataSource

Spring cannot blindly know which DataSource should be used by
which JPA persistence unit/repository.

Therefore we explicitly configure:

MySQL DataSource
↓
MySQL EntityManagerFactory
↓
OrderRepository

H2 DataSource
↓
H2 EntityManagerFactory
↓
AuditLogRepository


## @Qualifier

When multiple beans of the same type exist:

DataSource
├── mysqlDataSource
└── h2DataSource

@Qualifier tells Spring exactly which bean should be injected.

Example:

@Qualifier("mysqlDataSource")


## Entity Scanning

Each EntityManagerFactory is configured with the entity package
it owns.

MySQL EMF
↓
entity.mysql
↓
Order

H2 EMF
↓
entity.h2
↓
AuditLog


## Transaction Manager

Each persistence unit gets its own transaction manager:

MySQL EMF
↓
MySQL TransactionManager

H2 EMF
↓
H2 TransactionManager


## Internal Working — Interview Flow
```
Repository
↓
EntityManager
↓
EntityManagerFactory
↓
DataSource
↓
Connection Pool
↓
Database

Transaction:

@Transactional
↓
Spring AOP Proxy
↓
TransactionInterceptor
↓
Selected TransactionManager
↓
Begin Transaction
↓
Repository operations
↓
Commit / Rollback
Spring Boot default single-DB setup

DataSource
↓
EntityManagerFactory
↓
TransactionManager
↓
Spring Data JPA
↓
Repository


Multiple DB

MySQL DataSource
↓
MySQL EntityManagerFactory
↓
MySQL TransactionManager
↓
OrderRepository


H2 DataSource
↓
H2 EntityManagerFactory
↓
H2 TransactionManager
↓
AuditLogRepository
```


```
Spring Boot default single-DB setup

DataSource
    ↓
EntityManagerFactory
    ↓
TransactionManager
    ↓
Spring Data JPA
    ↓
Repository


Multiple DB

MySQL DataSource
    ↓
MySQL EntityManagerFactory
    ↓
MySQL TransactionManager
    ↓
OrderRepository


H2 DataSource
    ↓
H2 EntityManagerFactory
    ↓
H2 TransactionManager
    ↓
AuditLogRepository
```
# 6.1-B — Repository → Correct Database

Multiple database mapping:

```declarative
OrderRepository
↓
mysqlEntityManagerFactory
↓
mysqlDataSource
↓
MySQL


AuditLogRepository
↓
h2EntityManagerFactory
↓
h2DataSource
↓
H2

```

## Proof Experiment

AuditLogRepository.save()
↓
INSERT INTO audit_logs
↓
H2


OrderRepository.save()
↓
INSERT INTO orders
↓
MySQL

Therefore repositories were successfully mapped to their
respective persistence units/databases.


# 6.1-C — Multiple TransactionManagers

With two databases we have two transaction managers:

mysqlTransactionManager
h2TransactionManager


A transaction manager controls transactions for its associated
persistence unit.


MySQL:

mysqlEntityManagerFactory
↓
mysqlTransactionManager
↓
MySQL transaction


H2:

h2EntityManagerFactory
↓
h2TransactionManager
↓
H2 transaction


## @Primary

When multiple TransactionManager beans exist, @Primary can mark
one as the default candidate.

Example:

@Primary
mysqlTransactionManager


Therefore:

@Transactional

can use the primary transaction manager when no explicit manager
is specified.


## Explicit TransactionManager

@Transactional("h2TransactionManager")

explicitly selects the H2 transaction manager.


## Internal Working

```
@Transactional
↓
Spring AOP Proxy
↓
TransactionInterceptor
↓
Selected TransactionManager
↓
EntityManagerFactory
↓
DataSource
↓
Database Transaction
↓
Commit / Rollback
```


## Critical Concept

@Transactional does NOT automatically create one atomic
transaction across multiple databases.

Example:

```declarative
@Transactional
↓
mysqlTransactionManager
↓
MySQL transaction

H2 is not automatically part of the same atomic transaction.

```
Multiple independent database transactions require distributed
transaction mechanisms if true cross-database atomicity is required.


## Distributed Transaction Concept

```
Multiple databases
↓
Distributed Transaction
↓
JTA / XA
↓
Two-Phase Commit (2PC)
```
JTA/XA implementation is outside the scope of this project;
only the mechanism/concept is required for interviews.
# LEVEL 7 — Testing

## Goal

Testing ka goal manually har baar application verify karne ke
instead code ke expected behavior ko automatically verify karna hai.

Flow:

Code
↓
Test
↓
Expected vs Actual
↓
PASS / FAIL


## Testing Levels

### Unit Test

Small unit/class ko isolate karke test karte hain.

Example:

OrderService
↓
Mock OrderRepository

Usually:
→ Spring context not required
→ Database not required
→ Mockito commonly used
→ Very fast


### Integration Test

Multiple application components ko together test karte hain.

Example:

Controller
↓
Service
↓
Repository
↓
Test Database

Spring context is commonly involved.

Purpose:
→ Components correctly integrate kar rahe hain ya nahi?


## Unit vs Integration

Unit:
→ isolated
→ fast
→ dependencies mocked
→ business logic focus

Integration:
→ multiple components
→ real Spring wiring
→ test database commonly used
→ integration/wiring focus


# 7.1 — JUnit 5

JUnit is the Java testing framework used to define and execute
tests and perform assertions.

Dependency:

spring-boot-starter-test

It provides the common Spring Boot testing ecosystem including
JUnit, Mockito, Spring Test, AssertJ, etc.


## @Test

@Test tells JUnit that a method should be executed as a test.

Example:

@Test
void additionShouldWork() {
int result = 2 + 3;
assertEquals(5, result);
}


## Assertion

assertEquals(expected, actual)

Expected result is compared with actual result.

Expected == Actual
↓
PASS

Otherwise:
FAIL


## Internal Working

Test class
↓
JUnit discovers @Test methods
↓
Test method executes
↓
Assertion checks expected vs actual
↓
PASS / FAIL


# Mockito — Upcoming

Mockito will allow us to replace real dependencies with mocks
during unit testing.

Example:

OrderService
↓
Mock OrderRepository

when(orderRepository.findById(1L))
.thenReturn(Optional.of(order));


Meaning:
When the service calls findById(1L), the mock returns the
predefined result instead of accessing the real database.
# 7.2 — Mockito + OrderService Unit Testing

## Goal

Test OrderService business logic without starting Spring,
Hibernate or connecting to MySQL.

Production:

Controller
↓
OrderService
↓
OrderRepository
↓
Hibernate
↓
MySQL


Unit Test:

JUnit
↓
OrderService
↓
Mock OrderRepository
↓
No Database


## Mockito

Mockito is used to create fake/mock dependencies during unit tests.

Example:

@Mock
private OrderRepository orderRepository;

The real repository is replaced by a Mockito mock.


## @InjectMocks

@InjectMocks
private OrderServiceImp orderService;

Mockito injects the mocked dependencies into the class being tested.

Conceptually:

OrderServiceImp
↓
Mock OrderRepository


## @ExtendWith(MockitoExtension.class)

Integrates Mockito with JUnit 5 and initializes Mockito
annotations such as @Mock and @InjectMocks.

A unit test does NOT need @SpringBootTest.


## when().thenReturn()

Defines mock behavior.

when(orderRepository.findById(1L))
.thenReturn(Optional.of(order));

Meaning:

When the service calls findById(1L), the mock returns the
predefined Order instead of accessing the database.


## assertThrows()

Used to verify expected exceptions.

assertThrows(
ResourceNotFoundException.class,
() -> orderService.getOrderById(999L)
);


## verify()

Checks whether a dependency was called.

verify(orderRepository)
.findById(1L);


## when() vs verify()

when()
→ defines mock behavior

verify()
→ verifies mock interaction


## any()

any(Order.class)

Means any Order object is accepted as the argument.

Example:

when(orderRepository.save(any(Order.class)))
.thenAnswer(...);


## Internal Working
```
@Test
↓
JUnit executes test
↓
MockitoExtension initializes mocks
↓
@Mock creates mock dependency
↓
@InjectMocks injects mock into service
↓
when() configures mock behavior
↓
Service method executes
↓
Service talks to mock repository
↓
No real database
↓
Assertions / verify()
↓
PASS / FAIL
```

## Key Interview Point

Unit testing isolates the class under test.

For OrderService:
→ Service is real
→ Repository is mocked
→ Database is not required
→ Spring context is normally not required

This makes unit tests fast and focused on business logic.
# 7.3 — Mockito: Real Project Service Testing

## Core Pattern

Class under test = REAL

Dependencies = MOCK

Example:

OrderServiceImp
↓
Mock OrderRepository


## @Mock

Creates a Mockito mock of a dependency.

@Mock
OrderRepository orderRepository;

The real repository/database is not used.


## @InjectMocks

Creates/injects the mocked dependencies into the class under test.

@InjectMocks
OrderServiceImp orderServiceImp;

Conceptually:

OrderServiceImp service =
new OrderServiceImp(mockOrderRepository);


## when().thenReturn()

Defines behavior of a mock.

when(orderRepository.findById(1L))
.thenReturn(Optional.of(order));


Meaning:

When the service calls findById(1L), return the predefined
result instead of accessing the database.


## verify()

Verifies that a dependency interaction happened.

verify(orderRepository)
.findById(1L);


when()
→ defines behavior

verify()
→ verifies interaction


## ArgumentCaptor

Used when we need to inspect the actual argument passed
to a mocked dependency.

Example:

ArgumentCaptor<Order> captor =
ArgumentCaptor.forClass(Order.class);

verify(orderRepository)
.save(captor.capture());

Order savedOrder = captor.getValue();


Flow:

Service
↓
repository.save(order)
↓
ArgumentCaptor captures order
↓
captor.getValue()
↓
Verify entity fields


## any()

any(Order.class)

Allows any Order object to be used as an argument.

Example:

verify(orderRepository)
.save(any(Order.class));


## never()

Verifies that an interaction must NOT happen.

verify(orderRepository, never())
.delete(any(Order.class));


Useful for negative scenarios.

Example:

Order not found
↓
Exception
↓
delete() must NOT be called


## Common Service Tests

### Create

Request
↓
Service
↓
Order creation
↓
repository.save()
↓
ArgumentCaptor
↓
Verify saved entity


### Update

findById()
↓
Existing entity
↓
Modify entity
↓
save()
↓
ArgumentCaptor
↓
Verify updated fields


### Delete

findById()
↓
Existing entity
↓
delete()
↓
verify(delete())


Not found:

findById()
↓
Optional.empty()
↓
ResourceNotFoundException
↓
verify(delete(), never())


## Internal Working

@Test
↓
JUnit executes test
↓
MockitoExtension initializes mocks
↓
@Mock creates fake dependency
↓
@InjectMocks injects mock into service
↓
when() defines mock behavior
↓
Service executes real business logic
↓
Mock records interactions
↓
Assertions / verify / ArgumentCaptor
↓
PASS / FAIL


## Key Principle

Unit testing focuses on the class under test.

For OrderService:

REAL:
→ OrderServiceImp

MOCK:
→ OrderRepository

NOT USED:
→ MySQL
→ Hibernate
→ EntityManager
→ Spring ApplicationContext


## Mockito Interview Summary

@Mock
→ creates fake dependency

@InjectMocks
→ injects fake dependency into class under test

when()
→ defines mock behavior

verify()
→ verifies interaction

ArgumentCaptor
→ captures and inspects method arguments

never()
→ verifies interaction did not happen

# 7.3.5 — Private & Static Method Testing

## Private Methods

Private methods are implementation details of a class.

Generally, private methods should NOT be tested directly.

Instead:

Public method
↓
Private method
↓
Observable result
↓
Assertion


### Why?

If tests directly depend on private implementation, changing
the internal implementation can break tests even when public
behavior remains unchanged.


### Preferred Approach

Test private logic indirectly through the public method.

If a private method becomes complex enough to require
independent testing, consider extracting the logic into a
separate class/service and test that class independently.


## Interview Answer

"I generally don't test private methods directly. I test their
behavior through the public method that uses them. If the private
logic is complex enough to deserve independent testing, I would
extract it into a separate class and test that class."


# Static Methods

Modern Mockito supports static method mocking using MockedStatic.

Example:

try (MockedStatic<OrderUtils> mocked =
Mockito.mockStatic(OrderUtils.class)) {

    mocked.when(() ->
        OrderUtils.generateOrderReference(1L))
        .thenReturn("MOCK-001");

}


## Static Mock Internal Flow

Test
↓
Mockito.mockStatic()
↓
MockedStatic scope
↓
Static invocation intercepted
↓
Stubbed result returned
↓
Test completes
↓
MockedStatic closed
↓
Normal static behavior restored


## Important Design Point

Static mocking is technically possible, but excessive use of
static dependencies can create tight coupling and make testing
harder.

Prefer injectable dependencies when practical.

Instead of:

OrderService
↓
OrderUtils.staticMethod()


Prefer:

OrderService
↓
OrderReferenceGenerator
↓
Injected implementation


Then the dependency can be mocked normally using @Mock.


# Private vs Static

Private:
→ Usually test indirectly
→ Implementation detail
→ Mockito normally does not mock private methods
→ Extract complex logic if independent testing is needed


Static:
→ Modern Mockito can mock static methods
→ Use MockedStatic
→ Scope static mocks using try-with-resources
→ Prefer injectable dependencies when practical


# Interview Summary

Private method:
→ Test through public behavior.

Static method:
→ Mockito supports MockedStatic.
→ But avoid unnecessary static dependencies.
→ Prefer dependency injection when possible.
# Dependency Injection vs Static Dependency

## Static Dependency

Example:

OrderService
↓
OrderUtils.generateOrderReference()


The service directly depends on a static utility.

Problem:
→ Tight coupling
→ Harder to replace during unit testing
→ Static mocking may be required


## Injectable Dependency

Prefer:

OrderService
↓
OrderReferenceGenerator
↓
DefaultOrderReferenceGenerator


OrderService depends on an abstraction/dependency instead of
directly creating or calling a concrete implementation.


Example:

public interface OrderReferenceGenerator {
String generate(Long orderId);
}


@Component
public class DefaultOrderReferenceGenerator
implements OrderReferenceGenerator {

    @Override
    public String generate(Long orderId) {
        return "ORD-" + orderId;
    }
}


Spring injects the implementation into OrderService through
constructor injection.


## Internal Working

Application startup
↓
Spring scans @Component
↓
DefaultOrderReferenceGenerator bean created
↓
OrderService constructor requires
OrderReferenceGenerator
↓
Spring finds matching implementation
↓
Dependency injected into OrderService


## Testing Benefit

Production:

OrderService
↓
DefaultOrderReferenceGenerator


Unit Test:

OrderService
↓
Mock OrderReferenceGenerator


Test:

@Mock
OrderReferenceGenerator generator;

@InjectMocks
OrderService orderService;


Mockito can replace the real dependency with a mock without
using static mocking.


## Static vs Injectable

Static:

OrderService
↓
OrderUtils.staticMethod()

→ tightly coupled
→ static mocking may be required


Injectable:

OrderService
↓
OrderReferenceGenerator
↓
Implementation

→ loosely coupled
→ easy to mock with @Mock
→ easier to replace implementation
→ easier to unit test


## Interview Point

"Modern Mockito supports static mocking through MockedStatic,
but I prefer dependency injection where practical because it
reduces tight coupling and makes unit testing easier."

This is related to the Dependency Inversion Principle:
high-level business logic should depend on abstractions rather
than concrete implementation details.
# 7.4 — Controller Testing: @WebMvcTest + MockMvc

## Goal

Test the Controller/API layer without starting the real database
or testing the complete application.

Production:

HTTP Request
↓
Controller
↓
Service
↓
Repository
↓
Database


Controller Test:

MockMvc
↓
Controller REAL
↓
Service MOCK


## @WebMvcTest

@WebMvcTest(OrderController.class)

Loads the Spring MVC test slice required to test the controller.

It focuses on MVC-related infrastructure rather than starting
the complete application/database.

Useful for testing:

→ Request mapping
→ Path variables
→ Request body
→ Validation
→ HTTP status
→ JSON response
→ Controller-Service interaction


## MockMvc

MockMvc allows HTTP requests to be simulated without starting
a real web server.

Example:

mockMvc.perform(
get("/api/orders/1")
);


## Internal Flow

mockMvc.perform()
↓
Mock HTTP Request
↓
DispatcherServlet
↓
Controller
↓
Mock Service
↓
Controller response
↓
JSON serialization
↓
MockMvc assertions


## Controller + Mock Service

Controller is REAL.

Service is MOCK.

Example:

@MockBean
OrderService orderService;


Conceptually:

MockMvc
↓
OrderController
↓
Mock OrderService


## when()

Defines the behavior of the mocked service.

when(orderService.getOrderById(1L))
.thenReturn(response);


## verify()

Verifies that the controller called the service correctly.

verify(orderService)
.getOrderById(1L);


## jsonPath()

Used to inspect JSON response fields.

Example:

.andExpect(
jsonPath("$.id").value(1)
);


## POST Request

Example:

mockMvc.perform(
post("/api/orders")
.contentType(MediaType.APPLICATION_JSON)
.content("""
{
"customerName": "Test User",
"customerEmail": "test@gmail.com",
"amount": 5000
}
""")
)
.andExpect(status().isCreated());


contentType()
→ tells server the request body format.

content()
→ provides the actual request body.


## Validation Testing

Invalid request:

Request
↓
@Valid validation
↓
400 Bad Request
↓
Service should NOT be called


Example:

verify(orderService, never())
.createOrder(any(CreateOrderRequest.class));


## Service Unit Test vs Controller Test

Service Unit Test:

JUnit
↓
OrderService REAL
↓
Repository MOCK

Focus:
→ Business logic


Controller Test:

MockMvc
↓
OrderController REAL
↓
Service MOCK

Focus:
→ HTTP/API behavior
→ Validation
→ Status codes
→ JSON
→ Controller-Service interaction


## Key Interview Point

@WebMvcTest is a Spring MVC test slice used to test controller
behavior without loading the complete application.

MockMvc simulates HTTP requests and allows verification of
controller responses without starting a real web server.
