# 403 Error After Backend Restart - QUICK FIX

## Problem
After backend restarted, you're getting 403 Forbidden when trying to file a complaint, even though it worked before and complaints are in the database.

## Root Cause
Browser session was cleared when backend restarted. The old localStorage user data is no longer valid because the backend session is new.

## Immediate Fix - 3 Steps

### Step 1: Clear Browser Storage
1. Open DevTools: Press `F12`
2. Go to **Application** tab (or **Storage** in Firefox)
3. Click on **Local Storage** on the left
4. Find and click `http://localhost:3000`
5. Delete all entries
6. Close DevTools

### Step 2: Refresh Page
- Press `F5` or `Ctrl+R` to refresh
- You should be redirected to login page

### Step 3: Login Again
1. Register a new account OR use existing credentials
2. Login
3. File a complaint
4. ✅ It should work now!

## Why This Happened

1. Backend was running with an active session
2. You successfully filed a complaint and it was saved to database
3. Backend code was modified (SecurityConfig changes)
4. Backend restarted due to file changes (Spring DevTools auto-restart)
5. Old session cookie became invalid
6. localStorage still had old user data
7. When you tried to file a complaint, backend couldn't find the session
8. 403 Forbidden error

## Now It Should Work

Backend is running and ready. Just:
1. ✅ Clear browser storage (F12 → Application → Local Storage)
2. ✅ Refresh the page
3. ✅ Login again
4. ✅ File a complaint

## If It Still Doesn't Work

### Check Backend Logs
Look for messages like:
```
POST /api/complaints - Authentication: ...
User authenticated: test@example.com (ID: 1)
Complaint created successfully - ID: 1
```

### Use Debug Endpoint
Open browser console (F12) and run:
```javascript
fetch('http://localhost:8080/api/auth/debug', {
    credentials: 'include'
}).then(r => r.json()).then(d => {
    console.log('Auth Status:', d);
    if (d.authenticated) {
        console.log('✓ Authenticated as:', d.email);
    } else {
        console.log('✗ Not authenticated - need to login again');
    }
});
```

### Check Cookies
1. Press F12 → Application → Cookies
2. Look for `JSESSIONID` cookie for `localhost:8080`
3. If not present, login again to create it

## Permanent Fix (Optional)

If you want auto-restart to NOT happen, disable Spring DevTools:
Edit `pom.xml` and comment out or remove the spring-boot-devtools dependency.

For now, just follow the quick fix above and you're good to go!

## Status: ✅ READY TO TEST
Backend is running and fresh. Follow the 3 steps above to get back to working state.

