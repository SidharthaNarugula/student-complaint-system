# Student Complaint Management System — Project Overview

---

## 1. Project Overview

A full-stack web application that allows university students to file and track complaints, and allows administrators to review and manage those complaints through a defined lifecycle. The backend is a Spring Boot REST API; the frontend is a React SPA.

**Key purpose:** Give students a structured channel to raise issues, and give admins a controlled workflow to resolve them — with strict status-transition rules enforced on the server.

---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend Framework | Spring Boot 4.0.2 |
| Security | Spring Security 6 (session-based, BCrypt) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Build Tool | Maven |
| Frontend | React (JSX), plain CSS |
| Dev Tooling | Spring DevTools, Lombok |

---

## 3. Architecture

```
HTTP Request
    │
    ▼
Controller Layer  (/api/auth, /api/complaints)
    │  — validates input (@Valid), checks auth principal, enforces role guard
    ▼
Service Layer  (ComplaintService, StudentService, AdminService)
    │  — business logic, state-machine validation, logging
    ▼
Repository Layer  (Spring Data JPA interfaces)
    │  — CRUD against MySQL via JPA
    ▼
Model Layer  (User, Complaint, Student, Admin, enums)
```

- **Controller** is thin: it only extracts the authenticated user from the `SecurityContext`, maps the request to a domain object, and delegates to the service.
- **Service** owns all business rules: creating complaints, enforcing status transitions, basic user existence checks.
- **Repository** is pure Spring Data — no custom queries beyond `findByUser` on `ComplaintRepository`.
- **Model** is plain JPA entities; no Lombok (getters/setters written manually).

---

## 4. Core Features

- **Complaint creation** — Authenticated students can POST a new complaint (title, description, category). The service automatically sets status to `OPEN` and timestamps it.
- **Complaint tracking** — Students see only their own complaints (`GET /api/complaints/user`). Admins see all complaints (`GET /api/complaints`).
- **Role-based access** — Two roles: `STUDENT` and `ADMIN`. Enforced at the `SecurityConfig` level and reinforced inside controllers with explicit role checks and `@PreAuthorize`.
- **Complaint lifecycle** — Admins drive complaints through a strict four-step status machine (see section 5).
- **Authentication** — Session-based login via `POST /api/auth/login`. BCrypt password hashing on registration. Self-registration is `STUDENT`-only; admins must be seeded directly in the DB.

---

## 5. Complaint Lifecycle

### States

```
OPEN  →  IN_PROGRESS  →  RESOLVED  →  CLOSED
```

`CLOSED` is a terminal state — no further transitions are allowed.

### Transition Table

| Current State | Allowed Next State |
|---|---|
| `OPEN` | `IN_PROGRESS` |
| `IN_PROGRESS` | `RESOLVED` |
| `RESOLVED` | `CLOSED` |
| `CLOSED` | *(none — terminal)* |

### How validation works

1. `ComplaintStatus` is an **enum with abstract methods**. Each constant overrides `allowedNextStates()` returning an `EnumSet` of valid next states.
2. `canTransitionTo(ComplaintStatus next)` calls `allowedNextStates().contains(next)` — a single-line boolean check baked into the enum itself.
3. `ComplaintService.updateComplaintStatus()` fetches the complaint, calls `current.canTransitionTo(newStatus)`, and throws `InvalidStatusTransitionException` if it returns false.
4. The status is stored as a `STRING` column in MySQL (`@Enumerated(EnumType.STRING)`), so values are human-readable in the database.

### On invalid transitions

- Service throws `InvalidStatusTransitionException` (a `RuntimeException`) with a message that includes the current state and the disallowed target.
- `GlobalExceptionHandler` catches it and returns **HTTP 409 Conflict** with a structured JSON body (`timestamp`, `status`, `error`, `message`).
- Backward transitions (e.g., `RESOLVED → OPEN`) and state-skipping (e.g., `OPEN → RESOLVED`) are both rejected by the same path.

---

## 6. API Design

### Auth endpoints (`/api/auth`)

| Method | Path | Who | What it does |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Creates a new `STUDENT` user. Rejects duplicate emails with 400. |
| `POST` | `/api/auth/login` | Public | Authenticates via Spring Security, sets session cookie, returns user info + role. |
| `GET` | `/api/auth/user` | Authenticated | Returns the current user's info from the session. |
| `POST` | `/api/auth/logout` | Authenticated | Clears the `SecurityContext`. |

### Complaint endpoints (`/api/complaints`)

| Method | Path | Who | What it does |
|---|---|---|---|
| `POST` | `/api/complaints` | `STUDENT` | Creates a new complaint. Sets status to `OPEN`. Returns `201 Created`. |
| `GET` | `/api/complaints` | `ADMIN` | Returns all complaints as `ComplaintResponseDTO` (includes submitter name/email). |
| `GET` | `/api/complaints/user` | `STUDENT` | Returns only the authenticated student's complaints. |
| `GET` | `/api/complaints/{id}` | Authenticated | Returns a single complaint by ID. |
| `PATCH` | `/api/complaints/{id}/status` | `ADMIN` | Advances complaint status by one step. Enforces state machine. Returns `200` with updated DTO or `409` on bad transition. |
| `PUT` | `/api/complaints/{id}` | `ADMIN` | Full update of a complaint's fields (title, description, category, status). |
| `DELETE` | `/api/complaints/{id}` | `ADMIN` | Deletes a complaint. Returns `204 No Content`. |

