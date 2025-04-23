package com.andah.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    @Id
    private String id;
    
    private LocalDateTime timestamp;
    
    private PaymentMethod paymentMethod;
    
    private Double amount;
    
    private String transactionReference;
    
    private Long userId;
    
    private Long scooterId;
    
    // Reservation ID to establish the one-to-one relationship with Reservation entity
    private Long reservationId;
    
    // Status of payment transaction
    private PaymentStatus status;
    
    // One-to-one relationship with CardTransaction (can be null)
    private String cardTransactionId;
    
    // One-to-one relationship with CryptoTransaction (can be null)
    private String cryptoTransactionId;
    
    // One-to-one relationship with WalletTransaction (can be null) - when using wallet as payment method
    private String walletTransactionId;
    
    // Currency for payment (used mainly for crypto payments)
    private String currency;
    
    public enum PaymentMethod {
        CREDIT_CARD, CASH, CRYPTO, WALLET
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
