package com.andah.userservice.controller;

import com.andah.userservice.dto.RegisterRequest;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestRegistrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestRegistrationController.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<?> testRegister(@RequestBody RegisterRequest request) {
        try {
            logger.info("TEST: Received registration request for: {}", request.getEmail());
            
            // Create user directly
            User user = new User();
            user.setName(request.getName());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setVerified(false);
            user.setRole(User.Role.USER);
            user.setReservationIds(new ArrayList<>());
            user.setRatingIds(new ArrayList<>());
            
            // Save directly to repository
            User savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("TEST: Registration failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> testHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Test controller is working");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/simple")
    public ResponseEntity<?> simpleTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Simple test endpoint working!");
        response.put("timestamp", new java.util.Date().toString());
        return ResponseEntity.ok(response);
    }
}
