package com.andah.paymentservice.controller;

import com.andah.paymentservice.dto.CardTransactionDto;
import com.andah.paymentservice.dto.PaymentTransactionDto;
import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.service.CardTransactionService;
import com.andah.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/card-transactions")
public class CardTransactionController {

    private final CardTransactionService cardTransactionService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    public CardTransactionController(CardTransactionService cardTransactionService, 
                                      PaymentService paymentService,
                                      ObjectMapper objectMapper) {
        this.cardTransactionService = cardTransactionService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> createCardTransaction(@RequestBody CardTransactionDto cardTransactionDto) {
        try {
            // Validate required fields
            if (cardTransactionDto.getUserId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User ID is required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            
            if (cardTransactionDto.getAmount() == null || cardTransactionDto.getAmount() <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Amount is required and must be greater than zero");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
            
            // Process Stripe payment
            String chargeId = null;
            String chargeStatus = null;
            Boolean chargePaid = false;
                
            try {
                // Initialize Stripe
                Stripe.apiKey = stripeApiKey;
                
                // Use a request options object for thread safety
                RequestOptions requestOptions = RequestOptions.builder()
                        .setApiKey(stripeApiKey)
                        .build();
                
                // Use test token for Stripe testing
                String tokenId = "tok_visa"; // Default test token that always succeeds
                
                // Create charge parameters
                Map<String, Object> chargeParams = new HashMap<>();
                long amountInCents = Math.round(cardTransactionDto.getAmount() * 100);
                chargeParams.put("amount", amountInCents);
                chargeParams.put("currency", cardTransactionDto.getCurrency() != null ? 
                                 cardTransactionDto.getCurrency().toLowerCase() : "usd");
                chargeParams.put("source", tokenId);
                chargeParams.put("description", "Payment for user ID: " + cardTransactionDto.getUserId());

                // Add metadata if we have customer information
                if (cardTransactionDto.getCardHolderName() != null) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("customer_name", cardTransactionDto.getCardHolderName());
                    chargeParams.put("metadata", metadata);
                }

                // Process charge with Stripe
                Charge charge = Charge.create(chargeParams, requestOptions);
                chargeId = charge.getId();
                chargeStatus = charge.getStatus();
                chargePaid = charge.getPaid();
                
            } catch (StripeException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error processing Stripe payment: " + e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            // Update card transaction with Stripe data
            cardTransactionDto.setTransactionReference(chargeId);
            cardTransactionDto.setStatus(chargeStatus.toUpperCase());
            cardTransactionDto.setPaymentProcessor("STRIPE");
            
            // Save the card transaction
            CardTransactionDto savedTransaction = cardTransactionService.createCardTransaction(cardTransactionDto);
            
            // Create payment record
            PaymentTransactionDto paymentTransaction = new PaymentTransactionDto();
            paymentTransaction.setUserId(cardTransactionDto.getUserId());
            paymentTransaction.setAmount(cardTransactionDto.getAmount());
            paymentTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.CREDIT_CARD);
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
            paymentTransaction.setCardTransactionId(savedTransaction.getId());
            paymentTransaction.setTransactionReference("STRIPE-" + chargeId);
            paymentTransaction.setCurrency(cardTransactionDto.getCurrency() != null ? 
                                    cardTransactionDto.getCurrency() : "USD");
            
            // Save the payment transaction
            PaymentTransactionDto savedPayment = paymentService.createPayment(paymentTransaction);
            
            // Return success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Card payment processed successfully");
            successResponse.put("cardTransaction", savedTransaction);
            successResponse.put("paymentTransaction", savedPayment);
            
            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing card payment: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<CardTransactionDto>> getAllCardTransactions() {
        List<CardTransactionDto> transactions = cardTransactionService.getAllCardTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardTransactionDto> getCardTransactionById(@PathVariable String id) {
        return cardTransactionService.getCardTransactionById(id)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardTransactionDto>> getCardTransactionsByUserId(@PathVariable Long userId) {
        List<CardTransactionDto> transactions = cardTransactionService.getCardTransactionsByUserId(userId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<CardTransactionDto>> getCardTransactionsByPaymentId(@PathVariable String paymentId) {
        List<CardTransactionDto> transactions = cardTransactionService.getCardTransactionsByPaymentId(paymentId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardTransaction(@PathVariable String id) {
        cardTransactionService.deleteCardTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
