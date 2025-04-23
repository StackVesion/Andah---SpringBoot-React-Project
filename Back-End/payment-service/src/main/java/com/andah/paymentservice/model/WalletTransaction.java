package com.andah.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    private String id;
    
    private String walletId; // Reference to the wallet
    
    private WalletTransactionType type; // DEPOSIT or WITHDRAWAL
    
    private Double amount;
    
    private LocalDateTime date;
    
    private String description; // Added description field
    
    // Reference to CardTransaction (can be null)
    private String cardTransactionId;
    
    // Reference to CryptoTransaction (can be null)
    private String cryptoTransactionId;
    
    public enum WalletTransactionType {
        DEPOSIT, WITHDRAWAL
    }
}
