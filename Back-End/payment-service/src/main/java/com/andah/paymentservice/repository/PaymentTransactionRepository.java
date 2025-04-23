package com.andah.paymentservice.repository;

import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.model.PaymentTransaction.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends MongoRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByUserId(Long userId);
    List<PaymentTransaction> findByScooterId(Long scooterId);
    List<PaymentTransaction> findByReservationId(Long reservationId);
    List<PaymentTransaction> findByStatus(PaymentStatus status);
    List<PaymentTransaction> findByCryptoTransactionId(String cryptoTransactionId);
}
