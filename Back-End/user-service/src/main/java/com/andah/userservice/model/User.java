package com.andah.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    
    private String name;
    private String firstName;
    private String lastName;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    private String phoneNumber;
    private boolean isVerified;
    private String keycloakId;
    
    private List<String> reservationIds;
    private List<String> ratingIds;
    
    private Role role;
    
    // Champs pour l'OTP
    private boolean otpEnabled;
    private String otpSecret;
    private String tempOtp;
    private LocalDateTime tempOtpExpiryTime;
    
    public enum Role {
        USER, ADMIN, STATION_OWNER
    }
    
    // Méthode utilitaire pour vérifier si un OTP temporaire est valide
    public boolean isTempOtpValid() {
        return tempOtp != null && tempOtpExpiryTime != null && 
               LocalDateTime.now().isBefore(tempOtpExpiryTime);
    }
}
