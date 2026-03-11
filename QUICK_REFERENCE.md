# Quick Reference Guide

## Running the Application

### Terminal 1 - Backend
```bash
cd C:\Users\Sidharth\IdeaProjects\CompSysten
mvnw spring-boot:run
# Runs on http://localhost:8080
```

### Terminal 2 - Frontend
```bash
cd C:\Users\Sidharth\IdeaProjects\CompSysten\complaint-frontend
npm start
# Runs on http://localhost:3000
```

## Test Users

### Admin (Pre-created in database)
```
Email: admin@example.com
Password: admin123
Role: ADMIN
```

### Create Student (via Register button)
```
Name: Alice Johnson
Email: alice@example.com
Password: password123
Role: STUDENT (auto-assigned)
```

## Key Files Modified

### Backend
- `pom.xml` - Added Spring Security
- `CompSystenApplication.java` - Added @EnableMethodSecurity
- `src/main/java/com/example/compsysten/config/SecurityConfig.java` - NEW
- `src/main/java/com/example/compsysten/security/` - NEW (2 files)
- `src/main/java/com/example/compsysten/dto/` - NEW (2 files)
- `src/main/java/com/example/compsysten/controller/AuthController.java` - NEW
- `src/main/java/com/example/compsysten/repository/UserRepository.java` - NEW
- `src/main/java/com/example/compsysten/model/` - Updated (User.java, UserRole.java)
- Various other controllers and services updated

### Frontend
- `src/pages/Login.jsx` - Rewritten
- `src/pages/StudentDashboard.jsx` - Updated
- `src/pages/AdminDashboard.jsx` - Updated
- `src/components/Navbar.jsx` - Updated
- `src/components/ComplaintForm.jsx` - Updated

## API Quick Reference

### Authentication
```
POST /api/auth/register          → Register as student
POST /api/auth/login             → Login
GET /api/auth/user               → Get current user
POST /api/auth/logout            → Logout
```

### Complaints (Student)
```
POST /api/complaints             → Create complaint
GET /api/complaints/user         → Get my complaints
```

### Complaints (Admin)
```
GET /api/complaints              → Get all complaints
PUT /api/complaints/{id}         → Update complaint status
DELETE /api/complaints/{id}      → Delete complaint
```

## Curl Examples

### Register Student
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Create Complaint (with session)
```bash
curl -X POST http://localhost:8080/api/complaints \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Broken WiFi",
    "description": "WiFi is not working in Block A",
    "category": "Infrastructure"
  }'
```

### Get My Complaints
```bash
curl -X GET http://localhost:8080/api/complaints/user \
  -H "Content-Type: application/json" \
  -b cookies.txt
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Backend won't start | Check if MySQL is running, verify database exists |
| 401 Unauthorized | Login again, session might be expired |
| 403 Forbidden | You don't have permission for this endpoint |
| Email already exists | Register with different email |
| Wrong password | Check caps lock, password is case-sensitive |
| Frontend can't connect | Ensure backend is running on 8080 |
| CORS error | Backend CORS is configured for localhost:3000 |

## Database Setup

```sql
-- Create database
CREATE DATABASE complaint_tracker;

-- Create admin user (password: admin123)
USE complaint_tracker;
INSERT INTO users (name, email, password, role) VALUES 
('System Admin', 'admin@example.com', '$2a$10$slYQmyNdGziq3LEmnH57COO/Ki4ibB3P2dY3Si6bt6mS8qLmy5l6', 'ADMIN');
```

## Create BCrypt Hash

Online tool: https://www.bcryptcalculator.com/

Or use Spring CLI:
```bash
mvnw spring-boot:run -- org.springframework.security.crypto.bcrypt.BCrypt password
```

## Check Backend Status

```bash
curl http://localhost:8080/
```

Should return welcome message with available endpoints.

## Check Frontend

Open browser: `http://localhost:3000`

Should show Login/Register page.

## Production Checklist

- [ ] Change admin password in database
- [ ] Change MySQL root password
- [ ] Enable HTTPS
- [ ] Set environment variables for sensitive data
- [ ] Add rate limiting
- [ ] Enable detailed logging
- [ ] Set up backup strategy
- [ ] Test all user flows
- [ ] Load testing
- [ ] Security scanning

## Important Notes

1. **Passwords are NOT returned** in API responses
2. **Sessions expire** based on server configuration
3. **Admin users** must be created in database
4. **Registration** only creates STUDENT accounts
5. **Email must be unique** across system
6. **Passwords encrypted** with BCrypt strength 10

## Documentation Files

- `SETUP_GUIDE.md` - Installation and setup
- `SECURITY_IMPLEMENTATION.md` - Technical details
- `ARCHITECTURE.md` - System design
- `IMPLEMENTATION_SUMMARY.md` - What was implemented
- `test_api.sh` - API testing scripts
- `complaint-tests.http` - IntelliJ REST client tests

## Git Workflow (if using version control)

```bash
git add .
git commit -m "feat: Implement Spring Security with session-based auth"
git push origin main
```

---

**Quick Reference Version**: 1.0
**Date**: March 2, 2026
**System**: Student Complaint System with Spring Security
