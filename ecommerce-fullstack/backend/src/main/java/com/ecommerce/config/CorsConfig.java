package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Set CORS_ORIGINS to a comma-separated list of frontend URLs (default: allow all). */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${CORS_ORIGINS:*}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origins.split("\\s*,\\s*"))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
