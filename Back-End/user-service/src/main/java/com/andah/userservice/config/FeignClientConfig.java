package com.andah.userservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Configuration
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> {
            try {
                // Get the current request context
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    // Get Authorization header from the current request
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && !authHeader.isEmpty()) {
                        // Add the authorization header to the Feign request
                        requestTemplate.header("Authorization", authHeader);
                    }
                }
            } catch (Exception e) {
                // Just log the error, don't break the request
                System.out.println("Error forwarding Authorization header: " + e.getMessage());
            }
        };
    }
}
