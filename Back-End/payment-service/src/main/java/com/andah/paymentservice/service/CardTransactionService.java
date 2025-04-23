package com.andah.paymentservice.service;

import com.andah.paymentservice.dto.CardTransactionDto;
import com.andah.paymentservice.model.CardTransaction;
import com.andah.paymentservice.repository.CardTransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CardTransactionService {

    private final CardTransactionRepository cardTransactionRepository;
    
    @Value("${stripe.api.key:sk_test_51QWkPJDv0oob45G0dizhQzmMeUY6LcdW8POzhvJ6jJ0Mv9Do9GS2WC7XAq3ZDufBCaJuGRbaYl7NrtoyJxpgdx5d00FIR9nfuJ}")
    private String stripeApiKey;

    @Autowired
    public CardTransactionService(CardTransactionRepository cardTransactionRepository) {
        this.cardTransactionRepository = cardTransactionRepository;
    }
    
    /**
     * Process a card transaction using Stripe and create a record
     *
     * @param cardTransactionDto Card transaction details
     * @return Created transaction with Stripe response
     */
    public CardTransactionDto createCardTransaction(CardTransactionDto cardTransactionDto) {
        try {
            // Initialize Stripe with API key
            Stripe.apiKey = stripeApiKey;
            
            // Create a payment intent with Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long)(cardTransactionDto.getAmount() * 100)) // Amount in cents
                .setCurrency("usd")
                .setPaymentMethod(createPaymentMethod(cardTransactionDto))
                .setConfirm(true)
                .setDescription("Payment for user " + cardTransactionDto.getUserId())
                .build();
                
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Set Stripe transaction information
            cardTransactionDto.setTransactionId(paymentIntent.getId());
            cardTransactionDto.setStatus(paymentIntent.getStatus());
            
            // Save to database with masked card details
            CardTransaction cardTransaction = convertToEntity(cardTransactionDto);
            cardTransaction.setDate(LocalDateTime.now());
            
            // Mask sensitive information
            maskCardDetails(cardTransaction);
            
            cardTransaction = cardTransactionRepository.save(cardTransaction);
            return convertToDto(cardTransaction);
        } catch (StripeException e) {
            throw new RuntimeException("Error processing card payment: " + e.getMessage());
        }
    }
    
    /**
     * Create a payment method with Stripe for the card details
     * 
     * @param dto Card transaction details
     * @return Payment method ID
     */
    private String createPaymentMethod(CardTransactionDto dto) {
        // In a real implementation, you would create a PaymentMethod using Stripe API
        // For test mode, let's use a test payment method that always succeeds
        return "pm_card_visa"; // Stripe test payment method for success scenario
    }
    
    /**
     * Mask sensitive card information for storage
     * 
     * @param cardTransaction Card transaction with sensitive data
     */
    private void maskCardDetails(CardTransaction cardTransaction) {
        // Mask card number - keep only last 4 digits
        String cardNumber = cardTransaction.getCardNumber();
        if (cardNumber != null && cardNumber.length() > 4) {
            cardTransaction.setCardNumber("XXXX-XXXX-XXXX-" + cardNumber.substring(cardNumber.length() - 4));
        }
        
        // Remove CVV completely
        cardTransaction.setCvv(null);
    }

    public List<CardTransactionDto> getAllCardTransactions() {
        return cardTransactionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<CardTransactionDto> getCardTransactionById(String id) {
        return cardTransactionRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<CardTransactionDto> getCardTransactionsByUserId(Long userId) {
        return cardTransactionRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CardTransactionDto> getCardTransactionsByPaymentId(String paymentId) {
        return cardTransactionRepository.findByPaymentId(paymentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void deleteCardTransaction(String id) {
        cardTransactionRepository.deleteById(id);
    }

    private CardTransaction convertToEntity(CardTransactionDto dto) {
        CardTransaction entity = new CardTransaction();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setPaymentId(dto.getPaymentId());
        entity.setDate(dto.getDate());
        entity.setCardNumber(dto.getCardNumber());
        entity.setDateExp(dto.getDateExp());
        entity.setAmount(dto.getAmount());
        entity.setBillingAddress(dto.getBillingAddress());
        entity.setZipCode(dto.getZipCode());
        entity.setCardHolderName(dto.getCardHolderName());
        entity.setState(dto.getState());
        entity.setRegion(dto.getRegion());
        return entity;
    }

    private CardTransactionDto convertToDto(CardTransaction entity) {
        return CardTransactionDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .paymentId(entity.getPaymentId())
                .date(entity.getDate())
                .cardNumber(maskCardNumber(entity.getCardNumber()))
                .dateExp(entity.getDateExp())
                .amount(entity.getAmount())
                .billingAddress(entity.getBillingAddress())
                .zipCode(entity.getZipCode())
                .cardHolderName(entity.getCardHolderName())
                .state(entity.getState())
                .region(entity.getRegion())
                .build();
    }

    // Security: Mask all but last 4 digits of card number
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return "XXXX-XXXX-XXXX-" + lastFourDigits;
    }
}
