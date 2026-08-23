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
