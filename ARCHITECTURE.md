# Architecture Overview - Student Complaint System with Spring Security

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER (React)                        │
├─────────────────────────────────────────────────────────────────┤
│  Login Page (Register/Login)                                    │
│       ↓                                                          │
│  Student Dashboard ← → Admin Dashboard                          │
│  (My Complaints)      (All Complaints)                          │
└────────────────┬──────────────────────────────────────────────┘
                 │ HTTPS/HTTP with Cookies
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                    API LAYER (Spring Boot)                      │
├─────────────────────────────────────────────────────────────────┤
│  Spring Security Filter Chain                                   │
│        ↓                                                        │
│  Authentication Manager                                         │
│        ↓                                                        │
│  DaoAuthenticationProvider ← CustomUserDetailsService           │
│        ↓                                                        │
│  BCryptPasswordEncoder                                          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Controllers (Request Handling)                          │ │
│  │  ├─ AuthController (register, login, logout)           │ │
│  │  ├─ ComplaintController (CRUD complaints)              │ │
│  │  ├─ AdminController (admin management)                 │ │
│  │  └─ StudentController (student info)                   │ │
│  └──────────────────────────────────────────────────────────┘ │
│                    ↓                                            │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Services (Business Logic)                               │ │
│  │  ├─ ComplaintService                                    │ │
│  │  ├─ AdminService                                        │ │
│  │  ├─ StudentService                                      │ │
│  │  └─ CustomUserDetailsService                            │ │
│  └──────────────────────────────────────────────────────────┘ │
│                    ↓                                            │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │  Repositories (Database Access)                          │ │
│  │  ├─ UserRepository                                      │ │
│  │  ├─ ComplaintRepository                                 │ │
│  │  ├─ AdminRepository                                     │ │
│  │  └─ StudentRepository                                   │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────┬──────────────────────────────────────┘
                          │ JDBC/JPA
                          ↓
                  ┌───────────────┐
                  │    MySQL      │
                  │   Database    │
                  └───────────────┘
```

## Authentication Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    REGISTRATION FLOW                         │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  User fills:                                                │
│  ├─ Name                                                    │
│  ├─ Email (must be unique)                                 │
│  └─ Password (min 6 chars)                                 │
│         ↓                                                   │
│  POST /api/auth/register                                   │
│         ↓                                                   │
│  AuthController.register()                                 │
│         ↓                                                   │
│  Check if email already exists                             │
│         ├─ YES → Return 400 Bad Request                    │
│         └─ NO → Continue                                   │
│         ↓                                                   │
│  BCryptPasswordEncoder.encode(password)                    │
│         ↓                                                   │
│  Create User with role=STUDENT                             │
│         ↓                                                   │
│  UserRepository.save(user)                                 │
│         ↓                                                   │
│  Return: Success (201 Created)                             │
│                                                              │
└──────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────┐
│                    LOGIN FLOW                                │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  User enters:                                               │
│  ├─ Email                                                   │
│  └─ Password                                                │
│         ↓                                                   │
│  POST /api/auth/login                                      │
│         ↓                                                   │
│  AuthenticationManager.authenticate()                      │
│         ↓                                                   │
│  DaoAuthenticationProvider                                 │
│         ↓                                                   │
│  CustomUserDetailsService.loadUserByUsername(email)        │
│         ↓                                                   │
│  UserRepository.findByEmail(email)                         │
│         ├─ User not found → UsernameNotFoundException      │
│         └─ User found → Continue                           │
│         ↓                                                   │
│  BCryptPasswordEncoder.matches(password, stored)           │
│         ├─ No match → BadCredentialsException              │
│         └─ Match → Continue                                │
│         ↓                                                   │
│  Create Authentication object                             │
│         ↓                                                   │
│  Set in SecurityContext                                    │
│         ↓                                                   │
│  Create HttpSession (Cookie JSESSIONID)                    │
│         ↓                                                   │
│  Return: User details + role                               │
│         ↓                                                   │
│  Frontend: Redirect to /student or /admin                  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## Authorization Flow

```
┌──────────────────────────────────────────────────────────────┐
│              PROTECTED REQUEST (With Session)               │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Browser sends: JSESSIONID cookie                           │
│         ↓                                                   │
│  Spring Security Filter Chain                              │
│         ↓                                                   │
│  SessionManagementFilter                                   │
│    ├─ Validate session exists                              │
│    └─ Load user from session                               │
│         ↓                                                   │
│  AuthorizationFilter (@PreAuthorize check)                │
│         ↓                                                   │
│  @PreAuthorize("hasRole('STUDENT')")                      │
│    ├─ User role = STUDENT? → ALLOW                         │
│    ├─ User role = ADMIN? → DENY (403)                      │
│    └─ No session? → DENY (401)                             │
│         ↓                                                   │
│  RequestDispatcher → Controller                            │
│         ↓                                                   │
│  SecurityContextHolder.getAuthentication()                 │
│    └─ Get current user (CustomUserDetails)                 │
│         ↓                                                   │
│  Method Execution                                          │
│    └─ business logic with authenticated user               │
│         ↓                                                   │
│  Response (200 OK or error)                                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## Database Schema

