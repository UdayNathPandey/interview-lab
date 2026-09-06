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
