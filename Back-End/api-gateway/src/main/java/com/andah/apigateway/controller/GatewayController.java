package com.andah.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GatewayController {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public GatewayController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // Test endpoint to confirm gateway is working
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "DirectGateway is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // Direct test endpoint for users
    @GetMapping("/test-users")
    public Mono<ResponseEntity<Map<String, Object>>> testUsers() {
        Map<String, Object> dummyUsers = new HashMap<>();
        
        // Add some dummy user data
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user1");
        user1.put("email", "user1@example.com");
        user1.put("firstName", "Test");
        user1.put("lastName", "User");
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "user2");
        user2.put("email", "admin@example.com");
        user2.put("firstName", "Admin");
        user2.put("lastName", "User");
        
        dummyUsers.put("users", Arrays.asList(user1, user2));
        dummyUsers.put("timestamp", System.currentTimeMillis());
        dummyUsers.put("message", "This is dummy user data for testing");
        
        return Mono.just(ResponseEntity.ok(dummyUsers));
    }

    // ====== USER SERVICE ENDPOINTS ======
    
    // Public - Login a user
    @PostMapping("/auth/login")
    public Mono<ResponseEntity<String>> login(@RequestBody String loginRequest) {
        return webClientBuilder.build()
            .post()
            .uri("http://user-service:8083/api/auth/login")
            .bodyValue(loginRequest)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }
    
    // Public - Register a user
    @PostMapping("/auth/register")
    public Mono<ResponseEntity<String>> register(@RequestBody String registerRequest) {
        return webClientBuilder.build()
            .post()
            .uri("http://user-service:8083/api/auth/register")
            .bodyValue(registerRequest)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // Get all users - direct admin access
    @GetMapping("/users")
    public Mono<ResponseEntity<String>> getAllUsers() {
        return webClientBuilder.build()
            .get()
            .uri("http://user-service:8083/api/users")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // ====== STATION SERVICE ENDPOINTS ======
    
    // Get all stations
    @GetMapping("/stations")
    public Mono<ResponseEntity<String>> getAllStations() {
        return webClientBuilder.build()
            .get()
            .uri("http://station-service:8084/api/stations")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // ====== RESERVATION SERVICE ENDPOINTS ======
    
    // Get all reservations
    @GetMapping("/reservations")
    public Mono<ResponseEntity<String>> getAllReservations() {
        return webClientBuilder.build()
            .get()
            .uri("http://reservation-service:8087/api/reservations")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // ====== PAYMENT SERVICE ENDPOINTS ======
    
    // Get wallets
    @GetMapping("/wallets")
    public Mono<ResponseEntity<String>> getAllWallets() {
        return webClientBuilder.build()
            .get()
            .uri("http://payment-service:8085/api/wallets")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // ====== SCOOTER SERVICE ENDPOINTS ======
    
    // Get all scooters
    @GetMapping("/scooters")
    public Mono<ResponseEntity<String>> getAllScooters() {
        return webClientBuilder.build()
            .get()
            .uri("http://scooter-service:8086/api/scooters")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }

    // ====== RECLAMATION SERVICE ENDPOINTS ======
    
    // Get all reclamations
    @GetMapping("/reclamations")
    public Mono<ResponseEntity<String>> getAllReclamations() {
        return webClientBuilder.build()
            .get()
            .uri("http://reclamation-service:3001/api/reclamations")
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.ok(body));
                } else {
                    return response.bodyToMono(String.class)
                        .map(body -> ResponseEntity.status(response.statusCode()).body(body));
                }
            })
            .onErrorResume(e -> {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                error.put("timestamp", System.currentTimeMillis());
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error.toString()));
            });
    }
}
