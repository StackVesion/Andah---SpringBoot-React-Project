package com.andah.paymentservice.repository;

import com.andah.paymentservice.model.CardTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CardTransactionRepository extends MongoRepository<CardTransaction, String> {
    List<CardTransaction> findByUserId(Long userId);
    List<CardTransaction> findByPaymentId(String paymentId);
}
