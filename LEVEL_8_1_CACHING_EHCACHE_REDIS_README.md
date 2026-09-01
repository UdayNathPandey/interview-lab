# LEVEL 8.1 --- Caching: Spring Cache, Ehcache and Redis

> Interview Lab --- Hands-on Reference\
> Scope: Interview-level understanding + practical Spring Boot
> implementation\
> Current project stack during this experiment: Spring Boot 4.x / Spring
> Framework 7.x

------------------------------------------------------------------------

# 1. Why Caching?

Without caching:

``` text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Hibernate
  ↓
MySQL
```

If the same read operation happens repeatedly, the application may
repeatedly hit the database.

With caching:

``` text
Client
  ↓
Controller
  ↓
Spring Cache
  │
  ├── HIT  → return cached value
  │
  └── MISS
        ↓
      Service
        ↓
      Repository
        ↓
      Database
        ↓
      Result
        ↓
      Cache
```

Main benefits:

-   Reduce database load
-   Reduce response latency
-   Improve throughput
-   Avoid repeating expensive calculations/database reads

The trade-off is that cache introduces a **consistency problem**: the
cache can become stale.

------------------------------------------------------------------------

# 2. The Most Important Mental Model

There are three different concepts that should not be confused:

``` text
Spring Cache
    ↓
ABSTRACTION

JCache / JSR-107
    ↓
STANDARD API

Ehcache / Redis / Caffeine / etc.
    ↓
ACTUAL CACHE PROVIDER / IMPLEMENTATION
```

## Spring Cache

Spring provides annotations such as:

``` java
@Cacheable
@CachePut
@CacheEvict
```

The application code talks to the Spring Cache abstraction instead of
directly depending on a particular cache implementation.

## JCache / JSR-107

JCache (JSR-107) is a standard caching API. It defines common caching
concepts and APIs and is vendor-neutral.

Ehcache can act as a JCache provider.

## Ehcache

Ehcache is an actual Java caching provider.

Typical architecture:

``` text
@Service
   ↓
@Cacheable
   ↓
Spring Cache abstraction
   ↓
JCache
   ↓
Ehcache
   ↓
JVM / configured cache resources
```

## Redis

Redis is an external key-value data store that can also be used as a
cache.

Typical architecture:

``` text
@Service
   ↓
@Cacheable
   ↓
Spring Cache abstraction
   ↓
RedisCacheManager
   ↓
Redis server
```

------------------------------------------------------------------------

# 3. Internal Working of Spring Cache

This is the interview-level flow worth remembering.

``` text
HTTP Request
    ↓
Controller
    ↓
Spring AOP / Cache Proxy
    ↓
Cache Interceptor
    ↓
Check Cache
    │
    ├── HIT
    │     ↓
    │   Return cached value
    │
    └── MISS
          ↓
        Target Service Method
          ↓
        Repository
          ↓
        Hibernate
          ↓
        Database
          ↓
        Result
          ↓
        Put result into cache
          ↓
        Return response
```

Important:

> `@Cacheable` is intercepted by Spring's caching infrastructure before
> the target method is invoked.

Therefore, on a cache hit, the target method normally does not execute.

This is directly related to the Spring AOP proxy/self-invocation concept
studied earlier.

------------------------------------------------------------------------

# 4. `@Cacheable`

Example:

``` java
@Cacheable(value = "orders", key = "#id")
@Transactional(readOnly = true)
public OrderResponse getOrderById(Long id) {

    System.out.println("SERVICE METHOD EXECUTED");

    Order order = orderRepository.findById(id)
            .orElseThrow(...);

    return mapToResponse(order);
}
```

First request:

``` text
GET /orders/209
    ↓
Cache MISS
    ↓
Service executes
    ↓
Repository
    ↓
SQL SELECT
    ↓
Result
    ↓
Cache
```

Second request:

``` text
GET /orders/209
    ↓
Cache HIT
    ↓
Return cached result
```

No service/repository/SQL execution is required on the hit.

------------------------------------------------------------------------

# 5. `@CachePut`

`@CachePut` is different from `@Cacheable`.

``` java
@CachePut(value = "orders", key = "#id")
public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
    ...
}
```