---

## 7. Validation & Error Handling

### Input validation

- `ComplaintDTO` uses `@NotBlank` on `title`, `description`, `category` — all required.
- `StatusUpdateRequest` uses `@NotNull` on `status` — prevents null status from reaching the service.
- `RegisterRequest` / `LoginRequest` use `@NotBlank` and `@Email`.
- Controllers annotate inputs with `@Valid`, triggering Bean Validation before the method body runs.

### Error responses

All error responses are structured JSON with `timestamp`, `status`, `error`, `message`.

| Scenario | HTTP Status |
|---|---|
| Complaint not found by ID | `404 Not Found` |
| Invalid status transition | `409 Conflict` |
| Bean Validation failure (`@NotBlank`, etc.) | `400 Bad Request` |
| User not authenticated | `401 Unauthorized` |
| Authenticated but wrong role | `403 Forbidden` |

`GlobalExceptionHandler` (`@RestControllerAdvice`) handles:
- `ComplaintNotFoundException` → 404
- `InvalidStatusTransitionException` → 409
- `MethodArgumentNotValidException` → 400 (field-level messages aggregated into one string)

---

## 8. Security

- **Session-based authentication** — Spring Security's built-in session management (`IF_REQUIRED`). No JWT.
- **Password hashing** — BCrypt via `BCryptPasswordEncoder`. Passwords are never stored in plaintext.
- **CORS** — Configured explicitly to allow only `http://localhost:3000` with credentials.
- **CSRF** — Disabled (typical for REST APIs consumed by a same-origin frontend controlled by the developer).
- **Role enforcement:**
  - `@PreAuthorize("hasRole('ADMIN')")` on GET all, PATCH status, PUT, DELETE complaint endpoints.
  - Explicit `user.getRole()` check inside the complaint creation and user-complaint-fetch handlers, returning `403` if a non-student tries to use those paths.
  - Registration always assigns `STUDENT` role — admins cannot be created via the API.
- **`CustomUserDetailsService`** — loads the `User` entity from the DB by email and wraps it in `CustomUserDetails`, which exposes the `User` object directly to controllers via the `SecurityContext`.

---

## 9. Logging

SLF4J (backed by Logback, Spring Boot default) is used in `ComplaintService` and `ComplaintController`.

What gets logged:

| Event | Level |
|---|---|
| Complaint created (id, title, user email) | `INFO` |
| Status transition succeeded (id, old → new) | `INFO` |
| Status transition failed (id, current, requested, allowed) | `WARN` |
| Complaint not found during status update | `WARN` |
| Authentication failure or invalid principal type | `WARN` |
| Role mismatch (e.g., admin trying to file complaint) | `WARN` |
| Unexpected exceptions | `ERROR` |

No log aggregation or structured logging (e.g., JSON format) is configured.

---

## 10. Key Engineering Decisions

**Enum instead of String for status**
The `ComplaintStatus` enum carries its own transition logic (`allowedNextStates()`, `canTransitionTo()`). This means the state machine is co-located with the states themselves — adding a new state means updating the enum, not hunting for string comparisons across the codebase. Stored as `STRING` in the DB so migrations remain readable.

**Service layer owns transition validation**
The controller doesn't know what transitions are valid. It hands the request to `ComplaintService`, which delegates to the enum. This keeps the controller thin and makes the business rule testable in isolation.

**DTOs for request/response**
`ComplaintDTO` decouples what the client sends from the internal `Complaint` entity. This prevents accidental exposure of internal fields (e.g., `user`, `createdAt`) and allows Bean Validation annotations to live on the DTO rather than the entity. `ComplaintResponseDTO` controls exactly what structure the client receives, including denormalized `submitterName` and `submitterEmail` fields so the frontend doesn't need a second request.

---

## 11. Limitations

- **No JWT** — Authentication is session-based. Stateless or mobile clients can't use the API easily.
- **No admin self-registration** — Admins must be inserted directly into the database. There's no internal endpoint to promote a user or create an admin account.
- **No pagination** — `GET /api/complaints` returns all records. Will be a problem at scale.
- **No email notifications** — Status changes are not communicated to the student outside the dashboard.
- **No file attachments** — Students can't attach evidence to a complaint.
- **Frontend has no routing guard** — Role-based page protection relies on the backend; the React SPA doesn't enforce it client-side.
- **`Student` and `Admin` entities are unused in the complaint flow** — The `Complaint` entity links to `User`, not `Student`. The separate `Student` and `Admin` tables appear to be early-design artifacts that were superseded by `User + UserRole`.
- **No automated tests** — The test directory contains only the default Spring Boot application context test.
