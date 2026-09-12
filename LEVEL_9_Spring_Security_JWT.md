# LEVEL 9.1 — Spring Security Foundation

## Goal

Understand how an HTTP request travels through Tomcat,
Servlet Filters, Spring Security Filter Chain and finally
reaches DispatcherServlet and Controller.

## High-Level Flow

Client
↓
Tomcat
↓
Servlet Filter Chain
↓
DelegatingFilterProxy
↓
FilterChainProxy
↓
SecurityFilterChain
↓
Security Filters
↓
DispatcherServlet
↓
Controller

## Key Classes

DelegatingFilterProxy
↓
FilterChainProxy
↓
SecurityFilterChain

## Important Concept

Spring Security is implemented using Servlet Filters.

Security filters execute before DispatcherServlet/controller
processing.

A request can be rejected by Spring Security before it reaches
the Controller.

## 9.1 Experiment

Add Spring Security dependency and observe:

1. Spring Security auto-configuration
2. Default SecurityFilterChain
3. Protected endpoint behavior
4. Generated default password
5. Security-related request flow

JWT is NOT implemented in 9.1.
# LEVEL 9.3 — Authentication

## Goal

Understand how Spring Security authenticates a username/password
request and how Authentication reaches SecurityContext.

## Core Flow

Username + Password
↓
Authentication Request
↓
AuthenticationManager
↓
AuthenticationProvider
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
UserDetails
↓
PasswordEncoder
↓
Authentication Success
↓
SecurityContext
↓
Authorization
↓
Controller

## Responsibilities

Authentication
- Represents authentication information.

AuthenticationManager
- Coordinates authentication.
- Delegates to AuthenticationProvider.

ProviderManager
- Common AuthenticationManager implementation.
- Uses one or more AuthenticationProviders.

AuthenticationProvider
- Performs actual authentication.

DaoAuthenticationProvider
- Username/password authentication.
- Uses UserDetailsService + PasswordEncoder.

UserDetailsService
- Loads user-specific authentication data.
- Main method:
  loadUserByUsername(String username)

UserDetails
- Spring Security representation of a user.
- Contains username, password, authorities and account-status information.

PasswordEncoder
- Secure password encoding and verification.
- Registration:
  raw password → encode() → stored value
- Login:
  raw password + stored value → matches()

SecurityContext
- Holds the current Authentication.

SecurityContextHolder
- Provides access to the current SecurityContext.

## Important Distinction

Authentication:
"Who are you?"

Authorization:
"Are you allowed?"

authenticated():
- Authorization rule.
- Checks whether the current request has an authenticated identity.
- Does NOT itself validate username/password.

## Username/Password Flow

Client
↓
Tomcat
↓
Spring Security Filter Chain
↓
Authentication Filter
↓
AuthenticationManager
↓
AuthenticationProvider
↓
UserDetailsService
↓
Database / UserDetails
↓
PasswordEncoder
↓
Authentication
↓
SecurityContext
↓
Authorization
↓
DispatcherServlet
↓
Controller

## Experiment

Configured:
- HTTP Basic authentication
- In-memory UserDetailsService
- PasswordEncoder
- Protected /api/orders endpoint

Credentials:
username = uday
password = password

Without credentials:
→ authentication failure / 401

With valid credentials:
→ Authentication successful
→ SecurityContext populated
→ authenticated() passes
→ Controller executes

## JWT Connection

Username/password authentication:

Credentials
↓
AuthenticationManager
↓
AuthenticationProvider
↓
Authentication
↓
SecurityContext

JWT authentication will later replace the credential-verification
mechanism while still producing an Authentication that can be placed
into the SecurityContext.

## Status

9.3 COMPLETE
# LEVEL 9.4 — UserDetailsService + DB User

## Goal

Connect Spring Security authentication with a real user stored in MySQL.

## Flow

HTTP Basic credentials
↓
Authentication Filter
↓
AuthenticationManager
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
UserRepository
↓
MySQL users table
↓
UserDetails
↓
PasswordEncoder
↓
Authentication success/failure
↓
SecurityContext
↓
Authorization
↓
Controller

## Important

UserDetailsService loads user information.
It does NOT itself validate the password.

DaoAuthenticationProvider:
- calls UserDetailsService
- receives UserDetails
- uses PasswordEncoder to verify password

## 9.4 Experiment

Replace the in-memory user from 9.3 with a MySQL-backed user.

Authentication mechanism remains HTTP Basic.

JWT is still NOT implemented.

## Status

9.4 COMPLETE
# LEVEL 9.5 — PasswordEncoder

## Goal

Understand secure password storage and verification in Spring Security.

## Core Flow

Registration:
Raw Password
↓
PasswordEncoder.encode()
↓
Encoded/hashed password
↓
Database

Authentication:
Raw Password
↓
PasswordEncoder.matches(raw, storedEncoded)
↓
true / false

## Important

- Never store raw passwords in the database.
- `encode()` is used when storing a password.
- `matches()` is used during authentication.
- Passwords are not decrypted during login.
- Hashing and verification are different from encryption/decryption.
- `PasswordEncoder` is used by the authentication provider to verify
  the presented password against the stored encoded password.

## Current Project

PasswordEncoder:
PasswordEncoderFactories.createDelegatingPasswordEncoder()

