package com.andah.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;  // Changed from 'id' to 'userId' to match builder usage
    private String username;
    private String email;
    private String role;    // Changed from List<String> roles to String role
    private UserDto user;   // Added missing user field
}
