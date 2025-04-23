package com.andah.paymentservice.dto;

import com.andah.paymentservice.model.WalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDto {
    private String id;
    private String walletId;
    private WalletTransaction.WalletTransactionType type;
    private Double amount;
    private LocalDateTime date;
    private String description;
    private String cardTransactionId;
    private String cryptoTransactionId;
}