This produces encoded passwords with an algorithm id such as:

{bcrypt}...

The `{bcrypt}` prefix tells the DelegatingPasswordEncoder which
password algorithm should be used for verification.

## Status

9.5 COMPLETE
# LEVEL 9.6 — AuthenticationManager / AuthenticationProvider

## Goal

Understand how Spring Security delegates authentication from
AuthenticationManager to AuthenticationProvider.

## Core Flow

```
AuthenticationManager          ← interface
          │
          ▼
    ProviderManager            ← implementation
          │
          ▼
AuthenticationProvider         ← interface
          │
          ▼
DaoAuthenticationProvider      ← implementation
```

Authentication Filter
↓
AuthenticationManager
↓
ProviderManager
↓
AuthenticationProvider
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
PasswordEncoder
↓
Authentication

## Important

AuthenticationManager is the main authentication API.

ProviderManager is the commonly used AuthenticationManager
implementation.

ProviderManager delegates authentication to one or more
AuthenticationProvider instances.

AuthenticationProvider performs the actual authentication
logic for a particular authentication mechanism.

For username/password authentication:

ProviderManager
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
PasswordEncoder

## Current Experiment

HTTP Basic authentication is used only to demonstrate the
authentication pipeline.

JWT is NOT implemented yet.

## Status

9.6 COMPLETE
# LEVEL 9.7 — Login API

## Goal

Build a custom Login API using Spring Security's
AuthenticationManager.

The Login API will accept username and password,
authenticate the user, and return a successful login response.

JWT is NOT generated yet.
JWT generation will be implemented in 9.8.

---

## Login Flow

Client
↓
POST /auth/login
↓
LoginController
↓
AuthenticationManager
↓
ProviderManager
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
AppUserRepository
↓
MySQL
↓
UserDetails
↓
PasswordEncoder.matches()
↓
Authentication
↓
Login success response

---

## Important Classes

LoginController
↓
AuthenticationManager
↓
ProviderManager
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
PasswordEncoder

---

## Important Concept

The Login API does NOT manually check the password.

It delegates authentication to AuthenticationManager.

AuthenticationManager delegates to the appropriate
AuthenticationProvider.

For username/password authentication:

AuthenticationManager
↓
ProviderManager
↓
DaoAuthenticationProvider

DaoAuthenticationProvider uses:

UserDetailsService → load user
PasswordEncoder    → verify password

---

## Current Login API

POST /auth/login

Request:
{
"username": "uday",
"password": "password"
}

Successful response:
{
"message": "Login successful",
"username": "uday"
}

JWT is NOT returned yet.

---

## Important

Login API performs authentication.

Authorization will happen after authentication.

JWT will be introduced in Level 9.8.

---

## Status

9.7 COMPLETE
```
                 POST /auth/login
                        │
                        ▼
                LoginController
                        │
                        │ username + password
                        ▼
          UsernamePasswordAuthenticationToken
                        │
                        ▼
             AuthenticationManager
                        │
                        ▼
                 ProviderManager
                        │
                        ▼
           DaoAuthenticationProvider
                  /             \
                 /               \
                ▼                 ▼
     UserDetailsService     PasswordEncoder
                │                 │
                ▼                 │
      AppUserRepository           │
                │                 │
                ▼                 │
              MySQL               │
                │                 │
                ▼                 │
           UserDetails ───────────┘
                        │
                        ▼
              Authentication SUCCESS
                        │
                        ▼
                 LoginController
                        │
                        ▼
                LoginResponse
```
````
                    AuthenticationManager
                       /            \
                      /              \
           HTTP Basic             Login API
                ↓                    ↓
         Authentication         Authentication
            request                request
                      \            /
                       ↓          ↓
                   ProviderManager
                         ↓
               DaoAuthenticationProvider
                         ↓
                UserDetailsService
                         ↓
                      MySQL
````
# LEVEL 9.8 — JWT Generation

## Goal

Understand and implement JWT generation after successful
username/password authentication.

JWT is generated only after AuthenticationManager successfully
authenticates the user.

---

## Core Flow

Client
↓
POST /auth/login
↓
LoginController
↓
AuthenticationManager
↓
ProviderManager
↓
DaoAuthenticationProvider
↓
UserDetailsService
↓
MySQL
↓
PasswordEncoder
↓
Authentication SUCCESS
↓
JwtService
↓
JWT generated
↓
LoginResponse
↓
Client

---

## JWT Structure

JWT consists of three Base64URL-encoded parts:

HEADER.PAYLOAD.SIGNATURE

Header:
Algorithm + Token Type

Payload:
Claims such as subject, authorities, expiration

Signature:
Cryptographic signature used to verify token integrity

---

## Important

JWT is signed, not encrypted.

The payload should not contain sensitive secrets such as
passwords.

The client can decode the JWT payload, but cannot modify it
without invalidating the signature.

---

## Authentication vs JWT

Authentication:
"Who are you and are your credentials valid?"

JWT:
"Here is a signed token representing the authenticated identity."

---

## Current Experiment

Login API:

POST /auth/login

Request:
{
"username": "uday",
"password": "password"
}

Successful response:

{
"message": "Login successful",
"username": "uday",
"token": "<JWT>"
}

JWT validation/filter processing will be implemented in 9.9.

---

## Status

9.8 COMPLETE
