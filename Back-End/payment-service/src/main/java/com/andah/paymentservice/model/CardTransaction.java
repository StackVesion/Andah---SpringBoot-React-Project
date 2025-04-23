package com.andah.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "card_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTransaction {
    @Id
    private String id;
    private Long userId;
    private String paymentId; // Reference to the parent PaymentTransaction
    private LocalDateTime date;
    private String cardNumber; // Should be masked/encrypted in a real app
    private String dateExp;
    private Double amount;
    private String billingAddress;
    private String zipCode;
    private String cardHolderName;
    private String state;
    private String region;
    private String cvv; // Card verification value
    private String transactionId; // External transaction ID (e.g., from Stripe)
    private String status; // Transaction status
}
