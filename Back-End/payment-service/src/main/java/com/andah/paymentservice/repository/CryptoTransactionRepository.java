package com.andah.paymentservice.repository;

import com.andah.paymentservice.model.CryptoTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoTransactionRepository extends MongoRepository<CryptoTransaction, String> {
    List<CryptoTransaction> findByUserId(Long userId);
    List<CryptoTransaction> findByPaymentStatus(String status);
    List<CryptoTransaction> findByNowPaymentId(String nowPaymentId);
}
