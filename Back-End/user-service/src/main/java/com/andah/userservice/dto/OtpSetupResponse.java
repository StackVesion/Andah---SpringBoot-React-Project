package com.andah.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSetupResponse {
    private String secretKey;
    private String qrCodeImage;
    private boolean otpEnabled;
}