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

Implement production-style exception handling instead of returning generic errors or using try-catch in every controller.

## Concepts

- Custom Runtime Exception
- @ControllerAdvice
- @ExceptionHandler
- HTTP status codes
- Standard error response
- 404 NOT_FOUND
- 400 BAD_REQUEST
- Exception propagation
- Separation of business logic and error handling

## Architecture

Controller
↓
Service
↓
Exception thrown
↓
@ControllerAdvice
↓
@ExceptionHandler
↓
Standard Error Response

## Planned Exceptions

ResourceNotFoundException
BadRequestException

## Standard Error Response

- timestamp
- status
- error
- message
- path

## Interview Questions

- Why use custom exceptions?
- Why use @ControllerAdvice?
- @ControllerAdvice vs @RestControllerAdvice?
- @ExceptionHandler kaise work karta hai?
- Why shouldn't every controller have try-catch?
- RuntimeException vs Exception?
- 400 vs 404 vs 500?
- Exception propagation Spring MVC mein kaise hoti hai?