Internal flow:

``` text
Request
  ↓
Cache interceptor
  ↓
Target method ALWAYS executes
  ↓
Database update
  ↓
Method returns OrderResponse
  ↓
Cache is updated with returned value
```

Golden rule:

``` text
@Cacheable
→ "If cache has it, don't execute the method."

@CachePut
→ "Always execute the method, then update the cache."
```

Typical use:

``` text
GET     → @Cacheable
PUT     → @CachePut
```

------------------------------------------------------------------------

# 6. `@CacheEvict`

Used to remove cached data.

``` java
@CacheEvict(value = "orders", key = "#id")
public void deleteOrder(Long id) {
    ...
}
```

Conceptually:

``` text
DELETE /orders/209
      ↓
Database DELETE
      ↓
Cache entry 209 removed
```

If:

``` text
orders
  209 → old OrderResponse
```

exists after the database record is deleted, a later GET could
incorrectly return the old cached value.

`@CacheEvict` prevents that stale entry from remaining.

Useful arguments:

### `value` / `cacheNames`

Which cache?

``` java
@CacheEvict(value = "orders", ...)
```

### `key`

Which entry?

``` java
key = "#id"
```

### `allEntries`

Clear all entries in a cache:

``` java
@CacheEvict(
    value = "orders",
    allEntries = true
)
```

### `beforeInvocation`

Default is `false`.

Conceptually:

``` text
method executes successfully
        ↓
eviction
```

With:

``` java
beforeInvocation = true
```

the eviction happens before method invocation.

------------------------------------------------------------------------

# 7. Cache Keys

Example:

``` java
@Cacheable(value = "orders", key = "#id")
public OrderResponse getOrderById(Long id)
```

Then:

``` text
orders
  209 → OrderResponse
  210 → OrderResponse
  211 → OrderResponse
```

The cache name is:

``` text
orders
```

The key is:

``` text
209
210
211
```

With multiple arguments, a custom key can be created using SpEL.

Example:

``` java
key = "#id + '-' + #type"
```

Potential key:

``` text
209-PREMIUM
```

------------------------------------------------------------------------

# 8. Current Simple Spring Cache vs Ehcache

When no specific cache provider is configured, Spring Boot can
auto-configure a simple in-memory provider based on concurrent maps.

Conceptually:

``` text
Application JVM
    ↓
Spring CacheManager
    ↓
Simple in-memory cache
    ↓
Map
    ↓
key → value
```

Example:

``` text
orders
  209 → OrderResponse
  210 → OrderResponse
```

This was enough to prove:

``` text
First GET  → DB
Second GET → Cache
```

It is excellent for learning the abstraction, but it is not normally the
preferred production architecture.

------------------------------------------------------------------------

# 9. Ehcache

## What is Ehcache?

Ehcache is a Java caching provider/library.

It supports features such as:

-   In-process caching
-   Heap resources
-   Off-heap resources
-   Expiration
-   Eviction
-   JCache/JSR-107 integration
-   Additional advanced cache features

For this project we used Ehcache through Spring's cache abstraction and
JCache.

------------------------------------------------------------------------

# 10. Ehcache Architecture

``` text
Application
    ↓
Spring Cache abstraction
    ↓
JCache / JSR-107
    ↓
Ehcache provider
    ↓
JVM / Ehcache resources
```

The service still uses:

``` java
@Cacheable("orders")
```

It does not need to know that Ehcache is underneath.

This is the main benefit of the abstraction.

------------------------------------------------------------------------

# 11. Ehcache Configuration --- Hands-on Project

## Step 1 --- Spring Cache dependency

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

## Step 2 --- Ehcache dependency

For Ehcache 3:

``` xml
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
```

For JCache:

``` xml
<dependency>
    <groupId>javax.cache</groupId>
    <artifactId>cache-api</artifactId>
</dependency>
```

The exact compatible dependency combination matters with modern Spring
Boot/Jakarta versions; see the compatibility section below.

## Step 3 --- Enable caching

``` java
@EnableCaching
@SpringBootApplication
public class InterviewLabApplication {
    ...
}
```

