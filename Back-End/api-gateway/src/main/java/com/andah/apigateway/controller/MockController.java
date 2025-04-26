package com.andah.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/mock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MockController {

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mock controller is working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // Users endpoint
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "1");
        user1.put("email", "user1@example.com");
        user1.put("firstName", "John");
        user1.put("lastName", "Doe");
        user1.put("role", "USER");
        
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "2");
        user2.put("email", "admin@example.com");
        user2.put("firstName", "Admin");
        user2.put("lastName", "User");
        user2.put("role", "ADMIN");
        
        users.add(user1);
        users.add(user2);
        
        response.put("users", users);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Stations endpoint
    @GetMapping("/stations")
    public ResponseEntity<Map<String, Object>> getStations() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> stations = new ArrayList<>();
        
        Map<String, Object> station1 = new HashMap<>();
        station1.put("id", "1");
        station1.put("name", "Central Station");
        station1.put("address", "123 Main St");
        station1.put("capacity", 20);
        station1.put("availableScooters", 15);
        
        Map<String, Object> station2 = new HashMap<>();
        station2.put("id", "2");
        station2.put("name", "North Station");
        station2.put("address", "456 Oak Ave");
        station2.put("capacity", 15);
        station2.put("availableScooters", 10);
        
        stations.add(station1);
        stations.add(station2);
        
        response.put("stations", stations);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Scooters endpoint
    @GetMapping("/scooters")
    public ResponseEntity<Map<String, Object>> getScooters() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> scooters = new ArrayList<>();
        
        Map<String, Object> scooter1 = new HashMap<>();
        scooter1.put("id", "1");
        scooter1.put("model", "XR500");
        scooter1.put("status", "AVAILABLE");
        scooter1.put("batteryLevel", 95);
        scooter1.put("stationId", "1");
        
        Map<String, Object> scooter2 = new HashMap<>();
        scooter2.put("id", "2");
        scooter2.put("model", "XR500");
        scooter2.put("status", "IN_USE");
        scooter2.put("batteryLevel", 80);
        scooter2.put("stationId", null);
        
        scooters.add(scooter1);
        scooters.add(scooter2);
        
        response.put("scooters", scooters);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Reservations endpoint
    @GetMapping("/reservations")
    public ResponseEntity<Map<String, Object>> getReservations() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> reservations = new ArrayList<>();
        
        Map<String, Object> reservation1 = new HashMap<>();
        reservation1.put("id", "1");
        reservation1.put("userId", "1");
        reservation1.put("scooterId", "1");
        reservation1.put("startTime", "2025-04-24T09:00:00");
        reservation1.put("endTime", "2025-04-24T10:00:00");
        reservation1.put("status", "COMPLETED");
        reservation1.put("cost", 10.5);
        
        Map<String, Object> reservation2 = new HashMap<>();
        reservation2.put("id", "2");
        reservation2.put("userId", "2");
        reservation2.put("scooterId", "2");
        reservation2.put("startTime", "2025-04-24T11:00:00");
        reservation2.put("endTime", null);
        reservation2.put("status", "ACTIVE");
        reservation2.put("cost", 0.0);
        
        reservations.add(reservation1);
        reservations.add(reservation2);
        
        response.put("reservations", reservations);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Payments/Wallets endpoint
    @GetMapping("/wallets")
    public ResponseEntity<Map<String, Object>> getWallets() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> wallets = new ArrayList<>();
        
        Map<String, Object> wallet1 = new HashMap<>();
        wallet1.put("id", "1");
        wallet1.put("userId", "1");
        wallet1.put("balance", 150.75);
        wallet1.put("currency", "EUR");
        
        Map<String, Object> wallet2 = new HashMap<>();
        wallet2.put("id", "2");
        wallet2.put("userId", "2");
        wallet2.put("balance", 250.25);
        wallet2.put("currency", "EUR");
        
        wallets.add(wallet1);
        wallets.add(wallet2);
        
        response.put("wallets", wallets);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Reclamations endpoint
    @GetMapping("/reclamations")
    public ResponseEntity<Map<String, Object>> getReclamations() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> reclamations = new ArrayList<>();
        
        Map<String, Object> reclamation1 = new HashMap<>();
        reclamation1.put("id", "1");
        reclamation1.put("userId", "1");
        reclamation1.put("subject", "Scooter Issue");
        reclamation1.put("description", "Scooter battery drained too quickly");
        reclamation1.put("status", "PENDING");
        reclamation1.put("createdAt", "2025-04-22T15:30:00");
        
        Map<String, Object> reclamation2 = new HashMap<>();
        reclamation2.put("id", "2");
        reclamation2.put("userId", "2");
        reclamation2.put("subject", "Payment Issue");
        reclamation2.put("description", "Was charged twice for my reservation");
        reclamation2.put("status", "RESOLVED");
        reclamation2.put("createdAt", "2025-04-20T09:15:00");
        
        reclamations.add(reclamation1);
        reclamations.add(reclamation2);
        
        response.put("reclamations", reclamations);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    // Auth login endpoint that always succeeds
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.getOrDefault("email", "test@example.com");
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("email", email);
        user.put("firstName", "Test");
        user.put("lastName", "User");
        user.put("role", "USER");
        
        response.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        response.put("refreshToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IlRlc3QgVXNlciIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        response.put("user", user);
        response.put("userId", "1");
        response.put("username", email);
        response.put("email", email);
        response.put("role", "USER");
        
        return ResponseEntity.ok(response);
    }
    
    // Auth register endpoint that always succeeds
    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.getOrDefault("email", "newuser@example.com");
        String firstName = request.getOrDefault("firstName", "New");
        String lastName = request.getOrDefault("lastName", "User");
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", UUID.randomUUID().toString());
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("role", "USER");
        
        response.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwibmFtZSI6Ik5ldyBVc2VyIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        response.put("refreshToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwibmFtZSI6Ik5ldyBVc2VyIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        response.put("user", user);
        response.put("message", "User registered successfully");
        
        return ResponseEntity.ok(response);
    }
}
