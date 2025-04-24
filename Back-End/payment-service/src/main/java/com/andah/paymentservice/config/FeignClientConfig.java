package com.andah.paymentservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> {
            // Get the security context from the thread that's making the request
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getCredentials();
                // Add the bearer token to the request
                requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
            } else if (authentication != null && authentication.getPrincipal() != null) {
                // Fallback for other authentication types
                requestTemplate.header("X-User-Id", authentication.getName());
            }
        };
    }
}