## Step 4 --- Tell Spring to use JCache

``` properties
spring.cache.type=jcache
spring.cache.jcache.config=classpath:ehcache.xml
```

The second property tells Spring where the provider-specific cache
configuration is located.

------------------------------------------------------------------------

# 12. `ehcache.xml`

Example used for the hands-on:

``` xml
<?xml version="1.0" encoding="UTF-8"?>

<config
        xmlns="http://www.ehcache.org/v3">

    <cache alias="orders">

        <key-type>java.lang.Long</key-type>

        <value-type>com.interviewlab.dto.OrderResponse</value-type>

        <expiry>
            <ttl unit="seconds">5</ttl>
        </expiry>

        <resources>
            <heap unit="entries">1000</heap>
        </resources>

    </cache>

</config>
```

Meaning:

``` text
Cache name:
orders

Key:
Long

Value:
OrderResponse

TTL:
5 seconds

Heap capacity:
1000 entries
```

------------------------------------------------------------------------

# 13. Ehcache Internal Flow

### Cache MISS

``` text
GET /orders/211
      ↓
Spring Cache Proxy
      ↓
JCache
      ↓
Ehcache
      ↓
MISS
      ↓
OrderService
      ↓
OrderRepository
      ↓
Hibernate
      ↓
MySQL
      ↓
OrderResponse
      ↓
Ehcache.put(211, response)
      ↓
Response
```

### Cache HIT

``` text
GET /orders/211
      ↓
Spring Cache Proxy
      ↓
JCache
      ↓
Ehcache
      ↓
HIT
      ↓
OrderResponse
```

No database query is required on the hit.

------------------------------------------------------------------------

# 14. TTL --- Actual Ehcache Experiment

We configured:

``` xml
<ttl unit="seconds">5</ttl>
```

Flow:

``` text
10:00:00
GET /orders/211
    ↓
MISS
    ↓
DB
    ↓
Cache entry created


10:00:03
GET /orders/211
    ↓
HIT
    ↓
No SELECT


10:00:06
GET /orders/211
    ↓
Entry expired
    ↓
MISS
    ↓
DB SELECT
    ↓
New cache entry
```

This successfully proved that TTL is time-based cache expiration.

------------------------------------------------------------------------

# 15. Stale Data

Suppose:

``` text
Database:
209 → amount 1000

Cache:
209 → amount 1000
```

Now somebody directly updates the DB:

``` text
Database:
209 → amount 5000

Cache:
209 → amount 1000
```

GET:

``` text
GET /orders/209
    ↓
Cache HIT
    ↓
1000
```

The application can return stale data.

Important:

> A normal cache hit does not automatically query the database to verify
> freshness. That would defeat much of the performance benefit of
> caching.

------------------------------------------------------------------------

# 16. Cache Invalidation

Invalidation means:

> The cached value is no longer valid, so remove or refresh it.

Typical strategy:

``` text
READ
 ↓
@Cacheable


UPDATE
 ↓
@CachePut
or
@CacheEvict + later reload


DELETE
 ↓
@CacheEvict
```

TTL can also act as a time-based expiration/safety mechanism.

------------------------------------------------------------------------

# 17. Explicit Invalidation vs TTL

## Explicit invalidation

Event/change driven:

``` text
Database changes
      ↓
Application knows
      ↓
Cache update/eviction
```

Examples:

``` java
@CachePut
@CacheEvict
```

## TTL

Time driven:

``` text
Cache entry created
      ↓
Time passes
      ↓
TTL expires
      ↓
Entry expires
```

Shortcut:

``` text
Invalidation = "Data changed."

TTL          = "Enough time passed."
```

Real systems may use both.

------------------------------------------------------------------------

# 18. Important Version / JAXB Issue We Encountered

During this hands-on, the application was on the modern Spring Boot 4 /
Spring Framework 7 line.

The initial JCache + Ehcache setup produced:

``` text
BeanCreationException

Failed to instantiate javax.cache.CacheManager

javax/xml/bind/ValidationEventHandler
```

The important lesson is:

``` text
Spring Boot 4 / modern Jakarta ecosystem
                ↓
       compatibility matters
                ↓
      Ehcache/JCache/JAXB versions
```

