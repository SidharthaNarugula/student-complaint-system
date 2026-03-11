# Spring Security Implementation - Student Complaint System

## Overview
This document summarizes the implementation of Spring Security with session-based authentication for the Student Complaint System.

## What Was Implemented

### 1. Backend Changes

#### New Models
- **User.java**: Replaces separate Admin and Student entities
  - id, name, email (unique), password (BCrypt), role (Enum)
  - UserRole enum: STUDENT, ADMIN

- **UserRole.java**: Enum with STUDENT and ADMIN values

#### Updated Models
- **Complaint.java**: Changed from `Student` to `User` relationship
  - Foreign key: user_id instead of student_id

#### New Security Classes
- **CustomUserDetails.java**: Implements UserDetails interface
  - Loads user information and converts role to authority
  - Format: ROLE_STUDENT or ROLE_ADMIN

- **CustomUserDetailsService.java**: Implements UserDetailsService
  - Loads user by email from database
  - Used by Spring Security for authentication

#### Security Configuration
- **SecurityConfig.java**: Spring Security configuration
  - PasswordEncoder: BCryptPasswordEncoder
  - Form-based login/logout
  - Role-based authorization using @PreAuthorize
  - CSRF disabled (for API usage)
  - Session-based authentication

#### New DTOs
- **LoginRequest.java**: Email and password for login
- **RegisterRequest.java**: Name, email, password for registration

#### New/Updated Controllers
- **AuthController.java**: NEW - Handles authentication
  - POST /api/auth/register - Register as STUDENT
  - POST /api/auth/login - Login (email + password)
  - GET /api/auth/user - Get current logged-in user
  - POST /api/auth/logout - Logout

