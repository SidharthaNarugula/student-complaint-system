# Quick Test Guide - 403 Fix

## Prerequisites
- Backend rebuilt with latest changes
- Frontend running
- Student account created

## Test Steps

### 1. Start the Application
```bash
# Terminal 1: Backend
cd "C:\Users\Sidharth\IdeaProjects\CompSysten"
.\mvnw.cmd spring-boot:run

# Terminal 2: Frontend (wait for backend to start)
cd complaint-frontend
npm start
```

### 2. Login Test
- Navigate to `http://localhost:3000`
- Login with email: `test@example.com` (replace with your student email)
- Password: Your registered password
- ✅ Should see StudentDashboard

### 3. Check Authentication (Optional Debug)
Open browser DevTools (F12) → Console and run:
```javascript
fetch('http://localhost:8080/api/auth/debug', {
    method: 'GET',
    credentials: 'include'
}).then(r => r.json()).then(data => {
    console.log('Auth Debug:', data);
    if (data.authorities && data.authorities.includes('ROLE_STUDENT')) {
        console.log('✓ Has STUDENT role');
    } else {
        console.log('✗ Missing STUDENT role:', data.role);
    }
});
```

### 4. File a Complaint
1. In StudentDashboard, scroll to "File a Complaint" section
2. Fill in:
   - **Title**: "Test Complaint"
   - **Description**: "Testing the complaint submission"
   - **Category**: "Infrastructure"
3. Click "Submit Complaint"
4. ✅ Should see: "Complaint filed successfully!"
5. Complaint appears in "My Complaints" section below

### 5. Verify Complaint was Saved
- Logout
- Login again
- ✅ Complaint should still be visible in "My Complaints"

### 6. Logout Test
- Click Logout button
- ✅ Should redirect to login page
- ✅ localStorage should be cleared

## If Still Getting 403 Error

### Step 1: Check Backend Logs
Look for messages like:
```
[DEBUG] User authenticated: john@example.com
[DEBUG] Role: STUDENT
[DEBUG] Authorities: ROLE_STUDENT
```

If not present, backend isn't recognizing the user.

### Step 2: Verify User in Database
Connect to MySQL and run:
```sql
SELECT id, name, email, role FROM users WHERE email = 'test@example.com';
```

Should show:
```
id  | name  | email            | role
1   | Test  | test@example.com | STUDENT
```

If role is NULL or not STUDENT, re-register the account.

### Step 3: Check Frontend Axios Credentials
Open `complaint-frontend/src/services/api.js` and verify:
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true  // ← MUST be true
});
```

### Step 4: Use Debug Endpoint
From browser console (F12):
```javascript
// Check what authorities the server sees
fetch('http://localhost:8080/api/auth/debug', {
    method: 'GET',
    credentials: 'include'
}).then(r => r.json()).then(d => console.log(d));
```

Output should be:
```json
{
  "authenticated": true,
  "userId": 1,
  "name": "Test User",
  "email": "test@example.com",
  "role": "STUDENT",
  "authorities": ["ROLE_STUDENT"]
}
```

**If you see:**
- `"authenticated": false` → Login session expired or not authenticated
- `"role": "ADMIN"` → Wrong role, need STUDENT account
- `"authorities": []` → Role not being loaded

## Expected Success Output

```
Browser Console:
✓ Request POST /api/complaints → 201 Created

StudentDashboard:
✓ "Complaint filed successfully!" message shown

"My Complaints" list:
✓ New complaint visible with:
  - Your title
  - Your description  
  - Your category
  - Status: SUBMITTED
  - Your name as creator
```

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| 403 Forbidden | Not authenticated | Check `/api/auth/debug` |
| 403 Forbidden | Wrong role (ADMIN instead of STUDENT) | Register new STUDENT account |
| 401 Unauthorized | Session expired | Logout and login again |
| CORS error | withCredentials not set | Check api.js has `withCredentials: true` |
| "Not authenticated" | Backend session cleared | Restart backend or login again |

## Success Indicators

✅ Login works without errors
✅ Dashboard loads immediately after login
✅ Complaint form is visible and enabled
✅ Submit button works
✅ "Complaint filed successfully!" message appears
✅ Complaint appears in My Complaints list
✅ Page refresh shows complaint still exists
✅ Logout works properly

