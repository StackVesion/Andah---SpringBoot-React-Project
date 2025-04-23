package com.andah.paymentservice.controller;

import com.andah.paymentservice.dto.PaymentTransactionDto;
import com.andah.paymentservice.exception.ResourceNotFoundException;
import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.model.PaymentTransaction.PaymentMethod;
import com.andah.paymentservice.model.PaymentTransaction.PaymentStatus;
import com.andah.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create a new payment
     * @param paymentDto Payment information
     * @return Created payment details
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentTransactionDto paymentDto) {
        try {
            // VALIDATION - First check
            logger.info("Received payment request: " + paymentDto.toString());
            
            if (paymentDto.getAmount() == null || paymentDto.getAmount() <= 0.0) {
                return createErrorResponse("Payment amount must be greater than zero");
            }
            
            if (paymentDto.getUserId() == null) {
                return createErrorResponse("User ID is required");
            }
            
            if (paymentDto.getPaymentMethod() == null) {
                return createErrorResponse("Payment method is required");
            }
            
            // Payment method specific validations
            switch (paymentDto.getPaymentMethod()) {
                case CREDIT_CARD:
                    if (paymentDto.getCardTransactionId() == null || paymentDto.getCardTransactionId().isEmpty()) {
                        return createErrorResponse("Card transaction ID is required for credit card payments");
                    }
                    break;
                case CRYPTO:
                    if (paymentDto.getCryptoTransactionId() == null || paymentDto.getCryptoTransactionId().isEmpty()) {
                        return createErrorResponse("Crypto transaction ID is required for cryptocurrency payments");
                    }
                    if (paymentDto.getCurrency() == null || paymentDto.getCurrency().isEmpty()) {
                        return createErrorResponse("Currency is required for cryptocurrency payments");
                    }
                    break;
                case WALLET:
                    if (paymentDto.getWalletTransactionId() == null || paymentDto.getWalletTransactionId().isEmpty()) {
                        return createErrorResponse("Wallet transaction ID is required for wallet payments");
                    }
                    break;
                default:
                    // CASH doesn't require specific validation
                    break;
            }
            
            // If we reach here, all validations have passed
            logger.info("Payment validation passed, creating payment");
            PaymentTransactionDto savedPayment = paymentService.createPayment(paymentDto);
            return new ResponseEntity<>(savedPayment, HttpStatus.CREATED);
            
        } catch (Exception e) {
            logger.error("Error creating payment: " + e.getMessage(), e);
            return createErrorResponse("Error creating payment: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create consistent error responses
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        logger.warn("Payment validation failed: " + message);
        
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", message);
        errorResponse.put("path", "/api/payments");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Get all payments
     * @return List of all payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentTransactionDto>> getAllPayments() {
        List<PaymentTransactionDto> payments = paymentService.getAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    /**
     * Get payment by ID
     * @param id Payment ID
     * @return Payment details if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable String id) {
        // Get payment with related transaction details based on payment method
        Map<String, Object> paymentDetails = paymentService.getPaymentByIdWithDetails(id);
        
        // Check if payment was found
        if (paymentDetails.isEmpty() || !paymentDetails.containsKey("payment")) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        
        return new ResponseEntity<>(paymentDetails, HttpStatus.OK);
    }

    /**
     * Get payment details including associated transaction information
     * @param id Payment ID
     * @return Payment details with associated transactions
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getPaymentWithDetails(@PathVariable String id) {
        try {
            Optional<PaymentTransactionDto> paymentOpt = paymentService.getPaymentById(id);
            if (!paymentOpt.isPresent()) {
                return new ResponseEntity<>("Payment not found with id: " + id, HttpStatus.NOT_FOUND);
            }
            
            PaymentTransactionDto paymentTransaction = paymentOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("payment", paymentTransaction);
            
            // Check payment method and include relevant transaction details
            PaymentMethod paymentMethod = paymentTransaction.getPaymentMethod();
            if (paymentMethod != null) {
                switch (paymentMethod) {
                    case CREDIT_CARD:
                        if (paymentTransaction.getCardTransactionId() != null) {
                            // Assuming there's a service to get card transaction details
                            // CardTransactionDto cardTransaction = cardTransactionService.getCardTransactionById(paymentTransaction.getCardTransactionId());
                            // if (cardTransaction != null) {
                            //     result.put("cardTransaction", cardTransaction);
                            // }
                            result.put("info", "Payment was made using credit card");
                        }
                        break;
                    case CRYPTO:
                        if (paymentTransaction.getCryptoTransactionId() != null) {
                            // Assuming there's a service to get crypto transaction details
                            // CryptoTransactionDto cryptoTransaction = cryptoTransactionService.getCryptoTransactionById(paymentTransaction.getCryptoTransactionId());
                            // if (cryptoTransaction != null) {
                            //     result.put("cryptoTransaction", cryptoTransaction);
                            // }
                            result.put("info", "Payment was made using crypto");
                        }
                        break;
                    case WALLET:
                        if (paymentTransaction.getWalletTransactionId() != null) {
                            // Assuming there's a service to get wallet transaction details
                            // WalletTransactionDto walletTransaction = walletService.getWalletTransactionById(paymentTransaction.getWalletTransactionId());
                            // if (walletTransaction != null) {
                            //     result.put("walletTransaction", walletTransaction);
                            // }
                            result.put("info", "Payment was made using wallet");
                        }
                        break;
                    case CASH:
                        result.put("cashPayment", true);
                        result.put("info", "Cash payment does not have additional transaction details");
                        break;
                    default:
                        result.put("info", "No additional details available for payment method: " + paymentMethod);
                }
            }
            
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving payment details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payments by user ID
     * @param userId User ID
     * @return List of payments for the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentTransactionDto>> getPaymentsByUserId(@PathVariable Long userId) {
        List<PaymentTransactionDto> payments = paymentService.getPaymentsByUserId(userId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    /**
     * Get payments by reservation ID
     * @param reservationId Reservation ID
     * @return List of payments for the specified reservation
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentTransactionDto>> getPaymentsByReservationId(@PathVariable Long reservationId) {
        List<PaymentTransactionDto> payments = paymentService.getPaymentsByReservationId(reservationId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
    
    /**
     * Get payments by status
     * @param status Payment status (PENDING, COMPLETED, FAILED, REFUNDED)
     * @return List of payments with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentTransactionDto>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentTransactionDto> payments = paymentService.getPaymentsByStatus(status);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
    
    /**
     * Update payment status
     * @param id Payment ID
     * @param statusUpdate Status update request object
     * @return Updated payment details
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        
        String statusValue = statusUpdate.get("status");
        if (statusValue == null) {
            return createErrorResponse("Status value is required");
        }
        
        try {
            PaymentStatus status = PaymentStatus.valueOf(statusValue);
            PaymentTransactionDto updatedPayment = paymentService.updatePaymentStatus(id, status);
            return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return createErrorResponse("Invalid payment status: " + statusValue);
        }
    }
    
    /**
     * Update payment status by direct path variable
     * @param id Payment ID
     * @param status Status to update to (PENDING, COMPLETED, FAILED, REFUNDED)
     * @return Updated payment details
     */
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @PathVariable String status) {
        try {
            Optional<PaymentTransactionDto> paymentOpt = paymentService.getPaymentById(id);
            if (!paymentOpt.isPresent()) {
                return new ResponseEntity<>("Payment not found with id: " + id, HttpStatus.NOT_FOUND);
            }
            
            PaymentTransactionDto paymentTransaction = paymentOpt.get();
            // Convert string status to enum
            PaymentTransaction.PaymentStatus paymentStatus;
            try {
                paymentStatus = PaymentTransaction.PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>("Invalid payment status: " + status, HttpStatus.BAD_REQUEST);
            }
            
            paymentTransaction.setStatus(paymentStatus);
            PaymentTransactionDto updatedPayment = paymentService.updatePayment(paymentTransaction);
            
            return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating payment status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete a payment
     * @param id Payment ID to delete
     * @return Empty response with 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        // Verify payment exists before deleting
        paymentService.getPaymentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
                
        paymentService.deletePayment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Process a refund for a payment
     * @param id Payment ID to refund
     * @return Updated payment details after refund
     */
    @PostMapping("/refund/{id}")
    public ResponseEntity<PaymentTransactionDto> refundPayment(@PathVariable String id) {
        PaymentTransactionDto payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
                
        PaymentTransactionDto refundedPayment = paymentService.updatePaymentStatus(id, PaymentStatus.REFUNDED);
        return new ResponseEntity<>(refundedPayment, HttpStatus.OK);
    }
}