```
┌──────────────────────────┐         ┌────────────────────────────┐
│        USERS TABLE       │         │    COMPLAINTS TABLE        │
├──────────────────────────┤         ├────────────────────────────┤
│ id (PK)                  │         │ id (PK)                    │
│ name                     │         │ title                      │
│ email (UNIQUE)           │◄────────│ description                │
│ password (encrypted)     │         │ category                   │
│ role (STUDENT/ADMIN)     │         │ status (SUBMITTED...)      │
│                          │         │ created_at                 │
│ STUDENT                  │         │ updated_at                 │
│ ├─ alice@ex.com          │         │ user_id (FK→users.id)      │
│ ├─ bob@ex.com            │         │                            │
│ ADMIN                    │         │ Complaint Statuses:        │
│ └─ admin@ex.com          │         │ ├─ SUBMITTED               │
│                          │         │ ├─ IN_REVIEW               │
│                          │         │ └─ RESOLVED                │
└──────────────────────────┘         └────────────────────────────┘
```

## Role-Based Access Control Matrix

```
┌────────────────────────────────────────────────────────────────┐
│ ENDPOINT                  │ STUDENT  │ ADMIN    │ ANONYMOUS    │
├────────────────────────────────────────────────────────────────┤
│ POST /api/auth/register   │    ✓     │    ✓     │      ✓       │
│ POST /api/auth/login      │    ✓     │    ✓     │      ✓       │
│ GET /api/auth/user        │    ✓     │    ✓     │      ✗       │
│ POST /api/auth/logout     │    ✓     │    ✓     │      ✗       │
├────────────────────────────────────────────────────────────────┤
│ POST /api/complaints      │    ✓     │    ✗     │      ✗       │
│ GET /api/complaints       │    ✗     │    ✓     │      ✗       │
│ GET /api/complaints/user  │    ✓     │    ✗     │      ✗       │
│ PUT /api/complaints/{id}  │    ✗     │    ✓     │      ✗       │
│ DELETE /api/complaints/{id}│    ✗     │    ✓     │      ✗       │
├────────────────────────────────────────────────────────────────┤
│ GET /api/students         │    ✗     │    ✓     │      ✗       │
│ GET /api/admins           │    ✗     │    ✓     │      ✗       │
│ POST /api/admins          │    ✗     │    ✓     │      ✗       │
└────────────────────────────────────────────────────────────────┘

Legend:
✓ = Access Allowed
✗ = Access Denied
```

## Session Lifecycle

```
1. Login
   ├─ POST /api/auth/login (credentials)
   ├─ Server validates credentials
   ├─ Server creates session (JSESSIONID)
   └─ Server sends session cookie to browser

2. Authenticated Request
   ├─ Browser includes JSESSIONID cookie
   ├─ Server validates session exists
   ├─ Server loads user from session
   ├─ Request is processed with user context
   └─ Response sent

3. Logout
   ├─ POST /api/auth/logout
   ├─ Server invalidates session
   ├─ SecurityContext is cleared
   ├─ Session cookie is expired
   └─ Browser removes JSESSIONID

4. Access After Logout
   ├─ Browser doesn't include JSESSIONID (expired)
   ├─ Server can't load user from session
   ├─ Request treated as anonymous
   └─ Access denied to protected endpoints
```

## Security Layers

```
┌────────────────────────────────────────────────────────────┐
│ Layer 1: HTTP/HTTPS Transport                             │
│ └─ Encrypted communication (HTTPS recommended)            │
├────────────────────────────────────────────────────────────┤
│ Layer 2: CORS & Headers                                   │
│ └─ Origin validation, Security headers                    │
├────────────────────────────────────────────────────────────┤
│ Layer 3: Authentication                                   │
│ ├─ BCrypt password hashing                                │
│ ├─ Credential validation                                  │
│ └─ Session management                                     │
├────────────────────────────────────────────────────────────┤
│ Layer 4: Authorization                                    │
│ ├─ Role-based access control (@PreAuthorize)             │
│ ├─ Endpoint protection                                    │
│ └─ Business logic isolation                               │
├────────────────────────────────────────────────────────────┤
│ Layer 5: Data Isolation                                   │
│ ├─ Students see only own data                             │
│ ├─ Admins access all data                                 │
│ └─ Database constraints (FK, UNIQUE)                      │
└────────────────────────────────────────────────────────────┘
```

## Tech Stack Diagram

```
Frontend Layer
├─ React 19          (UI Framework)
├─ React Router      (Navigation)
├─ Bootstrap 5       (Styling)
└─ Axios             (HTTP Client)

├─ ↓↑ (HTTPS/Cookies)

Backend Layer
├─ Spring Boot 4.0.2  (Framework)
├─ Spring Security    (Authentication & Authorization)
├─ Spring Data JPA    (ORM)
└─ Lombok             (Code Generation)

├─ ↓↑ (JDBC)

Data Layer
└─ MySQL 5.7+        (Database)
```

---

**Architecture Diagram Created**: March 2, 2026
**System**: Student Complaint System v1.0
