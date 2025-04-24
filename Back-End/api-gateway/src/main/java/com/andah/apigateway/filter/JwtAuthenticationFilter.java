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

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private final List<String> openPaths = List.of(
        "/user-service/api/auth/login",
        "/user-service/api/auth/register",
        "/user-service/api/auth/refresh-token",
        "/user-service/api/auth/check-user",
        "/user-service/api/auth/login-alternative",
        "/user-service/api/auth/hello",
        "/actuator"
    );

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
        if (isOpenPath(path)) {
            return chain.filter(exchange);
        }
        
        // For other paths, let Spring Security handle authorization
        return chain.filter(exchange);
    }
    
    private boolean isOpenPath(String path) {
        return openPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}
