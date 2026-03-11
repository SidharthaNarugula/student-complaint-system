# Quick Start - Test Complaint Submission

## Step 1: Stop Current Processes
- Stop the backend (Ctrl+C in the Maven terminal)
- Stop the frontend (Ctrl+C in the npm terminal)

## Step 2: Rebuild Backend
```bash
cd "C:\Users\Sidharth\IdeaProjects\CompSysten"
.\mvnw.cmd clean package -DskipTests
```

Wait for the build to complete. You should see:
```
[INFO] BUILD SUCCESS
```

## Step 3: Start Backend
```bash
.\mvnw.cmd spring-boot:run
```

Wait until you see:
```
Started CompSystenApplication in X seconds
```

## Step 4: Start Frontend (in a new terminal)
```bash
cd "C:\Users\Sidharth\IdeaProjects\CompSysten\complaint-frontend"
npm start
```

The browser should open automatically at `http://localhost:3000`

## Step 5: Test the Flow

### Login:
1. Enter email: `test@example.com` (or your registered email)
2. Enter password: `password123` (or your registered password)
3. Click Login
4. You should see StudentDashboard

### File a Complaint:
1. In the "File a Complaint" section, fill in:
   - **Title**: "Complaint Title"
   - **Description**: "Detailed complaint description"
   - **Category**: "Infrastructure"
2. Click "Submit Complaint"
3. ✅ You should see: "Complaint filed successfully!"
4. Complaint should appear below in "My Complaints"

### Admin View (if you have admin account):
1. Logout (click Logout button)
2. Login with admin credentials
3. You should see Admin Dashboard
4. All student complaints should be visible

## If It Still Doesn't Work

### Check Browser Console (F12):
- Look for CORS errors
- Should NOT see "Access-Control-Allow-Origin" error

### Check Terminal Output:
- Backend should show: `[POST] /api/complaints` being called
- No 403 Forbidden errors

### Common Issues:

**Issue**: Still getting CORS error
- **Solution**: Make sure backend was rebuilt after SecurityConfig changes
- **Action**: Run `.\mvnw.cmd clean compile` to rebuild

**Issue**: Session not being recognized
- **Solution**: Frontend needs to send credentials
- **Check**: api.js has `withCredentials: true`

**Issue**: "Access Denied" error (403)
- **Solution**: User might not have STUDENT role
- **Check**: Login with a registered student account, not admin

## Expected Behavior After Fix

✅ Login works
✅ Dashboard loads immediately
✅ Complaint form is visible
✅ Can submit complaints without CORS error
✅ Complaint appears in list
✅ Can see success message
✅ Can logout and login again

