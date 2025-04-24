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
public class JwtPropagationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check if the request has an Authorization header
        List<String> authHeaders = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (!authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            
            // Forward the exact same header to the downstream service
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .build();
                
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -10; // Run before other filters but after JwtAuthenticationFilter
    }
}
