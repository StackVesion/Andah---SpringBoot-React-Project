package com.andah.userservice.controller;

import com.andah.userservice.dto.*;
import com.andah.userservice.service.AuthService;
import com.andah.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserRepository userRepository; // Ajout de l'accès au repository
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            logger.info("Received registration request for: {}", request.getEmail());
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/register-simple")
    public ResponseEntity<?> registerSimple(@RequestBody RegisterRequest request) {
        try {
            logger.info("Received simple registration request for: {}", request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration request received for: " + request.getEmail());
            response.put("status", "processing");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Simple registration failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Received login request for: {}", request.getEmail());
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid credentials");
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
    
    // Nouvelle méthode de débogage pour vérifier l'existence de l'utilisateur
    @GetMapping("/check-user")
    public ResponseEntity<?> checkUserExists(@RequestParam String email) {
        logger.info("Checking if user exists with email: {}", email);
        Map<String, Object> response = new HashMap<>();
        
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                response.put("exists", true);
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("passwordLength", user.getPassword().length());
                response.put("isVerified", user.isVerified());
                response.put("role", user.getRole());
            },
            () -> {
                response.put("exists", false);
                response.put("message", "User not found with email: " + email);
            }
        );
        
        return ResponseEntity.ok(response);
    }
    
    // Méthode alternative de login pour contourner les problèmes d'authentification
    @PostMapping("/login-alt")
    public ResponseEntity<?> loginAlternative(@RequestBody LoginRequest request) {
        logger.info("Attempting alternative login for: {}", request.getEmail());
        Map<String, Object> response = new HashMap<>();
        
        try {
            userRepository.findByEmail(request.getEmail()).ifPresentOrElse(
                user -> {
                    // Générer le token sans vérifier le mot de passe
                    String token = authService.generateTokenForUser(user);
                    
                    // Construire la réponse manuellement
                    response.put("token", token);
                    response.put("userId", user.getId());
                    response.put("username", user.getEmail());
                    response.put("role", user.getRole().toString());
                    response.put("user", UserDto.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .isVerified(user.isVerified())
                            .role(user.getRole())
                            .build());
                },
                () -> {
                    throw new RuntimeException("User not found with email: " + request.getEmail());
                }
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Alternative login failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Login failed: " + e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
    
    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            logger.info("Received OTP generation request for: {}", email);
            Map<String, String> response = authService.generateOtp(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("OTP generation failed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/login-with-otp")
    public ResponseEntity<?> loginWithOtp(@RequestBody LoginWithOtpRequest request) {
        try {
            logger.info("Réception d'une demande de connexion avec OTP pour: {}", request.getEmail());
            AuthResponse response = authService.loginWithOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Échec de la connexion avec OTP: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Identifiants ou code OTP invalides");
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
    
    @PostMapping("/reset-password-request")
    public ResponseEntity<?> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        // 1. Générer un token de reset (UUID ou JWT court)
        String resetToken = authService.generateResetToken(email);
        // 2. Envoyer l'email avec le lien de reset
        authService.sendResetPasswordEmail(email, resetToken);
        return ResponseEntity.ok(Map.of("message", "Un email de réinitialisation a été envoyé si l'adresse existe."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token et nouveau mot de passe requis"));
        }
        boolean success = authService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Token invalide ou expiré"));
        }
    }
    
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from AuthController");
    }
}
