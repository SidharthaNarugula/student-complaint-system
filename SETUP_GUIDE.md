# Student Complaint System - Setup Guide

## Overview
A full-stack Spring Boot + React application with Spring Security for managing student complaints with role-based access control (Students and Admins).

## Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+ and npm
- MySQL 5.7+

## Quick Start

### 1. Backend Setup

#### Database Setup
```sql
-- Create database
CREATE DATABASE complaint_tracker;

-- Use database
USE complaint_tracker;

-- Create tables (Spring will auto-create with JPA, but you can pre-create if needed)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create initial admin user (password: "admin123" BCrypt encoded)
INSERT INTO users (name, email, password, role) VALUES 
('System Admin', 'admin@example.com', '$2a$10$slYQmyNdGziq3LEmnH57COO/Ki4ibB3P2dY3Si6bt6mS8qLmy5l6', 'ADMIN');

-- Note: To generate other BCrypt hashes, use:
-- https://www.bcryptcalculator.com/ or Spring Security test tools
```

#### application.properties Update
File: `src/main/resources/application.properties`
```properties
spring.application.name=CompSysten
spring.datasource.url=jdbc:mysql://localhost:3306/complaint_tracker
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

#### Run Backend
```bash
cd C:\Users\Sidharth\IdeaProjects\CompSysten
.\mvnw.cmd spring-boot:run
```

Backend will start at: `http://localhost:8080`

### 2. Frontend Setup

```bash
cd C:\Users\Sidharth\IdeaProjects\CompSysten\complaint-frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend will start at: `http://localhost:3000`

## User Accounts

### Admin Account (Pre-created)
- **Email**: admin@example.com
- **Password**: admin123
- **Role**: ADMIN

### Create New Admin (Optional)
Use database insert:
```sql
INSERT INTO users (name, email, password, role) VALUES 
('Admin Name', 'admin2@example.com', '$2a$10/...', 'ADMIN');
```

### Student Registration
- Use the "Register" button on login page
- Only creates STUDENT role accounts
- Email must be unique

## Features

### Student Features
- ✅ Register with name, email, password
- ✅ Login with email and password
- ✅ File complaints (auto-assigned to logged-in student)
- ✅ View only their own complaints
- ✅ View complaint status

### Admin Features
- ✅ Login with email and password
- ✅ View all complaints from all students
- ✅ Update complaint status (SUBMITTED → IN_REVIEW → RESOLVED)
- ✅ Delete complaints
- ✅ See student information

## API Endpoints

### Authentication
```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET /api/auth/user
```

### Complaints (Authenticated)
```
POST /api/complaints (STUDENT only)
GET /api/complaints (ADMIN only)
GET /api/complaints/user (STUDENT only)
GET /api/complaints/{id}
PUT /api/complaints/{id} (ADMIN only)
DELETE /api/complaints/{id} (ADMIN only)
```

### Users (Authenticated)
```
GET /api/students (ADMIN only)
GET /api/admins (ADMIN only)
POST /api/admins (ADMIN only)
```

## Testing

### Manual Testing with Browser
1. Open `http://localhost:3000`
2. Register as a new student
3. Login with registered credentials
4. File a complaint and view it
5. Logout and login as admin
6. View all complaints and update status

### API Testing with IntelliJ REST Client
Open `complaint-tests.http` file in IntelliJ IDEA and run individual requests.

### API Testing with cURL/PowerShell
```powershell
# Register student
curl -X POST "http://localhost:8080/api/auth/register" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Test Student",
    "email": "test@example.com",
    "password": "password123"
  }'

# Login
curl -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/json" `
  -c cookies.txt `
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Get my complaints
curl -X GET "http://localhost:8080/api/complaints/user" `
  -b cookies.txt
```

## Project Structure

```
CompSysten/
├── src/
│   ├── main/
│   │   ├── java/com/example/compsysten/
│   │   │   ├── config/           # Security and CORS config
│   │   │   ├── controller/       # REST endpoints
│   │   │   ├── dto/              # Request/Response models
│   │   │   ├── model/            # Entity classes
│   │   │   ├── repository/       # Database access
│   │   │   ├── security/         # Authentication classes
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│
├── complaint-frontend/
│   ├── src/
│   │   ├── components/          # Reusable React components
│   │   ├── pages/               # Page components
│   │   ├── services/            # API service
│   │   └── App.js
│   └── package.json
│
├── pom.xml                      # Maven configuration
└── SECURITY_IMPLEMENTATION.md   # Detailed security docs
```

## Key Technology Stack

### Backend
- Spring Boot 4.0.2
- Spring Security
- Spring Data JPA
- MySQL
- Java 17

### Frontend
- React 19
- React Router
- Bootstrap 5
- Axios

## Security Features

1. **Password Encryption**: BCrypt with strength 10
2. **Session-Based Authentication**: Server manages user sessions
3. **Role-Based Access Control**: @PreAuthorize annotations
4. **User Isolation**: 
   - Students see only their complaints
   - Admins see all complaints
5. **CSRF Protection**: Configured in Spring Security
6. **CORS**: Configured for localhost:3000

## Common Issues

### Frontend shows login page indefinitely
- Ensure backend is running on port 8080
- Check browser console for API errors
- Verify CORS settings

### Cannot login
- Check if user exists in database
- Verify password is correct (case-sensitive)
- Check MySQL is running and database exists

### Complaints not saving
- Ensure user is authenticated (session cookie present)
- Check browser Network tab for API response
- Verify complaint table has foreign key to users table

### Database connection error
- Verify MySQL is running
- Check spring.datasource.url in application.properties
- Verify database and credentials match

## Development Notes

- Backend auto-reloads with spring-boot-devtools
- Frontend auto-reloads with React Scripts
- Database tables auto-created on first run (ddl-auto=update)
- Passwords are never returned in API responses
- Email is used as unique identifier for login

## Troubleshooting

### Reset Database
```sql
DROP DATABASE complaint_tracker;
CREATE DATABASE complaint_tracker;
-- Re-run the schema creation steps above
```

### Clear Sessions
Restart the backend application.

### Rebuild Everything
```bash
# Backend
cd CompSysten
mvnw clean install

# Frontend
cd complaint-frontend
rm -rf node_modules package-lock.json
npm install
```

## Next Steps

1. ✅ Implement Spring Security (DONE)
2. Consider adding:
   - Email notifications
   - Department field to users
   - Complaint categories/tags
   - Feedback from admin to students
   - Audit logging
   - Two-factor authentication
   - Password reset functionality
   - Admin user management UI

## Support & Documentation

- Spring Security Docs: https://spring.io/projects/spring-security
- Spring Boot Docs: https://spring.io/projects/spring-boot
- React Docs: https://react.dev
- Bootstrap Docs: https://getbootstrap.com

---

**Last Updated**: March 2, 2026
**Version**: 1.0 with Spring Security

For detailed security implementation details, see: SECURITY_IMPLEMENTATION.md
