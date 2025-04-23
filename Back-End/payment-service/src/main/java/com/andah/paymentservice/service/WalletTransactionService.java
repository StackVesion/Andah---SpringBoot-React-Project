package com.andah.paymentservice.service;

import com.andah.paymentservice.dto.WalletTransactionDto;
import com.andah.paymentservice.model.WalletTransaction;
import com.andah.paymentservice.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Autowired
    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public WalletTransactionDto createWalletTransaction(WalletTransactionDto transactionDto) {
        WalletTransaction transaction = convertToEntity(transactionDto);
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);
        return convertToDto(savedTransaction);
    }

    public WalletTransaction createWalletTransaction(WalletTransaction transaction) {
        return walletTransactionRepository.save(transaction);
    }

    public Optional<WalletTransactionDto> getWalletTransactionById(String id) {
        return walletTransactionRepository.findById(id).map(this::convertToDto);
    }

    public List<WalletTransaction> getWalletTransactionHistory(String walletId) {
        return walletTransactionRepository.findByWalletIdOrderByDateDesc(walletId);
    }
    
    /**
     * Get all wallet transactions for a specific wallet filtered by transaction type
     * @param walletId The wallet ID
     * @param type The transaction type (DEPOSIT or WITHDRAWAL)
     * @return List of wallet transactions of the specified type
     */
    public List<WalletTransactionDto> getWalletTransactionsByType(String walletId, WalletTransaction.WalletTransactionType type) {
        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletIdAndTypeOrderByDateDesc(walletId, type);
        return transactions.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get all transactions of a specific type across all wallets
     * @param type The transaction type (DEPOSIT or WITHDRAWAL)
     * @return List of transactions of the specified type
     */
    public List<WalletTransactionDto> getAllTransactionsByType(WalletTransaction.WalletTransactionType type) {
        List<WalletTransaction> transactions = walletTransactionRepository.findByType(type);
        return transactions.stream().map(this::convertToDto).toList();
    }
    
    /**
     * Get all wallet transactions for a user by finding their wallet first
     * @param walletId The wallet ID
     * @return List of wallet transactions converted to DTOs
     */
    public List<WalletTransactionDto> getWalletTransactionDtos(String walletId) {
        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletIdOrderByDateDesc(walletId);
        return transactions.stream().map(this::convertToDto).toList();
    }

    // Conversion methods
    private WalletTransactionDto convertToDto(WalletTransaction transaction) {
        WalletTransactionDto dto = new WalletTransactionDto();
        dto.setId(transaction.getId());
        dto.setWalletId(transaction.getWalletId());
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setDate(transaction.getDate());
        dto.setDescription(transaction.getDescription());
        dto.setCardTransactionId(transaction.getCardTransactionId());
        dto.setCryptoTransactionId(transaction.getCryptoTransactionId());
        return dto;
    }

    private WalletTransaction convertToEntity(WalletTransactionDto dto) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(dto.getWalletId());
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setDescription(dto.getDescription());
        transaction.setCardTransactionId(dto.getCardTransactionId());
        transaction.setCryptoTransactionId(dto.getCryptoTransactionId());
        return transaction;
    }
}
