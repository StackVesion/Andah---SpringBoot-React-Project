package com.andah.paymentservice.service;

import com.andah.paymentservice.dto.WalletDto;
import com.andah.paymentservice.model.Wallet;
import com.andah.paymentservice.model.WalletTransaction;
import com.andah.paymentservice.repository.WalletRepository;
import com.andah.paymentservice.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public Wallet getOrCreateWallet(Long userId) {
        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);
        if (existingWallet.isPresent()) {
            return existingWallet.get();
        } else {
            Wallet newWallet = new Wallet();
            newWallet.setUserId(userId);
            newWallet.setBalance(0.0);
            return walletRepository.save(newWallet);
        }
    }

    public WalletDto getWalletByUserId(Long userId) {
        Optional<Wallet> wallet = walletRepository.findByUserId(userId);
        return wallet.map(this::convertToDto).orElse(null);
    }

    public Wallet depositToWallet(String walletId, Double amount, String cardTransactionId, String cryptoTransactionId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        // Create wallet transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setType(WalletTransaction.WalletTransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        transaction.setCardTransactionId(cardTransactionId);
        transaction.setCryptoTransactionId(cryptoTransactionId);
        walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }

    public Wallet withdrawFromWallet(String walletId, Double amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds in wallet");
        }

        // Create wallet transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setType(WalletTransaction.WalletTransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());
        walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalance(wallet.getBalance() - amount);
        return walletRepository.save(wallet);
    }
    
    public Double getWalletBalance(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        return wallet.getBalance();
    }
    
    public WalletDto updateWallet(String walletId, WalletDto walletDto) {
        Wallet existingWallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
        
        // Update wallet fields from DTO
        if (walletDto.getBalance() != null) {
            existingWallet.setBalance(walletDto.getBalance());
        }
        
        Wallet updatedWallet = walletRepository.save(existingWallet);
        return convertToDto(updatedWallet);
    }
    
    // Conversion methods
    private WalletDto convertToDto(Wallet wallet) {
        WalletDto dto = new WalletDto();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        return dto;
    }
}
