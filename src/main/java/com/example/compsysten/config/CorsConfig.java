package com.example.compsysten.config;

import org.springframework.context.annotation.Configuration;

/**
 * Disabled - CORS is now configured in SecurityConfig.java
 * Having multiple CORS configurations can cause conflicts
 */
@Deprecated
@Configuration
public class CorsConfig {
    // Disabled to avoid CORS configuration conflicts
    // CORS is now handled by SecurityConfig.corsConfigurationSource()
}

