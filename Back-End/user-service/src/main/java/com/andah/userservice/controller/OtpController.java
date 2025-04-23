package com.andah.userservice.controller;

import com.andah.userservice.dto.OtpRequest;
import com.andah.userservice.dto.OtpRequestDto;
import com.andah.userservice.dto.OtpSetupResponse;
import com.andah.userservice.dto.OtpValidationRequest;
import com.andah.userservice.model.User;
import com.andah.userservice.repository.UserRepository;
import com.andah.userservice.service.OtpService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OtpController {

    private final Logger logger = LoggerFactory.getLogger(OtpController.class);
    private final OtpService otpService;
    private final UserRepository userRepository;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @PostMapping("/setup")
    public ResponseEntity<OtpSetupResponse> setupOtp(@RequestParam Long userId) {
        logger.info("Réception d'une demande de configuration OTP pour l'utilisateur ID: {}", userId);
        
        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Générer un nouveau secret
        String secret = otpService.generateSecret();
        user.setOtpSecret(secret);
        user.setOtpEnabled(true);
        userRepository.save(user);
        
        try {
            // Générer le QR code
            String qrCodeImage = otpService.generateQrCodeImage(secret, user.getEmail());
            
            OtpSetupResponse response = OtpSetupResponse.builder()
                    .secretKey(secret)
                    .qrCodeImage(qrCodeImage)
                    .otpEnabled(true)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (QrGenerationException e) {
            logger.error("Erreur lors de la génération du QR code: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/disable")
    public ResponseEntity<?> disableOtp(@RequestParam Long userId) {
        logger.info("Réception d'une demande de désactivation OTP pour l'utilisateur ID: {}", userId);
        
        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        otpService.disableOtpForUser(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentification OTP désactivée avec succès");
        response.put("otpEnabled", false);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate-totp")
    public ResponseEntity<?> validateTotp(@RequestBody OtpValidationRequest request) {
        logger.info("Réception d'une demande de validation OTP pour l'email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        boolean isValid = otpService.validateTotp(request.getOtpCode(), user.getOtpSecret());
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        
        if (isValid) {
            response.put("message", "Code OTP valide");
        } else {
            response.put("message", "Code OTP invalide");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/request-temp")
    public ResponseEntity<?> requestTempOtp(@RequestBody OtpRequestDto request) {
        logger.info("Réception d'une demande d'OTP temporaire pour l'email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Générer un OTP temporaire
        String tempOtp = otpService.generateTempOtp();
        
        // Sauvegarder l'OTP et envoyer par email
        otpService.saveTempOtpForUser(user, tempOtp, otpExpiryMinutes);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Un code de vérification a été envoyé à votre adresse email");
        response.put("expiryMinutes", otpExpiryMinutes);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate-temp")
    public ResponseEntity<?> validateTempOtp(@RequestBody OtpValidationRequest request) {
        logger.info("Réception d'une demande de validation d'OTP temporaire pour l'email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        boolean isValid = otpService.validateTempOtp(user, request.getOtpCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        
        if (isValid) {
            response.put("message", "Code OTP temporaire valide");
            // Si c'est pour la vérification de compte, on peut marquer le compte comme vérifié
            if (!user.isVerified()) {
                user.setVerified(true);
                userRepository.save(user);
                response.put("accountVerified", true);
            }
        } else {
            response.put("message", "Code OTP temporaire invalide ou expiré");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint spécial pour le développement uniquement
     * Permet de récupérer le code OTP d'un utilisateur sans passer par l'email
     */
    @GetMapping("/dev/get-code")
    public ResponseEntity<?> getOtpCodeForDev(@RequestParam String email) {
        logger.info("DEV ONLY: Demande de récupération directe du code OTP pour: {}", email);
        
        // Vérifier que nous sommes en mode développement
        String activeProfile = System.getProperty("spring.profiles.active", "default");
        if (!("default".equals(activeProfile) || "dev".equals(activeProfile))) {
            logger.error("Tentative d'accès à un endpoint de développement en mode production");
            return ResponseEntity.status(403).body(Map.of("error", "Cette fonctionnalité n'est disponible qu'en mode développement"));
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Map<String, Object> response = new HashMap<>();
        
        if (user.isTempOtpValid()) {
            response.put("tempOtp", user.getTempOtp());
            response.put("expiryTime", user.getTempOtpExpiryTime().toString());
            response.put("message", "Code OTP valide trouvé");
        } else {
            // Si aucun code valide n'existe, on retourne le code par défaut en développement
            response.put("tempOtp", "123456");
            response.put("message", "Aucun code valide trouvé, utilisez le code par défaut: 123456");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody OtpRequest request) {
        try {
            logger.info("Generating OTP for email: {}", request.getEmail());
            
            // Vérifier si l'utilisateur existe
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (user == null) {
                logger.warn("User not found with email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }
            
            // Générer et envoyer l'OTP
            String otp = otpService.generateAndSendOtp(request.getEmail());
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP generated and sent successfully");
            response.put("email", request.getEmail());
            
            // En mode simulation, inclure l'OTP dans la réponse pour faciliter les tests
            if (otpService.isSimulationMode()) {
                response.put("otp", otp);
                response.put("simulationMode", true);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating OTP: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to generate OTP: " + e.getMessage()));
        }
    }
    
    @PostMapping("/validate-simple")
    public ResponseEntity<?> validateOtp(@RequestBody OtpRequest request) {
        try {
            logger.info("Validating OTP for email: {}", request.getEmail());
            
            // Vérifier si l'utilisateur existe
            User user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (user == null) {
                logger.warn("User not found with email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }
            
            // Valider l'OTP
            boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
            
            if (isValid) {
                // Mettre à jour le statut vérifié si nécessaire
                if (!user.isVerified()) {
                    user.setVerified(true);
                    userRepository.save(user);
                    logger.info("User verified: {}", request.getEmail());
                }
                
                return ResponseEntity.ok(Map.of(
                    "message", "OTP validated successfully",
                    "email", request.getEmail(),
                    "valid", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid or expired OTP",
                    "email", request.getEmail(),
                    "valid", false
                ));
            }
        } catch (Exception e) {
            logger.error("Error validating OTP: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to validate OTP: " + e.getMessage()));
        }
    }
}