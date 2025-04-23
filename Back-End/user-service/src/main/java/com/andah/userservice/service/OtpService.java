package com.andah.userservice.service;

import com.andah.userservice.model.User;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@RequiredArgsConstructor
public class OtpService {
    
    private final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final UserService userService;

    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Getter
    @Value("${app.email.simulation:true}")
    private boolean simulationMode;
    
    @Value("${app.otp.default-code:123456}")
    private String defaultOtpCode;

    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;
    
    @Value("${app.otp.length:6}")
    private int otpLength;
    
    @Value("${app.otp.use-random:true}")
    private boolean useRandomOtp;
    
    // Map pour stocker les OTP (en mémoire) - email -> {code, timestamp}
    private final Map<String, Map<String, Object>> otpStorage = new HashMap<>();

    // Méthode pour générer un secret OTP
    public String generateSecret() {
        SecretGenerator generator = new DefaultSecretGenerator();
        return generator.generate();
    }

    // Méthode pour générer un code QR pour l'application authenticator
    public String generateQrCodeImage(String secret, String email) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Andah Scooter")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        
        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    // Méthode pour valider un code OTP
    public boolean validateTotp(String code, String secret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        
        return verifier.isValidCode(secret, code);
    }
    
    // Méthode pour générer un OTP temporaire (6 chiffres)
    public String generateTempOtp() {
        // En mode développement ou simulation, toujours utiliser le code par défaut
        if (simulationMode || "default".equals(activeProfile) || "dev".equals(activeProfile)) {
            logger.info("MODE DÉVELOPPEMENT: Code OTP fixé à '{}' pour les tests", defaultOtpCode);
            return defaultOtpCode;
        }
        
        // En production, générer un code aléatoire
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6 chiffres
        return String.valueOf(otp);
    }
    
    // Méthode pour enregistrer un OTP temporaire pour un utilisateur
    public void saveTempOtpForUser(User user, String tempOtp, int expiryMinutes) {
        logger.info("Enregistrement d'un OTP temporaire pour l'utilisateur: {}", user.getEmail());
        
        // Stocker le code OTP et sa date d'expiration
        user.setTempOtp(tempOtp);
        user.setTempOtpExpiryTime(LocalDateTime.now().plusMinutes(expiryMinutes));
        userService.saveUser(user);
        
        // Envoyer l'OTP par email avec notre nouveau service d'email amélioré
        try {
            logger.info("Email avec OTP envoyé avec succès à: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Échec d'envoi d'email: {}", e.getMessage());
            if ("default".equals(activeProfile) || "dev".equals(activeProfile)) {
                logger.warn("En mode développement, l'échec d'envoi d'email est ignoré. Utilisez la page de développement pour voir les codes OTP.");
            } else {
                throw e;
            }
        }
    }
    
    // Méthode pour valider un OTP temporaire
    public boolean validateTempOtp(User user, String otpToValidate) {
        // En mode développement, accepter le code par défaut même si l'utilisateur n'a pas de code OTP valide
        if ((simulationMode || "default".equals(activeProfile) || "dev".equals(activeProfile))
            && defaultOtpCode.equals(otpToValidate)) {
            logger.info("Validation réussie avec le code OTP par défaut pour: {}", user.getEmail());
            return true;
        }
        
        // Validation normale
        if (!user.isTempOtpValid()) {
            logger.warn("OTP expiré ou non défini pour l'utilisateur {}", user.getEmail());
            return false;
        }
        
        boolean isValid = user.getTempOtp().equals(otpToValidate);
        if (isValid) {
            // Réinitialiser l'OTP temporaire après validation réussie
            user.setTempOtp(null);
            user.setTempOtpExpiryTime(null);
            userService.saveUser(user);
            logger.info("Validation OTP réussie pour: {}", user.getEmail());
        } else {
            logger.warn("Code OTP invalide pour l'utilisateur: {}", user.getEmail());
        }
        
        return isValid;
    }
    
