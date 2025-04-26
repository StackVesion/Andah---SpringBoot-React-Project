package com.andah.userservice.controller;

import com.andah.userservice.dto.AuthResponse;
import com.andah.userservice.dto.UserDto;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.UserRepository;
import com.andah.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from User Service!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/bypass-login")
    public ResponseEntity<?> bypassLogin(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    String refreshToken = jwtUtil.generateRefreshToken(user);
                    
                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .refreshToken(refreshToken)
                            .userId(user.getId())
                            .username(user.getEmail())
                            .email(user.getEmail())
                            .role(user.getRole().toString())
                            .user(UserDto.builder()
                                    .id(user.getId())
                                    .email(user.getEmail())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .isVerified(user.isVerified())
                                    .role(user.getRole())
                                    .build())
                            .build();
                    
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "User not found with email: " + email);
                    return ResponseEntity.badRequest().body(response);
                });
    }
}
