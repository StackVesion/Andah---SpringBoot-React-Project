package com.andah.paymentservice.repository;

import com.andah.paymentservice.model.WalletTransaction;
import com.andah.paymentservice.model.WalletTransaction.WalletTransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    List<WalletTransaction> findByWalletId(String walletId);
    List<WalletTransaction> findByWalletIdOrderByDateDesc(String walletId);
    
    // New methods for transaction type filtering
    List<WalletTransaction> findByType(WalletTransactionType type);
    List<WalletTransaction> findByWalletIdAndType(String walletId, WalletTransactionType type);
    List<WalletTransaction> findByWalletIdAndTypeOrderByDateDesc(String walletId, WalletTransactionType type);
}