## Why did this happen?

Ehcache 3's XML configuration uses JAXB.

Historically, JAXB lived in the `javax.xml.bind.*` namespace.

Modern Jakarta-based applications use the `jakarta.*` namespace for the
newer Jakarta APIs.

Ehcache 3 provides a Jakarta-compatible variant, and its documentation
describes the Jakarta variant and the corresponding JAXB runtime
requirements.

For Maven, the Ehcache documentation shows the Jakarta variant using:

``` xml
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>3.11.1</version>
    <classifier>jakarta</classifier>
    <exclusions>
        <exclusion>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>[3,3.1)</version>
</dependency>
```

The exact version should be aligned with the application's Spring
Boot/Jakarta stack and the Ehcache version being used.

### Kid-level understanding

Old world:

``` text
Java/JAXB
   ↓
javax.xml.bind.*
```

Modern Jakarta world:

``` text
Jakarta JAXB
   ↓
jakarta.xml.bind.*
```

Ehcache's normal/JCache/JAXB dependencies and its Jakarta variant
therefore need to be compatible with the application's ecosystem.

### Interview takeaway

If you see:

``` text
ClassNotFoundException
javax/xml/bind/...
```

while starting Ehcache/JCache:

Do not immediately blame Spring Cache.

Check:

``` text
Spring Boot version
       ↓
Ehcache version
       ↓
JCache API
       ↓
JAXB implementation
       ↓
javax vs jakarta compatibility
```

------------------------------------------------------------------------

# 19. `heap` Warning We Saw

IntelliJ showed a warning around:

``` xml
<heap unit="entries">1000</heap>
```

The important point is:

-   It was an IDE/schema warning, not the application startup failure.
-   The application successfully ran after the dependency compatibility
    issue was resolved.
-   The warning is related to Ehcache configuration/schema evolution,
    not to the fundamental Spring Cache mechanism.

For this interview project, the important concepts are:

``` text
cache
TTL
expiry
heap resource
provider
Spring Cache abstraction
```

Do not spend unnecessary time on every XML schema detail unless a
project requires it.

------------------------------------------------------------------------

# 20. Ehcache in GKE / Kubernetes

This is extremely important for microservices.

Suppose:

``` text
Load Balancer
      │
 ┌────┼────┐
 ↓    ↓    ↓
Pod1 Pod2 Pod3
```

Each pod runs:

``` text
Spring Boot
    ↓
Ehcache
    ↓
JVM memory
```

Then:

``` text
Pod 1 → Cache 1
Pod 2 → Cache 2
Pod 3 → Cache 3
```

They are separate caches.

Example:

``` text
Pod 1:
orders[209] = 5000

Pod 2:
orders[209] = 1000
```

Updating Pod 1's local cache does not automatically update Pod 2's local
cache.

Therefore local Ehcache can create consistency challenges in a
horizontally scaled application.

------------------------------------------------------------------------

# 21. Why Redis Became Important

Now imagine:

``` text
Pod 1
Pod 2
Pod 3
...
Pod 20
```

If every pod maintains its own local cache:

``` text
Pod 1 → local cache
Pod 2 → local cache
Pod 3 → local cache
...
Pod 20 → local cache
```

Problems:

-   Duplicate cached data
-   Different cache state across pods
-   Cache invalidation becomes harder
-   A request routed to another pod may see a different cache state

A shared external cache solves this architecture problem:

``` text
             Load Balancer
            /      |      \
           ↓       ↓       ↓
        Pod 1    Pod 2    Pod 3
           \       |       /
            \      |      /
                 Redis
                   ↓
                 MySQL
```

All application instances can use the same Redis cache.

------------------------------------------------------------------------

# 22. Redis --- What Is It?

Redis is an external in-memory key-value data store that is commonly
used for:

-   Caching
-   Session storage
-   Distributed coordination/use cases
-   Pub/Sub
-   Other key-value workloads

For Spring Boot caching:

``` text
Spring Cache
     ↓
RedisCacheManager
     ↓
Redis
```

