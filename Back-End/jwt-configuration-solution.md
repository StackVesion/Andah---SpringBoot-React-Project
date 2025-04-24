# JWT Configuration Solution for Andah Microservices

This guide provides a comprehensive solution to fix your authentication issues across all microservices.

## 1. Sync JWT Secret Across All Services

Add the same JWT secret to all service application.properties files:

```properties
# For all services including api-gateway, user-service, etc.
app.jwt.secret=andahSecretKey123ForAllServices
app.jwt.expiration=86400000
```

## 2. Update API Gateway SecurityConfig

Modify your API Gateway's `SecurityConfig.java`:

```java
@Value("${app.jwt.secret}")
private String jwtSecret;

@Bean
public ReactiveJwtDecoder jwtDecoder() {
    // Create a JWT decoder with the same secret the user-service uses
    return NimbusReactiveJwtDecoder.withSecretKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .build();
}

@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        // ... your existing config ...
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
        );
        
    return http.build();
}
```

## 3. Fix Feign Client Authentication

Create a FeignClientConfig in each service that needs to call other services:

```java
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
            }
        };
    }
}
```

Configure your Feign clients to use this configuration:

```java
@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserServiceClient {
    // Your methods here
}
```

## 4. Create a JWT Propagation Filter for API Gateway

Create a filter to properly forward tokens to downstream services:

```java
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
        return -10; // Run before other filters
    }
}
```

## 5. Update Authentication Classes in User Service

Ensure your JWT generation in the AuthService uses the exact same secret and format:

```java
@Service
public class AuthService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;
    
    // Use this for token generation
    public String generateToken(UserDetails userDetails) {
        byte[] keyBytes = jwtSecret.getBytes();
        Key key = Keys.hmacShaKeyFor(keyBytes);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
                
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }
}
```

## 6. Testing Your Fix

After implementing these changes, restart all services and test with Postman:

1. Register a user
2. Log in with the same credentials
3. Use the token for protected endpoints
4. Test service-to-service communication

The token should now be correctly validated by all services, and services should be able to make authenticated calls to each other.
```