- **ComplaintController.java**: Updated
  - POST /api/complaints - Only STUDENT role (auto-assigns current user)
  - GET /api/complaints - Only ADMIN role (all complaints)
  - GET /api/complaints/user - Only STUDENT role (user's own complaints)
  - PUT /api/complaints/{id} - Only ADMIN role (update status)
  - DELETE /api/complaints/{id} - Only ADMIN role

- **StudentController.java**: Updated
  - Simplified to use User model instead of Student
  - GET /api/students - All students (ADMIN only)
  - GET /api/students/{id} - Student details

- **AdminController.java**: Updated
  - Now works with User model
  - POST /api/admins - Create admin (ADMIN only)
  - All endpoints require ADMIN role

#### Updated Services
- **ComplaintService.java**: Added method
  - getComplaintsByUser(User user) - Fetch only user's complaints

#### New Repository
- **UserRepository.java**: JPA repository for User
  - Custom method: findByEmail(String email)

#### Updated Repository
- **ComplaintRepository.java**: Added method
  - findByUser(User user)

### 2. Frontend Changes

#### Updated Pages
- **Login.jsx**: Complete rewrite
  - Two tabs: Login and Register
  - No role dropdown (role is determined by authentication)
  - Registration only creates STUDENT accounts
  - Login with email and password
  - Redirects to /admin or /student based on user role

- **StudentDashboard.jsx**: Updated
  - Authentication check using /api/auth/user
  - Shows only logged-in student's complaints
  - Displays student name and email
  - Logout functionality

- **AdminDashboard.jsx**: Updated
  - Authentication and role verification
  - Redirects non-admin to login
  - Fetches all complaints
  - Admin can update complaint status
  - Logout functionality

#### Updated Components
- **Navbar.jsx**: Updated
  - Accepts onLogout callback
  - Displays user name
  - Proper logout implementation

- **ComplaintForm.jsx**: Updated
  - Removed hardcoded studentId parameter
  - User is auto-assigned by backend based on authentication

- **ComplaintList.jsx**: Already had onComplaintUpdated callback support

### 3. Dependencies
Added to pom.xml:
- spring-boot-starter-security

### 4. Application Configuration
- **CompSystenApplication.java**: Updated
  - Added @EnableMethodSecurity for @PreAuthorize support

## Authentication Flow

### Student Registration
```
1. User enters: Name, Email, Password
2. POST /api/auth/register
3. Backend: Creates User with role=STUDENT, password is BCrypt encoded
4. Response: Success message
5. User redirected to login
```

### Student Login
```
1. User enters: Email, Password
2. POST /api/auth/login
3. Backend: Validates credentials using CustomUserDetailsService
4. Backend: Creates session and returns user role
5. Frontend: Redirects to /student based on role
```

### Admin Login
```
1. Same flow as student
2. Admin user must exist in database with role=ADMIN
3. Frontend: Redirects to /admin based on role
```

### Accessing Protected Resources
```
1. GET /api/complaints/user (STUDENT only)
   - Returns only complaints by logged-in student
2. GET /api/complaints (ADMIN only)
   - Returns all complaints
3. PUT /api/complaints/{id} (ADMIN only)
   - Updates complaint status
```

## Database Changes

### User Table (replaces Admin and Student)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### Complaint Table (updated)
```sql
ALTER TABLE complaints CHANGE COLUMN student_id user_id BIGINT NOT NULL;
ALTER TABLE complaints ADD FOREIGN KEY (user_id) REFERENCES users(id);
```

## Key Security Features

1. **Password Encryption**: BCryptPasswordEncoder (strength 10)
2. **Session-based**: Sessions managed by Spring Security
3. **CSRF Protection**: Disabled for API (can be re-enabled for form-based)
4. **Role-based Access**: @PreAuthorize annotations
5. **User Isolation**: 
   - Students see only their own complaints
   - Admins see all complaints
6. **Authentication Check**: Frontend verifies user on page load

## Testing the System

### Create Admin User (Database)
```sql
INSERT INTO users (name, email, password, role) VALUES 
('Admin User', 'admin@example.com', '$2a$10$...BCryptHashOfPassword...', 'ADMIN');
```

### Register Student
```
POST http://localhost:8080/api/auth/register
{
  "name": "John Student",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login as Student
```
POST http://localhost:8080/api/auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
Response: { role: "STUDENT", ... } → Frontend redirects to /student
```

### Login as Admin
```
POST http://localhost:8080/api/auth/login
{
  "email": "admin@example.com",
  "password": "password123"
}
Response: { role: "ADMIN", ... } → Frontend redirects to /admin
```

## Important Notes

1. **API is REST-based**: Not a traditional form-based app
2. **Credentials sent with requests**: Frontend sends cookies automatically with credentials: true in axios
3. **Session cookie**: JSESSIONID managed by Spring
4. **Admin accounts**: Must be created directly in database (no admin registration endpoint)
5. **Password requirements**: Min 6 characters for registration
6. **Email unique**: Each user must have unique email

## File Structure

### Backend
```
src/main/java/com/example/compsysten/
├── config/
│   ├── CorsConfig.java (updated)
│   └── SecurityConfig.java (new)
├── controller/
│   ├── AdminController.java (updated)
│   ├── AuthController.java (new)
│   ├── ComplaintController.java (updated)
│   ├── StudentController.java (updated)
│   └── WelcomeController.java (updated)
├── dto/
│   ├── LoginRequest.java (new)
│   └── RegisterRequest.java (new)
├── model/
│   ├── Complaint.java (updated)
│   ├── User.java (new)
│   └── UserRole.java (new)
├── repository/
│   ├── ComplaintRepository.java (updated)
│   └── UserRepository.java (new)
├── security/
│   ├── CustomUserDetails.java (new)
│   └── CustomUserDetailsService.java (new)
├── service/
│   └── ComplaintService.java (updated)
└── CompSystenApplication.java (updated)
```

### Frontend
```
src/
├── pages/
│   ├── AdminDashboard.jsx (updated)
│   ├── Login.jsx (rewritten)
│   └── StudentDashboard.jsx (updated)
├── components/
│   ├── ComplaintForm.jsx (updated)
│   ├── ComplaintList.jsx (unchanged - works with callback)
│   └── Navbar.jsx (updated)
└── services/
    └── api.js (unchanged)
```

## Next Steps (Optional Enhancements)

1. Add JWT tokens (if moving to micro-services)
2. Add department field to User model
3. Add complaint filtering by category
4. Add email notifications
5. Add audit logging
6. Add two-factor authentication
7. Implement password reset functionality

---
Implementation completed: March 2, 2026