Spring Boot provides Redis auto-configuration and the
`spring-boot-starter-data-redis` starter. By default, that starter uses
Lettuce as the Redis client.

------------------------------------------------------------------------

# 23. Ehcache vs Redis

  -----------------------------------------------------------------------
  Feature                 Ehcache                 Redis
  ----------------------- ----------------------- -----------------------
  Main type               Java cache provider     External key-value data
                                                  store

  Typical location        Application/JVM         Separate Redis
                                                  server/service

  Local to JVM            Yes, commonly           No

  Shared across pods      Not automatically       Yes

  Multi-instance          More difficult          Natural fit
  architecture                                    

  Network hop             No for local cache      Yes

  Typical use             Local/in-process cache  Distributed/shared
                                                  cache

  TTL                     Supported               Supported

  Application restart     Local cache normally    Data can survive
                          lost                    depending on Redis
                                                  persistence/config

  Kubernetes scaling      Each pod gets its own   Pods can share one
                          local cache             Redis deployment

  Complexity              Lower                   Higher

  Common production role  Local/L1 cache          Shared/distributed
                                                  cache
  -----------------------------------------------------------------------

Important:

> Redis is not simply "a better Ehcache". They solve different
> architectural needs.

Ehcache:

``` text
Very fast local access
```

Redis:

``` text
Shared cache across application instances
```

------------------------------------------------------------------------

# 24. Two-Level Cache Idea

A more advanced architecture can combine them:

``` text
Request
  ↓
L1 Cache
(Ehcache/Caffeine)
  ↓
MISS
  ↓
L2 Cache
(Redis)
  ↓
MISS
  ↓
MySQL
```

Conceptually:

``` text
Application
   ↓
L1 Local Cache
   ↓ miss
L2 Redis
   ↓ miss
Database
```

This is useful to know for interviews, but it is outside the current
hands-on scope.

------------------------------------------------------------------------

# 25. Redis Implementation --- Reference Steps

We did not need Redis to understand the caching fundamentals. The
following is a reference implementation path for a future project.

## Step 1 --- Add dependency

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

Spring Boot manages the compatible client dependencies.

The default client is Lettuce.

------------------------------------------------------------------------

## Step 2 --- Run Redis

Local development can run Redis through Docker, a local installation, or
another Redis environment.

Typical default endpoint:

``` text
localhost:6379
```

For example:

``` text
Redis Server
    ↓
localhost
    ↓
6379
```

------------------------------------------------------------------------

## Step 3 --- Configure connection

Typical Spring Boot configuration:

``` properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

If authentication is configured:

``` properties
spring.data.redis.username=...
spring.data.redis.password=...
```

------------------------------------------------------------------------

## Step 4 --- Enable Spring caching

``` java
@EnableCaching
```

------------------------------------------------------------------------

## Step 5 --- Use Spring Cache annotations

Your service code can remain:

``` java
@Cacheable(value = "orders", key = "#id")
public OrderResponse getOrderById(Long id) {
    ...
}
```

This is the important abstraction benefit.

The service does not need to know whether the cache provider is:

``` text
Ehcache
Redis
Caffeine
Simple Cache
```

------------------------------------------------------------------------

# 26. Redis Internal Flow

## Cache MISS

``` text
GET /orders/209
      ↓
Spring Cache Proxy
      ↓
RedisCacheManager
      ↓
Redis
      ↓
MISS
      ↓
Service
      ↓
Repository
      ↓
MySQL
      ↓
OrderResponse
      ↓
Redis SET
      ↓
Response
```

## Cache HIT

``` text
GET /orders/209
      ↓
Spring Cache Proxy
      ↓
RedisCacheManager
      ↓
Redis
      ↓
HIT
      ↓
OrderResponse
```

Database is not queried on the cache hit.

------------------------------------------------------------------------

# 27. Redis TTL Configuration

Spring Boot supports Redis cache TTL configuration.

Simple configuration:

``` properties
spring.cache.type=redis
spring.cache.redis.time-to-live=10m
```

You can also define cache names:

``` properties
spring.cache.cache-names=orders,customers
```

Conceptually:

``` text
orders:209
    ↓
OrderResponse
    ↓
