# Fix: Invalid Email/Password Error After Registration

## Problem
After registering a user with email and password, attempting to login with the same credentials resulted in "Invalid email or password" error.

## Root Cause
The issue was in **SecurityConfig.java** - the AuthenticationManager bean was being configured incorrectly:

### Original problematic code:
```java
@Bean
public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder.build();
}
```

**Why this was problematic:**
1. The AuthenticationManager was dependent on HttpSecurity, creating a circular dependency
2. In Spring Security 6.x (used by Spring Boot 4.0.2), this approach doesn't properly initialize the password encoder
3. The password encoder was not being properly applied during authentication, causing password verification to fail

## Solution
Refactored the authentication configuration to use `DaoAuthenticationProvider` with `AuthenticationConfiguration`:

### Updated code:
```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}

@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.disable())
        .csrf(csrf -> csrf.disable())
        .authenticationProvider(authenticationProvider())  // Register the provider
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/", "/api/auth/register", "/api/auth/login").permitAll()
            .requestMatchers("/student/**", "/api/complaints/user/**").hasRole("STUDENT")
            .requestMatchers("/admin/**", "/api/admins/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
    return http.build();
}
```

## What Changed
1. **Created DaoAuthenticationProvider bean**: Explicitly configures the DAO authentication provider with the UserDetailsService and PasswordEncoder
2. **Fixed AuthenticationManager bean**: Now uses AuthenticationConfiguration instead of HttpSecurity, which is the proper way in Spring Security 6.x
3. **Registered the authentication provider**: Added `.authenticationProvider(authenticationProvider())` in the SecurityFilterChain to ensure it's used
4. **Removed form login configuration**: Since this is a REST API, form login configuration is not needed

## Testing the Fix
After rebuilding the project:

1. **Register a new user:**
   - Navigate to the registration form
   - Enter name, email, and password
   - Submit the registration

2. **Login with the same credentials:**
   - Enter the email and password you just registered
   - Click Login
   - You should now be successfully authenticated

## Why This Works
- The password entered during registration is encoded using BCryptPasswordEncoder
- The password entered during login is now properly verified against the encoded password using the same BCryptPasswordEncoder
- The DaoAuthenticationProvider ensures password comparison happens correctly during authentication

## Files Modified
- `src/main/java/com/example/compsysten/config/SecurityConfig.java`

