package com.andah.paymentservice.dto;

import com.andah.paymentservice.model.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDto {
    private String id;
    private LocalDateTime timestamp;
    private PaymentTransaction.PaymentMethod paymentMethod;
    private Double amount;
    private String transactionReference;
    private Long userId;
    private String userName; // From user-service
    private Long scooterId;
    private String scooterName; // From scooter-service
    private Long reservationId;
    private PaymentTransaction.PaymentStatus status;
    
    // New fields for the updated relations logic
    private String cardTransactionId;    // ID of associated card transaction (optional)
    private String cryptoTransactionId;  // ID of associated crypto transaction (optional)
    private String walletTransactionId;  // ID of associated wallet transaction (optional)
    private String currency;             // Currency code (e.g., USD, BTC, ETH)
}
