package com.andah.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Email cannot be blank")
    private String email;  // Changed from username to email to match service usage
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