TTL = 10 minutes
```

Spring Data Redis also allows per-cache configuration through
`RedisCacheConfiguration` / `RedisCacheManager`.

------------------------------------------------------------------------

# 28. Redis Serialization

Redis stores bytes/serialized representations rather than Java objects
directly.

Therefore the application needs serializers.

Conceptually:

``` text
Java OrderResponse
      ↓
Serializer
      ↓
bytes
      ↓
Redis
```

On read:

``` text
Redis bytes
      ↓
Deserializer
      ↓
OrderResponse
```

This is an important reason to think about:

-   Serialization format
-   Compatibility
-   Security
-   Payload size

For a real production project, choose the serialization strategy
deliberately rather than blindly accepting defaults.

------------------------------------------------------------------------

# 29. Redis Key Structure

Spring Data Redis cache support prefixes keys by cache name by default.

Conceptually:

``` text
orders::209
orders::210
customers::10
```

This prevents identical numeric keys in different caches from colliding.

Example:

``` text
orders::209
customers::209
```

These are different keys.

Keeping cache-name prefixes enabled is generally recommended unless you
have a specific reason to change them.

------------------------------------------------------------------------

# 30. Redis `@Cacheable`, `@CachePut`, `@CacheEvict`

The application-level annotations remain the same:

``` java
@Cacheable("orders")
```

``` java
@CachePut("orders")
```

``` java
@CacheEvict("orders")
```

Only the backing cache provider changes:

``` text
Before:

@Cacheable
    ↓
Spring Cache
    ↓
Ehcache
    ↓
JVM


After:

@Cacheable
    ↓
Spring Cache
    ↓
RedisCacheManager
    ↓
Redis
```

This is the biggest reason to learn Spring Cache abstraction first.

------------------------------------------------------------------------

# 31. Cache Strategy Summary

A common pattern:

``` text
READ
 ↓
@Cacheable
 ↓
HIT → return cache
MISS → DB → cache


UPDATE
 ↓
@CachePut
 ↓
DB update → refresh cache


DELETE
 ↓
@CacheEvict
 ↓
DB delete → remove cache


TTL
 ↓
Time-based expiration
```

------------------------------------------------------------------------

# 32. Interview Questions

## Q1. Is Spring Cache a cache implementation?

No.

Spring Cache is an abstraction.

------------------------------------------------------------------------

## Q2. Is Ehcache a cache implementation?

Yes.

Ehcache is a Java cache provider.

------------------------------------------------------------------------

## Q3. Is JCache the same as Ehcache?

No.

JCache is a standard API/specification.

Ehcache can implement/provide JCache support.

------------------------------------------------------------------------

## Q4. Why use Redis if Ehcache already exists?

Because Ehcache is commonly local to the JVM.

Redis can provide a shared cache across multiple application instances.

------------------------------------------------------------------------

## Q5. Does `@Cacheable` execute the method every time?

No.

On a cache hit, the target method normally does not execute.

------------------------------------------------------------------------

## Q6. Does `@CachePut` skip the method on a cache hit?

No.

The method always executes and its result is put into the cache.

------------------------------------------------------------------------

## Q7. What does `@CacheEvict` do?

Removes cache entries.

------------------------------------------------------------------------

## Q8. What is stale cache?

When cached data is older/different from the source-of-truth data.

------------------------------------------------------------------------

## Q9. What is TTL?

Time To Live --- the lifetime/expiration duration of a cache entry.

------------------------------------------------------------------------

## Q10. Is Ehcache automatically shared between Kubernetes pods?

No.

A local Ehcache instance normally belongs to its JVM/pod.

------------------------------------------------------------------------

## Q11. Is Redis a database?

Redis is an in-memory key-value data store and can serve many roles,
including caching. In a caching architecture, it is normally treated as
a cache layer rather than the application's relational source of truth.

------------------------------------------------------------------------

# 33. One-Page Mental Model

``` text
                    SPRING CACHE
                    (ABSTRACTION)
                         │
             ┌───────────┼───────────┐
             ↓           ↓           ↓
         @Cacheable   @CachePut  @CacheEvict
             │           │           │
            READ       UPDATE       DELETE
                         │
                         ↓
                  CACHE PROVIDER
                         │
              ┌──────────┼──────────┐
              ↓          ↓          ↓
           Simple     Ehcache     Redis
           In-memory   Local      Shared
              │          │          │
             JVM        JVM       External
