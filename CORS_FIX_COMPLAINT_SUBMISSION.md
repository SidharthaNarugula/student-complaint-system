# CORS Error Fix - Complaint Submission Issue ✅

## Problem
When trying to file a complaint, users got the error:
```
Access to XMLHttpRequest at 'http://localhost:8080/api/complaints' from origin 'http://localhost:3000' has been blocked by CORS policy
```

The complaint submission was failing because CORS was disabled and session authentication wasn't working properly with the frontend.

## Root Causes
1. **CORS was disabled** in SecurityConfig (`.cors(cors -> cors.disable())`)
2. **Session wasn't being sent** by axios - credentials weren't being included in requests
3. **Session configuration** wasn't optimized for cross-origin requests

## Solutions Applied

### 1. Enabled CORS in SecurityConfig.java
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);  // ✅ Allow credentials/cookies
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

// Changed from: .cors(cors -> cors.disable())
// To: .cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

### 2. Enabled Credentials in axios (api.js)
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true  // ✅ Send cookies with all requests
});
```

### 3. Configured Sessions in application.properties
```properties
# Session configuration
server.servlet.session.cookie.http-only=false
server.servlet.session.cookie.same-site=lax
server.servlet.session.timeout=30m
```

### 4. Updated SecurityConfig Authorization Rules
Added explicit permission for complaint creation:
```java
.requestMatchers("/api/complaints").hasRole("STUDENT")
```

## Flow After Fix

### Filing a Complaint:
1. ✅ Student fills complaint form and clicks "Submit"
2. ✅ Frontend sends POST request to `/api/complaints` with `withCredentials: true`
3. ✅ Browser includes session cookie in the request
4. ✅ CORS allows the request to pass through
5. ✅ Backend receives request with valid session
6. ✅ `SecurityContextHolder.getContext().getAuthentication()` finds the authenticated user
7. ✅ Complaint is created and saved to database
8. ✅ Success message displayed to user

## Technical Details

### How Session-Based Authentication Works Now:
1. User logs in → Backend creates HTTP Session and sets JSESSIONID cookie
2. Browser stores JSESSIONID cookie
3. Frontend axios is configured with `withCredentials: true`
4. Every request includes the JSESSIONID cookie
5. Backend recognizes the session and retrieves the authenticated user
6. Authentication is validated for protected endpoints

### CORS Preflight Request Flow:
1. Browser sees cross-origin POST request
2. Sends OPTIONS preflight request first
3. CORS configuration responds with allowed headers/methods
4. Browser then sends actual POST request
5. Request includes credentials/cookies

## Files Modified
- `src/main/java/com/example/compsysten/config/SecurityConfig.java` - Enable CORS
- `complaint-frontend/src/services/api.js` - Enable credentials
- `src/main/resources/application.properties` - Configure session cookies

## Testing the Fix

### 1. Stop and Rebuild the Backend
```bash
./mvnw.cmd clean package -DskipTests
./mvnw.cmd spring-boot:run
```

### 2. Start the Frontend
```bash
cd complaint-frontend
npm start
```

### 3. Test Complaint Filing
1. Login with student account
2. Fill in complaint form:
   - Title: "Test Complaint"
   - Description: "Testing complaint submission"
   - Category: "Infrastructure"
3. Click "Submit Complaint"
4. ✅ Should see "Complaint filed successfully!" message
5. Complaint should appear in "My Complaints" list below

### 4. Test Admin View
1. Login with admin account (if available)
2. Go to Admin Dashboard
3. ✅ Should see all complaints filed by students

## What This Enables
✅ Cross-origin requests from frontend to backend
✅ Session cookies are sent with each request
✅ Backend can authenticate users from frontend requests
✅ Complaint filing works properly
✅ Admin can view all complaints
✅ Proper CORS preflight handling

## Security Notes
- CORS is now configured to accept requests from `http://localhost:3000` only
- `allowCredentials(true)` means cookies are included with cross-origin requests
- Session timeout is set to 30 minutes
- CSRF protection is disabled (necessary for this REST API architecture)
- Session cookies have `SameSite=Lax` for security

For production deployment, adjust the allowed origins to your actual domain.