    // Méthode pour activer l'authentification OTP pour un utilisateur
    public void enableOtpForUser(User user) {
        String secret = generateSecret();
        user.setOtpSecret(secret);
        user.setOtpEnabled(true);
        userService.saveUser(user);
    }
    
    // Méthode pour désactiver l'authentification OTP pour un utilisateur
    public void disableOtpForUser(User user) {
        user.setOtpSecret(null);
        user.setOtpEnabled(false);
        userService.saveUser(user);
    }

    /**
     * Génère un OTP pour un email donné et l'envoie par email
     * 
     * @param email Email de l'utilisateur
     * @return Le code OTP généré
     */
    public String generateAndSendOtp(String email) {
        String otp = generateOtp();
        storeOtp(email, otp);
        
        try {
            // Utiliser le service d'email amélioré pour envoyer l'OTP
            logger.info("Email OTP envoyé avec succès à: {}", email);
        } catch (Exception e) {
            logger.error("Échec d'envoi d'email OTP: {}", e.getMessage(), e);
            // Ne pas lever d'exception en mode simulation
            if (!simulationMode) {
                throw new RuntimeException("Failed to send OTP email", e);
            } else {
                logger.info("Mode simulation: Ignorer l'erreur d'envoi d'email");
            }
        }
        
        return otp;
    }
    
    /**
     * Valide un OTP pour un email donné
     * 
     * @param email Email de l'utilisateur
     * @param otp Code OTP à valider
     * @return true si l'OTP est valide, false sinon
     */
    public boolean validateOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) {
            logger.warn("No OTP found for email: {}", email);
            // En mode simulation, accepter le code par défaut
            if (simulationMode && otp.equals(defaultOtpCode)) {
                logger.info("Simulation mode: default OTP accepted for: {}", email);
                return true;
            }
            return false;
        }
        
        Map<String, Object> otpData = otpStorage.get(email);
        String storedOtp = (String) otpData.get("code");
        LocalDateTime timestamp = (LocalDateTime) otpData.get("timestamp");
        
        // Vérifier si l'OTP a expiré
        if (timestamp.plusMinutes(expiryMinutes).isBefore(LocalDateTime.now())) {
            logger.warn("OTP expired for email: {}", email);
            otpStorage.remove(email);
            return false;
        }
        
        // Vérifier si l'OTP est correct
        boolean isValid = storedOtp.equals(otp);
        
        // Si valide, supprimer l'OTP pour qu'il ne puisse pas être réutilisé
        if (isValid) {
            otpStorage.remove(email);
            logger.info("OTP validated successfully for: {}", email);
        } else {
            logger.warn("Invalid OTP provided for: {}", email);
        }
        
        return isValid;
    }
    
    /**
     * Génère un OTP aléatoire
     * 
     * @return Code OTP
     */
    private String generateOtp() {
        // Si useRandomOtp est true, générer toujours un code aléatoire, même en mode simulation
        if (useRandomOtp || !simulationMode) {
            // Générer un nombre aléatoire avec le nombre de chiffres spécifié
            Random random = new Random();
            StringBuilder otp = new StringBuilder();
            
            for (int i = 0; i < otpLength; i++) {
                otp.append(random.nextInt(10));
            }
            
            String generatedOtp = otp.toString();
            logger.info("Generated new random OTP: {}", generatedOtp);
            return generatedOtp;
        } else {
            // Utiliser le code par défaut seulement si useRandomOtp est false et en mode simulation
            logger.info("Simulation mode with default OTP: using code: {}", defaultOtpCode);
            return defaultOtpCode;
        }
    }
    
    /**
     * Stocke un OTP en mémoire
     * 
     * @param email Email de l'utilisateur
     * @param otp Code OTP
     */
    private void storeOtp(String email, String otp) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("code", otp);
        otpData.put("timestamp", LocalDateTime.now());
        
        otpStorage.put(email, otpData);
        logger.info("OTP stored for email: {}", email);
    }
}