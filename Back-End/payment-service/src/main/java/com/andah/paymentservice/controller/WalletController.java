package com.andah.paymentservice.controller;

import com.andah.paymentservice.dto.PaymentTransactionDto;
import com.andah.paymentservice.dto.WalletDto;
import com.andah.paymentservice.dto.WalletTransactionDto;
import com.andah.paymentservice.dto.CardTransactionDto;
import com.andah.paymentservice.dto.CryptoTransactionDto;
import com.andah.paymentservice.exception.ResourceNotFoundException;
import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.model.Wallet;
import com.andah.paymentservice.model.WalletTransaction;
import com.andah.paymentservice.service.CardTransactionService;
import com.andah.paymentservice.service.CryptoTransactionService;
import com.andah.paymentservice.service.PaymentService;
import com.andah.paymentservice.service.WalletService;
import com.andah.paymentservice.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final PaymentService paymentService;
    private final WalletTransactionService walletTransactionService;
    private final CardTransactionService cardTransactionService;
    private final CryptoTransactionService cryptoTransactionService;

    @Autowired
    public WalletController(WalletService walletService, PaymentService paymentService, WalletTransactionService walletTransactionService, CardTransactionService cardTransactionService, CryptoTransactionService cryptoTransactionService) {
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.walletTransactionService = walletTransactionService;
        this.cardTransactionService = cardTransactionService;
        this.cryptoTransactionService = cryptoTransactionService;
    }

    /**
     * Create a new wallet for a user
     * @param userId The user ID
     * @return The created wallet
     */
    @PostMapping("/{userId}/create")
    public ResponseEntity<?> createWallet(@PathVariable Long userId) {
        try {
            // Check if wallet already exists
            WalletDto existingWallet = walletService.getWalletByUserId(userId);
            
            if (existingWallet != null) {
                return new ResponseEntity<>(Map.of(
                    "message", "Wallet already exists for user: " + userId,
                    "wallet", existingWallet
                ), HttpStatus.OK);
            }
            
            // Create a new wallet with 0.0 balance
            Wallet newWallet = new Wallet();
            newWallet.setUserId(userId);
            newWallet.setBalance(0.0);
            Wallet savedWallet = walletService.getOrCreateWallet(userId);
            
            WalletDto walletDto = new WalletDto();
            walletDto.setId(savedWallet.getId());
            walletDto.setUserId(savedWallet.getUserId());
            walletDto.setBalance(savedWallet.getBalance());
            
            return new ResponseEntity<>(Map.of(
                "message", "Wallet created successfully",
                "wallet", walletDto
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a wallet by user ID
     * @param userId The user ID
     * @return The wallet if found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getWalletByUserId(@PathVariable Long userId) {
        try {
            WalletDto wallet = walletService.getWalletByUserId(userId);
            if (wallet == null) {
                return new ResponseEntity<>(Map.of("error", "Wallet not found for user: " + userId), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get wallet balance by user ID
     * @param userId The user ID
     * @return The wallet balance
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<?> getWalletBalance(@PathVariable Long userId) {
        try {
            Wallet wallet = walletService.getOrCreateWallet(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("balance", wallet.getBalance());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recharge wallet with card payment
     * @param userId User ID
     * @param requestBody Request containing cardTransactionId and amount
     * @return Updated wallet information
     */
    @PostMapping("/{userId}/recharge/card")
    public ResponseEntity<?> rechargeWalletWithCard(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestBody) {
            
        // Validate required fields
        if (requestBody == null) {
            return new ResponseEntity<>(Map.of("error", "Request body is required"), HttpStatus.BAD_REQUEST);
        }
        
        if (!requestBody.containsKey("cardTransactionId") || requestBody.get("cardTransactionId") == null) {
            return new ResponseEntity<>(Map.of("error", "cardTransactionId is required"), HttpStatus.BAD_REQUEST);
        }
        
        String cardTransactionId = requestBody.get("cardTransactionId").toString();
        
        // Validate amount
        if (!requestBody.containsKey("amount") || requestBody.get("amount") == null) {
            return new ResponseEntity<>(Map.of("error", "Amount is required"), HttpStatus.BAD_REQUEST);
        }
        
        double amount;
        try {
            amount = Double.parseDouble(requestBody.get("amount").toString());
            if (amount <= 0) {
                return new ResponseEntity<>(Map.of("error", "Amount must be greater than zero"), HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(Map.of("error", "Invalid amount format"), HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Verify card transaction exists
            CardTransactionDto cardTransaction = cardTransactionService.getCardTransactionById(cardTransactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Card transaction not found: " + cardTransactionId));

            // Get user's wallet (creates if it doesn't exist)
            Wallet wallet = walletService.getOrCreateWallet(userId);
            
            // Create wallet transaction record
            WalletTransactionDto walletTransaction = new WalletTransactionDto();
            walletTransaction.setWalletId(wallet.getId());
            walletTransaction.setType(WalletTransaction.WalletTransactionType.DEPOSIT);
            walletTransaction.setAmount(amount);
            walletTransaction.setDate(LocalDateTime.now());
            walletTransaction.setDescription("Card recharge: " + cardTransactionId);
            walletTransaction.setCardTransactionId(cardTransactionId);
            
            // Save wallet transaction
            WalletTransactionDto savedTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
            
            // Update wallet balance
            wallet = walletService.depositToWallet(wallet.getId(), amount, cardTransactionId, null);
            
            // Create payment transaction record
            PaymentTransactionDto paymentTransaction = new PaymentTransactionDto();
            paymentTransaction.setUserId(userId);
            paymentTransaction.setAmount(amount);
            paymentTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.CREDIT_CARD);
            paymentTransaction.setTransactionReference("WALLET-RECHARGE-" + savedTransaction.getId());
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
            paymentTransaction.setCardTransactionId(cardTransactionId);
            paymentTransaction.setWalletTransactionId(savedTransaction.getId());
            paymentTransaction.setCurrency("USD"); // Default to USD for card transactions
            
            PaymentTransactionDto savedPayment = paymentService.createPayment(paymentTransaction);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Wallet recharged successfully with card payment");
            response.put("walletTransaction", savedTransaction);
            response.put("payment", savedPayment);
            response.put("newBalance", wallet.getBalance());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Recharge wallet with crypto payment
     * @param userId User ID
     * @param requestBody Request containing cryptoTransactionId and amount
     * @return Updated wallet information
     */
    @PostMapping("/{userId}/recharge/crypto")
    public ResponseEntity<?> rechargeWalletWithCrypto(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestBody) {
            
        // Validate required fields
        if (requestBody == null) {
            return new ResponseEntity<>(Map.of("error", "Request body is required"), HttpStatus.BAD_REQUEST);
        }
        
        if (!requestBody.containsKey("cryptoTransactionId") || requestBody.get("cryptoTransactionId") == null) {
            return new ResponseEntity<>(Map.of("error", "cryptoTransactionId is required"), HttpStatus.BAD_REQUEST);
        }
        
        String cryptoTransactionId = requestBody.get("cryptoTransactionId").toString();
        
        // Validate amount
        if (!requestBody.containsKey("amount") || requestBody.get("amount") == null) {
            return new ResponseEntity<>(Map.of("error", "Amount is required"), HttpStatus.BAD_REQUEST);
        }
        
        double amount;
        try {
            amount = Double.parseDouble(requestBody.get("amount").toString());
            if (amount <= 0) {
                return new ResponseEntity<>(Map.of("error", "Amount must be greater than zero"), HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(Map.of("error", "Invalid amount format"), HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Verify crypto transaction exists
            CryptoTransactionDto cryptoTransaction = cryptoTransactionService.getCryptoTransactionById(cryptoTransactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Crypto transaction not found: " + cryptoTransactionId));
            
            // Get the cryptocurrency type from the transaction
            String currency = cryptoTransaction.getPayCurrency();
            if (currency == null || currency.isEmpty()) {
                currency = "BTC"; // Default to BTC if not specified
            }

            // Get user's wallet (creates if it doesn't exist)
            Wallet wallet = walletService.getOrCreateWallet(userId);
            
            // Create wallet transaction record
            WalletTransactionDto walletTransaction = new WalletTransactionDto();
            walletTransaction.setWalletId(wallet.getId());
            walletTransaction.setType(WalletTransaction.WalletTransactionType.DEPOSIT);
            walletTransaction.setAmount(amount);
            walletTransaction.setDate(LocalDateTime.now());
            walletTransaction.setDescription("Crypto recharge: " + cryptoTransactionId);
            walletTransaction.setCryptoTransactionId(cryptoTransactionId);
            
            // Save wallet transaction
            WalletTransactionDto savedTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
            
            // Update wallet balance
            wallet = walletService.depositToWallet(wallet.getId(), amount, null, cryptoTransactionId);
            
            // Create payment transaction record
            PaymentTransactionDto paymentTransaction = new PaymentTransactionDto();
            paymentTransaction.setUserId(userId);
            paymentTransaction.setAmount(amount);
            paymentTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.CRYPTO);
            paymentTransaction.setTransactionReference("WALLET-RECHARGE-" + savedTransaction.getId());
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
            paymentTransaction.setCryptoTransactionId(cryptoTransactionId);
            paymentTransaction.setWalletTransactionId(savedTransaction.getId());
            paymentTransaction.setCurrency(currency);
            
            PaymentTransactionDto savedPayment = paymentService.createPayment(paymentTransaction);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Wallet recharged successfully with crypto payment");
            response.put("walletTransaction", savedTransaction);
            response.put("payment", savedPayment);
            response.put("newBalance", wallet.getBalance());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all wallet transactions for a user
     * @param userId The user ID
     * @return List of wallet transactions
     */
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getWalletTransactions(@PathVariable Long userId) {
        try {
            // Get the wallet
            WalletDto wallet = walletService.getWalletByUserId(userId);
            if (wallet == null) {
                return new ResponseEntity<>(Map.of("error", "Wallet not found for user: " + userId), HttpStatus.NOT_FOUND);
            }
            
            // Get all transactions for the wallet
            List<WalletTransactionDto> transactions = walletTransactionService.getWalletTransactionDtos(wallet.getId());
            
            return new ResponseEntity<>(Map.of(
                "userId", userId,
                "walletId", wallet.getId(),
                "transactions", transactions
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pay with wallet - deducts money from wallet balance
     * @param userId User ID 
     * @param amount Amount to pay (can be in request parameter or body)
     * @param requestBody Optional request body with additional parameters
     * @return Payment confirmation
     */
    @PostMapping("/{userId}/pay")
    public ResponseEntity<?> payWithWallet(
            @PathVariable Long userId,
            @RequestParam(required = false) Double amount,
            @RequestBody(required = false) Map<String, Object> requestBody) {
            
        // Check if amount is in request param, otherwise get from body
        if (amount == null && requestBody != null && requestBody.containsKey("amount")) {
            try {
                amount = Double.parseDouble(requestBody.get("amount").toString());
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(Map.of("error", "Invalid amount format"), HttpStatus.BAD_REQUEST);
            }
        }
        
        // Get reservationId if provided in the request body
        Long reservationId = null;
        if (requestBody != null && requestBody.containsKey("reservationId")) {
            try {
                reservationId = Long.parseLong(requestBody.get("reservationId").toString());
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(Map.of("error", "Invalid reservationId format"), HttpStatus.BAD_REQUEST);
            }
        }

        // Get scooter ID if provided
        Long scooterId = null;
        if (requestBody != null && requestBody.containsKey("scooterId")) {
            try {
                scooterId = Long.parseLong(requestBody.get("scooterId").toString());
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(Map.of("error", "Invalid scooterId format"), HttpStatus.BAD_REQUEST);
            }
        }

        if (amount == null) {
            return new ResponseEntity<>(Map.of("error", "Amount is required"), HttpStatus.BAD_REQUEST);
        }

        try {
            // Get user's wallet
            Wallet wallet = walletService.getOrCreateWallet(userId);

            // Check if wallet has sufficient balance
            if (wallet.getBalance() < amount) {
                return new ResponseEntity<>(
                    Map.of(
                        "success", false,
                        "error", "Insufficient wallet balance",
                        "required", amount,
                        "available", wallet.getBalance()
                    ),
                    HttpStatus.BAD_REQUEST
                );
            }

            // Create wallet transaction
            WalletTransactionDto walletTransaction = new WalletTransactionDto();
            walletTransaction.setWalletId(wallet.getId());
            walletTransaction.setType(WalletTransaction.WalletTransactionType.WITHDRAWAL);
            walletTransaction.setAmount(amount);
            walletTransaction.setDate(LocalDateTime.now());
            walletTransaction.setDescription("Payment for reservation: " + 
                                        (reservationId != null ? reservationId : "N/A") + 
                                        ", scooter: " + 
                                        (scooterId != null ? scooterId : "N/A"));
            
            // Save wallet transaction
            WalletTransactionDto savedTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
            
            // Deduct amount from wallet balance
            double newBalance = wallet.getBalance() - amount;
            wallet.setBalance(newBalance);
            
            // Update wallet using appropriate DTO
            WalletDto walletDto = new WalletDto();
            walletDto.setId(wallet.getId());
            walletDto.setUserId(wallet.getUserId());
            walletDto.setBalance(newBalance);
            WalletDto updatedWallet = walletService.updateWallet(wallet.getId(), walletDto);
            
            // Create payment record
            PaymentTransactionDto paymentTransaction = new PaymentTransactionDto();
            paymentTransaction.setUserId(userId);
            paymentTransaction.setAmount(amount);
            paymentTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.WALLET);
            paymentTransaction.setTransactionReference("WALLET-" + savedTransaction.getId());
            paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
            paymentTransaction.setWalletTransactionId(savedTransaction.getId());
            paymentTransaction.setCurrency("USD"); // Default currency for wallet payments
            
            if (reservationId != null) {
                paymentTransaction.setReservationId(reservationId);
            }
            
            if (scooterId != null) {
                paymentTransaction.setScooterId(scooterId);
            }
            
            // Save the payment transaction
            PaymentTransactionDto savedPayment = paymentService.createPayment(paymentTransaction);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", savedPayment);
            response.put("walletTransaction", savedTransaction);
            response.put("walletBalance", updatedWallet.getBalance());
            response.put("message", "Payment processed successfully from wallet");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                "success", false,
                "error", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Withdraw money from wallet
     * @param userId User ID
     * @param requestBody Contains amount to withdraw
     * @return Withdrawal confirmation
     */
    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<?> withdrawFromWallet(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestBody) {
            
        // Validate required fields
        if (requestBody == null || !requestBody.containsKey("amount")) {
            return new ResponseEntity<>(Map.of("error", "Amount is required"), HttpStatus.BAD_REQUEST);
        }
        
        double amount;
        try {
            amount = Double.parseDouble(requestBody.get("amount").toString());
            if (amount <= 0) {
                return new ResponseEntity<>(Map.of("error", "Amount must be greater than zero"), HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(Map.of("error", "Invalid amount format"), HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Get user's wallet
            Wallet wallet = walletService.getOrCreateWallet(userId);
            
            // Check if wallet has sufficient balance
            if (wallet.getBalance() < amount) {
                return new ResponseEntity<>(
                    Map.of(
                        "success", false,
                        "error", "Insufficient wallet balance",
                        "required", amount,
                        "available", wallet.getBalance()
                    ),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // Get description if provided
            String description = "Withdrawal from wallet";
            if (requestBody.containsKey("description") && requestBody.get("description") != null) {
                description = requestBody.get("description").toString();
            }
            
            // Process the withdrawal
            Wallet updatedWallet = walletService.withdrawFromWallet(wallet.getId(), amount);
            
            // Create wallet transaction
            WalletTransactionDto walletTransaction = new WalletTransactionDto();
            walletTransaction.setWalletId(wallet.getId());
            walletTransaction.setType(WalletTransaction.WalletTransactionType.WITHDRAWAL);
            walletTransaction.setAmount(amount);
            walletTransaction.setDate(LocalDateTime.now());
            walletTransaction.setDescription(description);
            
            // Save wallet transaction
            WalletTransactionDto savedTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Withdrawal processed successfully");
            response.put("walletTransaction", savedTransaction);
            response.put("newBalance", updatedWallet.getBalance());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                "success", false,
                "error", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get withdrawal history for a user
     * @param userId User ID
     * @return List of withdrawal transactions
     */
    @GetMapping("/{userId}/withdrawals")
    public ResponseEntity<?> getWithdrawalHistory(@PathVariable Long userId) {
        try {
            // Get the wallet
            WalletDto wallet = walletService.getWalletByUserId(userId);
            if (wallet == null) {
                return new ResponseEntity<>(Map.of("error", "Wallet not found for user: " + userId), HttpStatus.NOT_FOUND);
            }
            
            // Get withdrawal transactions
            List<WalletTransactionDto> withdrawals = walletTransactionService.getWalletTransactionsByType(
                wallet.getId(), WalletTransaction.WalletTransactionType.WITHDRAWAL);
            
            return new ResponseEntity<>(Map.of(
                "userId", userId,
                "walletId", wallet.getId(),
                "withdrawals", withdrawals
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get deposit history for a user
     * @param userId User ID
     * @return List of deposit transactions
     */
    @GetMapping("/{userId}/deposits")
    public ResponseEntity<?> getDepositHistory(@PathVariable Long userId) {
        try {
            // Get the wallet
            WalletDto wallet = walletService.getWalletByUserId(userId);
            if (wallet == null) {
                return new ResponseEntity<>(Map.of("error", "Wallet not found for user: " + userId), HttpStatus.NOT_FOUND);
            }
            
            // Get deposit transactions
            List<WalletTransactionDto> deposits = walletTransactionService.getWalletTransactionsByType(
                wallet.getId(), WalletTransaction.WalletTransactionType.DEPOSIT);
            
            return new ResponseEntity<>(Map.of(
                "userId", userId,
                "walletId", wallet.getId(),
                "deposits", deposits
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all withdrawal transactions across all wallets (admin endpoint)
     * @return List of withdrawal transactions
     */
    @GetMapping("/transactions/withdrawals")
    public ResponseEntity<?> getAllWithdrawals() {
        try {
            List<WalletTransactionDto> withdrawals = walletTransactionService.getAllTransactionsByType(
                WalletTransaction.WalletTransactionType.WITHDRAWAL);
            
            return new ResponseEntity<>(Map.of(
                "withdrawals", withdrawals
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all deposit transactions across all wallets (admin endpoint)
     * @return List of deposit transactions
     */
    @GetMapping("/transactions/deposits")
    public ResponseEntity<?> getAllDeposits() {
        try {
            List<WalletTransactionDto> deposits = walletTransactionService.getAllTransactionsByType(
                WalletTransaction.WalletTransactionType.DEPOSIT);
            
            return new ResponseEntity<>(Map.of(
                "deposits", deposits
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
