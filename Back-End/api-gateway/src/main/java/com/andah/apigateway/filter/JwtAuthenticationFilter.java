package com.andah.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private final List<String> openApiPaths = List.of(
        // User service authentication endpoints
        "/api/auth/",
        "/user-service/api/auth/",
        // Reclamation public endpoints
        "/api/reclamations/public/",
        "/reclamation-service/api/reclamations/public/",
        // Actuator endpoints
        "/actuator/"
    );
    
    // Pattern for Swagger UI and API docs
    private final Pattern swaggerPattern = Pattern.compile("^/(swagger-ui\\.html|swagger-ui/.*|v3/api-docs/.*)$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Forward any Authorization header to downstream services
        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            // We don't need to validate the token here, just pass it on
            return chain.filter(exchange);
        }
        
        // Allow open paths without authentication
        final String path = request.getURI().getPath();
        
        if (isOpenPath(path) || swaggerPattern.matcher(path).matches()) {
            return chain.filter(exchange);
        }
        
        // For other paths, let Spring Security handle authorization
        return chain.filter(exchange);
    }
    
    private boolean isOpenPath(String path) {
        return openApiPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}
