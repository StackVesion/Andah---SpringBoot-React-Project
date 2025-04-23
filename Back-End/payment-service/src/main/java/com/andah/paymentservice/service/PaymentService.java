package com.andah.paymentservice.service;

import com.andah.paymentservice.dto.CardTransactionDto;
import com.andah.paymentservice.dto.CryptoTransactionDto;
import com.andah.paymentservice.dto.PaymentTransactionDto;
import com.andah.paymentservice.dto.WalletDto;
import com.andah.paymentservice.exception.ResourceNotFoundException;
import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.model.PaymentTransaction.PaymentMethod;
import com.andah.paymentservice.model.PaymentTransaction.PaymentStatus;
import com.andah.paymentservice.model.Wallet;
import com.andah.paymentservice.model.WalletTransaction;
import com.andah.paymentservice.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.HashMap;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final CardTransactionService cardTransactionService;
    private final CryptoTransactionService cryptoTransactionService;
    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;

    @Autowired
    public PaymentService(PaymentTransactionRepository paymentRepository,
                         CardTransactionService cardTransactionService,
                         CryptoTransactionService cryptoTransactionService,
                         WalletService walletService,
                         WalletTransactionService walletTransactionService) {
        this.paymentRepository = paymentRepository;
        this.cardTransactionService = cardTransactionService;
        this.cryptoTransactionService = cryptoTransactionService;
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
    }

    @Transactional
    public PaymentTransactionDto createPayment(PaymentTransactionDto paymentDto) {
        // Additional validation based on payment method (controller already does basic validation)
        validatePaymentDetails(paymentDto);
        
        // Create the main payment transaction
        PaymentTransaction payment = convertToEntity(paymentDto);
        payment.setTimestamp(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        return convertToDto(payment);
    }
    
    /**
     * Additional validation of payment details beyond the basic payment method validation
     */
    private void validatePaymentDetails(PaymentTransactionDto paymentDto) {
        // Make sure amount is positive
        if (paymentDto.getAmount() == null || paymentDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        
        // Additional validations based on payment method
        if (paymentDto.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            if (paymentDto.getCardTransactionId() == null) {
                throw new IllegalArgumentException("Card transaction ID is required for credit card payments");
            }
        } else if (paymentDto.getPaymentMethod() == PaymentMethod.CRYPTO) {
            if (paymentDto.getCryptoTransactionId() == null) {
                throw new IllegalArgumentException("Crypto transaction ID is required for cryptocurrency payments");
            }
            if (paymentDto.getCurrency() == null || paymentDto.getCurrency().trim().isEmpty()) {
                throw new IllegalArgumentException("Currency is required for cryptocurrency payments");
            }
        } else if (paymentDto.getPaymentMethod() == PaymentMethod.WALLET) {
            if (paymentDto.getWalletTransactionId() == null) {
                throw new IllegalArgumentException("Wallet transaction ID is required for wallet payments");
            }
        }
        // CASH payments don't require any additional validation
    }
    
    @Transactional
    public PaymentTransactionDto createPaymentWithWallet(Long userId, Double amount, Long reservationId, Long scooterId) {
        // Validate inputs
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Get user's wallet
        var wallet = walletService.getWalletByUserId(userId);
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet", "userId", userId.toString());
        }
        
        // Check if wallet has sufficient funds
        if (wallet.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds in wallet. Current balance: " + wallet.getBalance());
        }
        
        // Create wallet transaction (withdrawal)
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setWalletId(wallet.getId());
        walletTransaction.setType(WalletTransaction.WalletTransactionType.WITHDRAWAL);
        walletTransaction.setAmount(amount);
        walletTransaction.setDate(LocalDateTime.now());
        walletTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
        
        // Update wallet balance
        walletService.withdrawFromWallet(wallet.getId(), amount);
        
        // Create payment transaction
        PaymentTransactionDto paymentDto = new PaymentTransactionDto();
        paymentDto.setUserId(userId);
        paymentDto.setReservationId(reservationId);
        paymentDto.setScooterId(scooterId);
        paymentDto.setAmount(amount);
        paymentDto.setPaymentMethod(PaymentMethod.WALLET);
        paymentDto.setStatus(PaymentStatus.COMPLETED);
        paymentDto.setTimestamp(LocalDateTime.now());
        paymentDto.setWalletTransactionId(walletTransaction.getId());
        paymentDto.setTransactionReference("WALLET-" + System.currentTimeMillis());
        
        return createPayment(paymentDto);
    }
    
    @Transactional
    public PaymentTransactionDto rechargeWallet(Long userId, Double amount, PaymentMethod paymentMethod, 
                                               CardTransactionDto cardDto, CryptoTransactionDto cryptoDto) {
        // Validate inputs
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Recharge amount must be greater than zero");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Validate payment method
        if (paymentMethod != PaymentMethod.CREDIT_CARD && paymentMethod != PaymentMethod.CRYPTO) {
            throw new IllegalArgumentException("Wallet can only be recharged with credit card or crypto");
        }
        
        // Get or create user's wallet
        var wallet = walletService.getOrCreateWallet(userId);
        
        // Create the payment transaction record first
        PaymentTransactionDto paymentDto = new PaymentTransactionDto();
        paymentDto.setUserId(userId);
        paymentDto.setAmount(amount);
        paymentDto.setPaymentMethod(paymentMethod);
        paymentDto.setStatus(PaymentStatus.COMPLETED);
        paymentDto.setTimestamp(LocalDateTime.now());
        paymentDto.setTransactionReference("WALLET-RECHARGE-" + System.currentTimeMillis());
        
        // Process based on payment method
        String cardTransactionId = null;
        String cryptoTransactionId = null;
        
        if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            // Validate card details
            if (cardDto == null) {
                throw new IllegalArgumentException("Card details are required for card payments");
            }
            
            // Set basic card transaction details
            cardDto.setUserId(userId);
            cardDto.setDate(LocalDateTime.now());
            cardDto.setAmount(amount);
            
            // Create card transaction
            var cardTransaction = cardTransactionService.createCardTransaction(cardDto);
            cardTransactionId = cardTransaction.getId();
            paymentDto.setCardTransactionId(cardTransactionId);
        } else if (paymentMethod == PaymentMethod.CRYPTO) {
            // Validate crypto details
            if (cryptoDto == null) {
                throw new IllegalArgumentException("Crypto details are required for crypto payments");
            }
            
            // Set basic crypto transaction details
            cryptoDto.setUserId(userId);
            cryptoDto.setDate(LocalDateTime.now());
            cryptoDto.setPriceAmount(amount);
            cryptoDto.setPaymentStatus(PaymentStatus.COMPLETED.toString());
            
            // Create crypto transaction
            var cryptoTransaction = cryptoTransactionService.createCryptoTransaction(cryptoDto);
            cryptoTransactionId = cryptoTransaction.getId();
            paymentDto.setCryptoTransactionId(cryptoTransactionId);
            
            // Set currency if provided
            if (cryptoDto.getPayCurrency() != null) {
                paymentDto.setCurrency(cryptoDto.getPayCurrency());
            }
        }
        
        // Create payment transaction
        PaymentTransaction payment = convertToEntity(paymentDto);
        payment.setTimestamp(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        // Now create wallet transaction and update wallet balance
        wallet = walletService.depositToWallet(wallet.getId(), amount, cardTransactionId, cryptoTransactionId);
        
        return convertToDto(payment);
    }
    
    private void createCardTransaction(PaymentTransaction payment) {
        CardTransactionDto cardDto = new CardTransactionDto();
        cardDto.setUserId(payment.getUserId());
        cardDto.setPaymentId(payment.getId());
        cardDto.setDate(payment.getTimestamp());
        cardDto.setAmount(payment.getAmount());
        // Other card details would normally come from the payment request
        // For now, we set defaults or leave as null
        cardTransactionService.createCardTransaction(cardDto);
    }
    
    private void createCryptoTransaction(PaymentTransaction payment) {
        CryptoTransactionDto cryptoDto = new CryptoTransactionDto();
        cryptoDto.setUserId(payment.getUserId());
        cryptoDto.setPaymentId(payment.getId());
        cryptoDto.setDate(payment.getTimestamp());
        cryptoDto.setPriceAmount(payment.getAmount());
        cryptoDto.setPaymentStatus(payment.getStatus().toString());
        // Other crypto details would normally come from the payment request
        // For now, we set defaults or leave as null
        cryptoTransactionService.createCryptoTransaction(cryptoDto);
    }

    public List<PaymentTransactionDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a payment transaction by ID
     * 
     * @param id The payment transaction ID
     * @return Optional containing the payment transaction DTO if found, empty otherwise
     */
    public Optional<PaymentTransactionDto> getPaymentById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        
        return paymentRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<PaymentTransactionDto> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PaymentTransactionDto> getPaymentsByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<PaymentTransactionDto> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PaymentTransactionDto updatePaymentStatus(String id, PaymentStatus status) {
        PaymentTransaction payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
                
        payment.setStatus(status);
        payment = paymentRepository.save(payment);
        return convertToDto(payment);
    }
    
    @Transactional
    public void deletePayment(String id) {
        // Check if payment exists
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        paymentRepository.deleteById(id);
    }

    /**
     * Get payment by ID with related transaction details based on payment method
     * @param id Payment ID
     * @return Payment with related transaction details
     */
    public Map<String, Object> getPaymentByIdWithDetails(String id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<PaymentTransaction> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isEmpty()) {
            return response; // Empty response if payment not found
        }
        
        PaymentTransaction payment = paymentOpt.get();
        PaymentTransactionDto paymentDto = convertToDto(payment);
        response.put("payment", paymentDto);
        
        // Add related transaction details based on payment method
        switch (payment.getPaymentMethod()) {
            case CREDIT_CARD:
                if (payment.getCardTransactionId() != null && !payment.getCardTransactionId().isEmpty()) {
                    cardTransactionService.getCardTransactionById(payment.getCardTransactionId())
                            .ifPresent(cardTrans -> response.put("cardTransaction", cardTrans));
                }
                break;
                
            case CRYPTO:
                if (payment.getCryptoTransactionId() != null && !payment.getCryptoTransactionId().isEmpty()) {
                    cryptoTransactionService.getCryptoTransactionById(payment.getCryptoTransactionId())
                            .ifPresent(cryptoTrans -> response.put("cryptoTransaction", cryptoTrans));
                }
                break;
                
            case WALLET:
                if (payment.getWalletTransactionId() != null && !payment.getWalletTransactionId().isEmpty()) {
                    walletTransactionService.getWalletTransactionById(payment.getWalletTransactionId())
                            .ifPresent(walletTrans -> response.put("walletTransaction", walletTrans));
                    
                    // Optionally add wallet balance information
                    if (payment.getUserId() != null) {
                        WalletDto wallet = walletService.getWalletByUserId(payment.getUserId());
                        if (wallet != null) {
                            response.put("wallet", wallet);
                        }
                    }
                }
                break;
                
            default:
                // No additional details for CASH payments
                break;
        }
        
        return response;
    }

    /**
     * Create a payment record for a crypto transaction
     * 
     * @param cryptoTransactionDto The DTO of the crypto transaction
     * @return The created payment transaction DTO
     */
    @Transactional
    public PaymentTransactionDto createPaymentFromCryptoTransaction(CryptoTransactionDto cryptoTransactionDto) {
        if (cryptoTransactionDto == null) {
            throw new IllegalArgumentException("Crypto transaction cannot be null");
        }
        
        if (cryptoTransactionDto.getId() == null) {
            throw new IllegalArgumentException("Crypto transaction ID cannot be null");
        }
        
        // Create the payment transaction
        PaymentTransactionDto paymentDto = new PaymentTransactionDto();
        paymentDto.setUserId(cryptoTransactionDto.getUserId());
        paymentDto.setAmount(cryptoTransactionDto.getPriceAmount());
        paymentDto.setPaymentMethod(PaymentTransaction.PaymentMethod.CRYPTO);
        
        // Set status based on crypto transaction status
        String cryptoStatus = cryptoTransactionDto.getPaymentStatus();
        if ("CONFIRMED".equalsIgnoreCase(cryptoStatus) || "FINISHED".equalsIgnoreCase(cryptoStatus)) {
            paymentDto.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
        } else if ("WAITING".equalsIgnoreCase(cryptoStatus) || "PENDING".equalsIgnoreCase(cryptoStatus)) {
            paymentDto.setStatus(PaymentTransaction.PaymentStatus.PENDING);
        } else if ("EXPIRED".equalsIgnoreCase(cryptoStatus) || "FAILED".equalsIgnoreCase(cryptoStatus)) {
            paymentDto.setStatus(PaymentTransaction.PaymentStatus.FAILED);
        } else {
            paymentDto.setStatus(PaymentTransaction.PaymentStatus.PENDING); // Default to pending
        }
        
        paymentDto.setTimestamp(LocalDateTime.now());
        paymentDto.setCryptoTransactionId(cryptoTransactionDto.getId());
        paymentDto.setCurrency(cryptoTransactionDto.getPriceCurrency());
        paymentDto.setTransactionReference("CRYPTO-" + cryptoTransactionDto.getNowPaymentId());
        
        // Save the payment and get the DTO with the generated ID
        PaymentTransactionDto createdPayment = createPayment(paymentDto);
        
        return createdPayment;
    }

    /**
     * Find all payment transactions linked to a specific crypto transaction
     * 
     * @param cryptoTransactionId The ID of the crypto transaction
     * @return List of payment transactions linked to the crypto transaction
     */
    public List<PaymentTransactionDto> getPaymentsByCryptoTransactionId(String cryptoTransactionId) {
        if (cryptoTransactionId == null) {
            throw new IllegalArgumentException("Crypto transaction ID cannot be null");
        }
        
        List<PaymentTransaction> payments = paymentRepository.findByCryptoTransactionId(cryptoTransactionId);
        return payments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing payment transaction
     * 
     * @param paymentDto The payment transaction DTO with updated values
     * @return The updated payment transaction DTO
     */
    @Transactional
    public PaymentTransactionDto updatePayment(PaymentTransactionDto paymentDto) {
        if (paymentDto == null || paymentDto.getId() == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        
        PaymentTransaction payment = paymentRepository.findById(paymentDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentDto.getId()));
        
        // Update all fields from DTO
        payment.setStatus(paymentDto.getStatus());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setAmount(paymentDto.getAmount());
        payment.setTransactionReference(paymentDto.getTransactionReference());
        payment.setUserId(paymentDto.getUserId());
        payment.setScooterId(paymentDto.getScooterId());
        payment.setReservationId(paymentDto.getReservationId());
        payment.setCardTransactionId(paymentDto.getCardTransactionId());
        payment.setCryptoTransactionId(paymentDto.getCryptoTransactionId());
        payment.setWalletTransactionId(paymentDto.getWalletTransactionId());
        payment.setCurrency(paymentDto.getCurrency());
        
        payment = paymentRepository.save(payment);
        return convertToDto(payment);
    }

    /**
     * Process wallet deposit (recharge)
     * 
     * @param userId The user ID
     * @param amount The amount to deposit
     * @param cardTransactionId Optional card transaction ID if deposit was made via card
     * @param cryptoTransactionId Optional crypto transaction ID if deposit was made via crypto
     * @return The payment transaction DTO
     */
    @Transactional
    public PaymentTransactionDto processWalletDeposit(Long userId, Double amount, String cardTransactionId, String cryptoTransactionId) {
        // Get or create wallet
        Wallet wallet = walletService.getOrCreateWallet(userId);
        
        // Prepare payment DTO
        PaymentTransactionDto paymentDto = new PaymentTransactionDto();
        paymentDto.setUserId(userId);
        paymentDto.setAmount(amount);
        paymentDto.setPaymentMethod(PaymentMethod.WALLET);
        paymentDto.setStatus(PaymentStatus.COMPLETED);
        
        // Set relevant transaction IDs
        if (cardTransactionId != null) {
            paymentDto.setCardTransactionId(cardTransactionId);
            
            // Check if there's a card transaction to get currency
            cardTransactionService.getCardTransactionById(cardTransactionId).ifPresent(cardDto -> {
                if (cardDto.getCurrency() != null) {
                    paymentDto.setCurrency(cardDto.getCurrency());
                } else {
                    // Set a default currency if not specified
                    paymentDto.setCurrency("USD");
                }
            });
        } else if (cryptoTransactionId != null) {
            paymentDto.setCryptoTransactionId(cryptoTransactionId);
            
            // Check if there's a crypto transaction to get currency
            cryptoTransactionService.getCryptoTransactionById(cryptoTransactionId).ifPresent(cryptoDto -> {
                if (cryptoDto.getPayCurrency() != null) {
                    paymentDto.setCurrency(cryptoDto.getPayCurrency());
                }
            });
        }
        
        // Create payment transaction
        PaymentTransaction payment = convertToEntity(paymentDto);
        payment.setTimestamp(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        // Now create wallet transaction and update wallet balance
        wallet = walletService.depositToWallet(wallet.getId(), amount, cardTransactionId, cryptoTransactionId);
        
        return convertToDto(payment);
    }

    private PaymentTransaction convertToEntity(PaymentTransactionDto dto) {
        PaymentTransaction payment = new PaymentTransaction();
        payment.setId(dto.getId());
        payment.setUserId(dto.getUserId());
        payment.setReservationId(dto.getReservationId());
        payment.setScooterId(dto.getScooterId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(dto.getStatus());
        payment.setTimestamp(dto.getTimestamp());
        payment.setTransactionReference(dto.getTransactionReference());
        
        // Set the transaction IDs
        payment.setCardTransactionId(dto.getCardTransactionId());
        payment.setCryptoTransactionId(dto.getCryptoTransactionId());
        payment.setWalletTransactionId(dto.getWalletTransactionId());
        payment.setCurrency(dto.getCurrency());
        
        return payment;
    }

    private PaymentTransactionDto convertToDto(PaymentTransaction payment) {
        PaymentTransactionDto dto = new PaymentTransactionDto();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUserId());
        dto.setReservationId(payment.getReservationId());
        dto.setScooterId(payment.getScooterId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus());
        dto.setTimestamp(payment.getTimestamp());
        dto.setTransactionReference(payment.getTransactionReference());
        
        // Set the transaction IDs
        dto.setCardTransactionId(payment.getCardTransactionId());
        dto.setCryptoTransactionId(payment.getCryptoTransactionId());
        dto.setWalletTransactionId(payment.getWalletTransactionId());
        dto.setCurrency(payment.getCurrency());
        
        return dto;
    }
}
