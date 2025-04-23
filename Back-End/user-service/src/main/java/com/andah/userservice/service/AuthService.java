package com.andah.userservice.service;

import com.andah.userservice.dto.AuthResponse;
import com.andah.userservice.dto.LoginRequest;
import com.andah.userservice.dto.RegisterRequest;
import com.andah.userservice.dto.UserDto;
import com.andah.userservice.dto.LoginWithOtpRequest;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.UserRepository;
import com.andah.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.andah.userservice.service.EmailService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Value("${app.otp.default-code:123456}")
    private String defaultOtpCode;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.use-random:true}")
    private boolean useRandomOtp;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        try {
            logger.info("Step 1: Validating registration data for: {}", request.getEmail());
            validateRegistrationRequest(request);

            logger.info("Step 2: Checking if email already exists");
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email already in use: {}", request.getEmail());
                throw new IllegalArgumentException("Email already in use");
            }

            logger.info("Step 3: Checking if phone number already exists");
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                logger.warn("Phone number already in use: {}", request.getPhoneNumber());
                throw new IllegalArgumentException("Phone number already in use");
            }



            logger.info("Step 5: Creating user entity in our database");
            User user = new User();
            user.setName(request.getName());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            // Encode the password for our local database
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setVerified(false);
            user.setRole(User.Role.USER);


            // Initialize collection fields to avoid NPEs
            user.setReservationIds(new ArrayList<>());
            user.setRatingIds(new ArrayList<>());

            logger.info("Step 6: Saving user to database");
            UserDto savedUser = userService.createUser(user);
            logger.info("User saved with ID: {}", savedUser.getId());

            logger.info("Step 7: Using Keycloak token instead of JWT");
            // Le token JWT est désormais géré par Keycloak, mais nous pouvons toujours

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            logger.info("Step 8: Building response");
            return AuthResponse.builder()
                    .token(token)
                    .userId(savedUser.getId())
                    .username(savedUser.getEmail())
                    .role(savedUser.getRole().toString())
                    .user(savedUser)
                    .build();
        } catch (Exception e) {
            logger.error("Registration error at service level: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Authentification avec Keycloak - Cette partie serait gérée par Keycloak directement
            // via le flux de sécurité, mais nous pouvons toujours vérifier la cohérence avec notre base

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

            // Nous n'avons plus besoin de vérifier le mot de passe manuellement, car Keycloak s'en chargera


            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }

            // Generate JWT token (pour la compatibilité avec l'existant)
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            // Build and return response
            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getEmail())
                    .role(user.getRole().toString())
                    .user(userService.getUserById(user.getId()))
                    .build();
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Generates a token for a user
     * This method is used by the controller to generate tokens for verified users
     */
    public String generateTokenForUser(User user) {
        logger.info("Generating token for user: {}", user.getEmail());
        return jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
    }    

    /**
     * Generate and send OTP to user's email
     */
    public Map<String, String> generateOtp(String email) {
        logger.info("Generating OTP for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Generate OTP code - either random or default for testing
        String otpCode = useRandomOtp ? generateRandomOtp() : defaultOtpCode;
        
        // Set OTP in user record
        user.setTempOtp(otpCode);
        user.setTempOtpExpiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userRepository.save(user);
        
        // ENVOI EFFECTIF DE L'OTP PAR EMAIL
        String subject = "Votre code OTP";
        String body = "Votre code OTP est : " + otpCode + "\nIl est valable " + otpExpiryMinutes + " minutes.";
        emailService.sendSimpleEmail(email, subject, body);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP generated successfully and sent to " + email);
        response.put("expiresIn", otpExpiryMinutes + " minutes");
        
        return response;
    }
    
    /**
     * Login with OTP authentication
     */
    public AuthResponse loginWithOtp(LoginWithOtpRequest request) {
        logger.info("Processing login with OTP for email: {}", request.getEmail());
        
        try {
            // Find the user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));
            
            // Verify OTP code
            if (!verifyOtp(user, request.getOtp())) {
                logger.warn("Invalid OTP provided for user: {}", request.getEmail());
                throw new BadCredentialsException("Invalid OTP code");
            }
            
            // Generate token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
            
            logger.info("Login with OTP successful for user: {}", request.getEmail());
            
            // Return authentication response
            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getEmail())
                    .role(user.getRole().toString())
                    .user(userService.getUserById(user.getId()))
                    .build();
                    
        } catch (Exception e) {
            logger.error("Login with OTP failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Verify OTP code
     */
    private boolean verifyOtp(User user, String otpCode) {
        // If user has temporary OTP set and it's valid
        if (user.isTempOtpValid() && user.getTempOtp().equals(otpCode)) {
            // Clear the temporary OTP after successful verification
            user.setTempOtp(null);
            user.setTempOtpExpiryTime(null);
            userRepository.save(user);
            return true;
        }
        
        // If user has TOTP enabled, verify with TOTP
        if (user.isOtpEnabled() && user.getOtpSecret() != null) {
            // Implement TOTP verification logic here if needed
            // For now, we'll just return false
            return false;
        }
        
        return false;
    }
    
    /**
     * Generate a random OTP code
     */
    private String generateRandomOtp() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(number);
    }

    // --- RESET PASSWORD FEATURE ---
    /**
     * Génère un token de reset pour l'utilisateur (UUID ou JWT court)
     */
    public String generateResetToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        // TODO: Associer ce token à l'utilisateur en base (table ResetPasswordToken ou champ temporaire User)
        return token;
    }

    /**
     * Envoie l'email de reset
     */
    public void sendResetPasswordEmail(String email, String resetToken) {
        String resetUrl = "https://andah/reset-password?token=" + resetToken;
        String subject = "Réinitialisation de votre mot de passe";
        String body = "Cliquez sur ce lien pour réinitialiser votre mot de passe : " + resetUrl;
        emailService.sendSimpleEmail(email, subject, body);
    }

    /**
     * Applique le nouveau mot de passe si le token est valide
     */
    public boolean resetPassword(String token, String newPassword) {
        // TODO: Vérifier que le token existe, n'est pas expiré, et retrouver l'utilisateur associé
        // Exemple: User user = findUserByResetToken(token);
        // if (user == null) return false;
        // user.setPassword(passwordEncoder.encode(newPassword));
        // user.setResetToken(null); // ou supprimer le token
        // userRepository.save(user);
        // return true;
        return false;
    }
}