```

------------------------------------------------------------------------

# 34. Interview-Level Internal Flow

The most important flow to remember:

``` text
Request
  ↓
Controller
  ↓
Spring Proxy
  ↓
Cache Interceptor
  ↓
CacheManager
  ↓
Cache Provider
  │
  ├── HIT
  │     ↓
  │   Return cached value
  │
  └── MISS
        ↓
      Service
        ↓
      Repository
        ↓
      Hibernate
        ↓
      Database
        ↓
      Result
        ↓
      Cache.put()
        ↓
      Response
```

Provider-specific layer:

``` text
Simple:

CacheManager
    ↓
ConcurrentMap
    ↓
JVM


Ehcache:

CacheManager
    ↓
JCache/Ehcache
    ↓
JVM/cache resources


Redis:

RedisCacheManager
    ↓
Redis client
    ↓
Redis server
```

------------------------------------------------------------------------

# 35. What We Actually Completed in Interview Lab

### Experiment 1 --- `@Cacheable`

``` text
First request
→ SQL

Second identical request
→ no SQL
→ cache hit
```

### Experiment 2 --- `@CachePut`

``` text
PUT
→ method executes
→ DB updates
→ cache updated

GET
→ updated value from cache
```

### Experiment 3 --- `@CacheEvict`

``` text
DELETE
→ DB delete
→ cache entry removed

GET
→ cache miss
→ DB queried
→ not found
```

### Experiment 4 --- Stale Cache

``` text
DB changed directly
→ cache still contained old value
→ GET returned cached old value
```

### Experiment 5 --- Ehcache TTL

``` text
TTL = 5 seconds

within 5 seconds
→ HIT

after expiration
→ MISS
→ DB query
→ cache recreated
```

------------------------------------------------------------------------

# 36. Scope Decision

For Interview Lab, we intentionally did NOT go deep into:

-   Redis Cluster administration
-   Redis Sentinel internals
-   Cache stampede mitigation
-   Cache avalanche
-   Cache penetration
-   Distributed locking
-   Pub/Sub invalidation architecture
-   Two-level L1/L2 cache implementation
-   Advanced Ehcache clustering
-   Detailed serializer benchmarking

Those are production-specialization topics.

The required developer/interview foundation is:

``` text
Spring Cache abstraction
        ↓
@Cacheable
@CachePut
@CacheEvict
        ↓
Cache key
        ↓
Cache hit/miss
        ↓
Stale data
        ↓
Invalidation
        ↓
TTL
        ↓
Ehcache = local cache provider
        ↓
Redis = shared/distributed cache option
```

------------------------------------------------------------------------

# 37. Final Interview Answer

If asked:

> "Explain caching in your Spring Boot application."

A strong concise answer:

> "We use Spring's Cache abstraction at the service layer. `@Cacheable`
> checks the cache before executing the method, so a cache hit avoids
> the repository/database call. `@CachePut` always executes the method
> and refreshes the cache with its returned value, while `@CacheEvict`
> removes entries when data becomes invalid. The underlying provider can
> be something like Ehcache or Redis. Ehcache is commonly an
> in-process/local cache, whereas Redis is an external shared cache and
> is more suitable when multiple application instances or Kubernetes
> pods need a common cache. TTL and explicit invalidation are used to
> control cache freshness."

------------------------------------------------------------------------

# 38. Official References

Spring Boot Caching:
https://docs.spring.io/spring-boot/reference/io/caching.html

Spring Boot Redis:
https://docs.spring.io/spring-boot/reference/data/nosql.html

Spring Data Redis Cache:
https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html

Ehcache Documentation: https://www.ehcache.org/documentation/

Ehcache Getting Started / Jakarta Variant:
https://www.ehcache.org/documentation/3.11/getting-started.html

Ehcache JCache / JSR-107:
https://www.ehcache.org/documentation/3.9/107.html
