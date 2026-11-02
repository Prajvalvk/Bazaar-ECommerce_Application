package com.ecommerce.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for the React storefront.
 * Always allowed: local dev (localhost:5173) and any Vercel deployment (https://*.vercel.app).
 * Extra origins (for example a custom domain) can be added with CORS_ORIGINS, comma-separated.
 * Wildcards such as https://*.example.com are supported. Trailing slashes are ignored.
 * Auth uses a Bearer token header (not cookies), so credentials are not needed.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${CORS_ORIGINS:}")
    private String extraOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> patterns = new ArrayList<>(List.of("http://localhost:5173", "https://*.vercel.app"));
        for (String o : extraOrigins.split(",")) {
            String origin = o.trim().replaceAll("/+$", "");
            if (!origin.isEmpty() && !patterns.contains(origin)) patterns.add(origin);
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(patterns.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
