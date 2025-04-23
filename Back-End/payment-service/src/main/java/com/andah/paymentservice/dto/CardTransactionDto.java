package com.andah.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionDto {
    private String id;
    private Long userId;
    private String paymentId;
    private LocalDateTime date;
    private String cardNumber; // Should be masked for security
    private String dateExp;
    private Integer expiryMonth; // Month expiration (1-12)
    private Integer expiryYear;  // Year expiration (e.g., 2025)
    private Double amount;
    private String billingAddress;
    private String zipCode;
    private String cardHolderName;
    private String state;
    private String region;
    private String transactionId; // Stripe transaction ID
    private String transactionReference; // Reference code from payment processor
    private String paymentProcessor; // Name of payment processor (e.g., "Stripe")
    private String currency; // Currency of the transaction (e.g., "USD")
    private String status; // Status of the transaction
    private String cvv; // CVV code for the card (for processing only, should not be stored)
}
