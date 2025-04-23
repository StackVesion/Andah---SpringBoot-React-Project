package com.andah.paymentservice.controller;

import com.andah.paymentservice.dto.CryptoTransactionDto;
import com.andah.paymentservice.dto.PaymentTransactionDto;
import com.andah.paymentservice.model.PaymentTransaction;
import com.andah.paymentservice.service.CryptoTransactionService;
import com.andah.paymentservice.service.NowPaymentsService;
import com.andah.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/crypto-transactions")
public class CryptoTransactionController {

    private final CryptoTransactionService cryptoTransactionService;
    private final PaymentService paymentService;
    private final NowPaymentsService nowPaymentsService;
    private final RestTemplate restTemplate;
    
    @Value("${nowpayments.api.key:0CQX75G-JZ2M9QJ-KSVJ3QQ-Y8KDRHD}")
    private String apiKey;
    
    @Value("${nowpayments.api.url:https://api.nowpayments.io/v1}")
    private String apiUrl;

    @Autowired
    public CryptoTransactionController(CryptoTransactionService cryptoTransactionService, 
                                       PaymentService paymentService,
                                       NowPaymentsService nowPaymentsService) {
        this.cryptoTransactionService = cryptoTransactionService;
        this.paymentService = paymentService;
        this.nowPaymentsService = nowPaymentsService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a cryptocurrency transaction including NowPayments API integration
     * This endpoint handles both creating the NowPayments transaction and saving the transaction record
     * 
     * @param requestData Contains all required payment information
     * @return Created crypto transaction with payment details
     */
    @PostMapping
    public ResponseEntity<?> createCryptoTransaction(@RequestBody Map<String, Object> requestData) {
        try {
            // Validate required fields
            if (!requestData.containsKey("userId") || requestData.get("userId") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User ID is required"
                ));
            }
            
            // Check for either priceAmount or amount
            Double priceAmount = null;
            if (requestData.containsKey("priceAmount") && requestData.get("priceAmount") != null) {
                priceAmount = Double.parseDouble(requestData.get("priceAmount").toString());
            } else if (requestData.containsKey("amount") && requestData.get("amount") != null) {
                priceAmount = Double.parseDouble(requestData.get("amount").toString());
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Either priceAmount or amount is required"
                ));
            }
            
            // Extract and parse parameters
            Long userId = Long.parseLong(requestData.get("userId").toString());
            
            // Get priceCurrency with validation
            String priceCurrency = null;
            if (requestData.containsKey("priceCurrency") && requestData.get("priceCurrency") != null) {
                priceCurrency = requestData.get("priceCurrency").toString().trim();
                if (priceCurrency.isEmpty()) {
                    priceCurrency = "usd"; // Default to USD if empty string
                }
            } else {
                priceCurrency = "usd"; // Default to USD if missing
            }
            
            // Get payCurrency with validation
            String payCurrency = null;
            if (requestData.containsKey("payCurrency") && requestData.get("payCurrency") != null) {
                payCurrency = requestData.get("payCurrency").toString().trim();
            }
            
            if (payCurrency == null || payCurrency.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "payCurrency is required for cryptocurrency payments"
                ));
            }
            
            String orderId = requestData.containsKey("orderId") && requestData.get("orderId") != null ? 
                    requestData.get("orderId").toString() : "ANDAH-" + System.currentTimeMillis();
                    
            String orderDescription = requestData.containsKey("orderDescription") && requestData.get("orderDescription") != null ? 
                    requestData.get("orderDescription").toString() : "Payment for Andah services";
            
            // Validate amount
            if (priceAmount <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Price amount must be greater than zero"
                ));
            }
            
            // Validate crypto currency
            if (!isSupportedCryptoCurrency(payCurrency)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Unsupported cryptocurrency: " + payCurrency
                ));
            }
            
            // 1. Call NowPayments API
            try {
                // Prepare headers
                HttpHeaders headers = new HttpHeaders();
                headers.set("x-api-key", apiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                
                // Prepare request body for NowPayments API with all possible fields
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("price_amount", priceAmount);
                requestBody.put("price_currency", priceCurrency);
                requestBody.put("pay_currency", payCurrency);
                requestBody.put("order_id", orderId);
                requestBody.put("order_description", orderDescription);
                
                // Add callback URL
                String callbackUrl = "https://andah.com/api/crypto-transactions/ipn-callback";
                requestBody.put("ipn_callback_url", callbackUrl);
                
                // Log request for debugging
                System.out.println("=== NOWPAYMENTS API REQUEST ===");
                System.out.println("Headers: " + headers.toString());
                System.out.println("Request Body: " + requestBody.toString());
                System.out.println("API URL: " + apiUrl + "/payment");
                System.out.println("==============================");
                
                // Create HTTP entity
                HttpEntity<Map<String, Object>> httpRequestEntity = new HttpEntity<>(requestBody, headers);
                
                // Make the API call to NowPayments Payment API
                ResponseEntity<Map> nowPaymentsResponse = restTemplate.exchange(
                        apiUrl + "/payment", 
                        HttpMethod.POST, 
                        httpRequestEntity, 
                        Map.class);
                
                // 2. Process NowPayments response
                Map<String, Object> nowPaymentsData = nowPaymentsResponse.getBody();
                if (nowPaymentsData == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("success", false, "message", "No response from NowPayments API"));
                }
                
                // Log response for debugging
                System.out.println("=== NOWPAYMENTS API RESPONSE ===");
                System.out.println("Status Code: " + nowPaymentsResponse.getStatusCode());
                System.out.println("Response Body: " + nowPaymentsData.toString());
                System.out.println("===============================");
                
                // Check if response contains payment_id
                if (!nowPaymentsData.containsKey("payment_id")) {
                    String errorMessage = "Error from NowPayments API: ";
                    if (nowPaymentsData.containsKey("message")) {
                        errorMessage += nowPaymentsData.get("message");
                    } else {
                        errorMessage += "Missing payment_id in response";
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", errorMessage));
                }
                
                // Check if we have a successful status code from NowPayments (201 Created)
                if (nowPaymentsResponse.getStatusCode() != HttpStatus.CREATED) {
                    return ResponseEntity.status(nowPaymentsResponse.getStatusCode())
                            .body(Map.of(
                                "success", false, 
                                "message", "NowPayments API returned status code: " + nowPaymentsResponse.getStatusCode(),
                                "nowPaymentsResponse", nowPaymentsData
                            ));
                }
                
                // 3. Create and save the CryptoTransaction
                CryptoTransactionDto cryptoTransaction = new CryptoTransactionDto();
                cryptoTransaction.setUserId(userId);
                cryptoTransaction.setAmount(priceAmount);
                cryptoTransaction.setDate(LocalDateTime.now());
                
                // Set data from NowPayments response
                // Map all fields from the NowPayments API response to our model
                if (nowPaymentsData.containsKey("payment_id") && nowPaymentsData.get("payment_id") != null) {
                    cryptoTransaction.setNowPaymentId(nowPaymentsData.get("payment_id").toString());
                }
                
                if (nowPaymentsData.containsKey("payment_status") && nowPaymentsData.get("payment_status") != null) {
                    cryptoTransaction.setPaymentStatus(nowPaymentsData.get("payment_status").toString());
                }
                
                if (nowPaymentsData.containsKey("pay_address") && nowPaymentsData.get("pay_address") != null) {
                    cryptoTransaction.setPayAddress(nowPaymentsData.get("pay_address").toString());
                }
                
                // Set price amount and currency
                if (nowPaymentsData.containsKey("price_amount") && nowPaymentsData.get("price_amount") != null) {
                    try {
                        cryptoTransaction.setPriceAmount(Double.parseDouble(nowPaymentsData.get("price_amount").toString()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing price_amount: " + e.getMessage());
                    }
                }
                
                if (nowPaymentsData.containsKey("price_currency") && nowPaymentsData.get("price_currency") != null) {
                    cryptoTransaction.setPriceCurrency(nowPaymentsData.get("price_currency").toString());
                }
                
                // Set pay amount and currency
                if (nowPaymentsData.containsKey("pay_amount") && nowPaymentsData.get("pay_amount") != null) {
                    try {
                        cryptoTransaction.setPayAmount(Double.parseDouble(nowPaymentsData.get("pay_amount").toString()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing pay_amount: " + e.getMessage());
                    }
                }
                
                if (nowPaymentsData.containsKey("pay_currency") && nowPaymentsData.get("pay_currency") != null) {
                    cryptoTransaction.setPayCurrency(nowPaymentsData.get("pay_currency").toString());
                }
                
                // Set order information
                if (nowPaymentsData.containsKey("order_id") && nowPaymentsData.get("order_id") != null) {
                    cryptoTransaction.setOrderId(nowPaymentsData.get("order_id").toString());
                }
                
                if (nowPaymentsData.containsKey("order_description") && nowPaymentsData.get("order_description") != null) {
                    cryptoTransaction.setOrderDescription(nowPaymentsData.get("order_description").toString());
                }
                
                // Set additional fields from NowPayments response
                if (nowPaymentsData.containsKey("ipn_callback_url") && nowPaymentsData.get("ipn_callback_url") != null) {
                    cryptoTransaction.setIpnCallBack(nowPaymentsData.get("ipn_callback_url").toString());
                }
                
                if (nowPaymentsData.containsKey("created_at") && nowPaymentsData.get("created_at") != null) {
                    cryptoTransaction.setCreatedAt(nowPaymentsData.get("created_at").toString());
                }
                
                if (nowPaymentsData.containsKey("updated_at") && nowPaymentsData.get("updated_at") != null) {
                    cryptoTransaction.setUpdatedAt(nowPaymentsData.get("updated_at").toString());
                }
                
                if (nowPaymentsData.containsKey("purchase_id") && nowPaymentsData.get("purchase_id") != null) {
                    cryptoTransaction.setPurchaseId(nowPaymentsData.get("purchase_id").toString());
                }
                
                if (nowPaymentsData.containsKey("amount_received") && nowPaymentsData.get("amount_received") != null) {
                    try {
                        cryptoTransaction.setAmountReceived(Double.parseDouble(nowPaymentsData.get("amount_received").toString()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing amount_received: " + e.getMessage());
                    }
                }
                
                if (nowPaymentsData.containsKey("payin_extra_id") && nowPaymentsData.get("payin_extra_id") != null) {
                    cryptoTransaction.setPayinExtraId(nowPaymentsData.get("payin_extra_id").toString());
                }
                
                if (nowPaymentsData.containsKey("smart_contract") && nowPaymentsData.get("smart_contract") != null) {
                    cryptoTransaction.setSmartContract(nowPaymentsData.get("smart_contract").toString());
                }
                
                if (nowPaymentsData.containsKey("network") && nowPaymentsData.get("network") != null) {
                    cryptoTransaction.setNetwork(nowPaymentsData.get("network").toString());
                }
                
                if (nowPaymentsData.containsKey("network_precision") && nowPaymentsData.get("network_precision") != null) {
                    try {
                        cryptoTransaction.setNetworkPrecision(Integer.parseInt(nowPaymentsData.get("network_precision").toString()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing network_precision: " + e.getMessage());
                    }
                }
                
                if (nowPaymentsData.containsKey("time_limit") && nowPaymentsData.get("time_limit") != null) {
                    cryptoTransaction.setTimeLimit(nowPaymentsData.get("time_limit").toString());
                }
                
                if (nowPaymentsData.containsKey("burning_percent") && nowPaymentsData.get("burning_percent") != null) {
                    try {
                        cryptoTransaction.setBurningPercent(Double.parseDouble(nowPaymentsData.get("burning_percent").toString()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing burning_percent: " + e.getMessage());
                    }
                }
                
                if (nowPaymentsData.containsKey("expiration_estimate_date") && nowPaymentsData.get("expiration_estimate_date") != null) {
                    cryptoTransaction.setExpirationEstimateDate(nowPaymentsData.get("expiration_estimate_date").toString());
                }
                
                // Save the crypto transaction
                CryptoTransactionDto savedCryptoTransaction = cryptoTransactionService.createCryptoTransaction(cryptoTransaction);
                
                // 4. Create and save the PaymentTransaction
                PaymentTransactionDto paymentTransaction = new PaymentTransactionDto();
                paymentTransaction.setUserId(userId);
                paymentTransaction.setAmount(priceAmount);
                paymentTransaction.setPaymentMethod(PaymentTransaction.PaymentMethod.CRYPTO);
                paymentTransaction.setStatus(PaymentTransaction.PaymentStatus.PENDING); // Crypto payments start as pending
                paymentTransaction.setCryptoTransactionId(savedCryptoTransaction.getId());
                
                // Set the currency field required by PaymentController validation
                if (nowPaymentsData.containsKey("pay_currency") && nowPaymentsData.get("pay_currency") != null) {
                    paymentTransaction.setCurrency(nowPaymentsData.get("pay_currency").toString());
                } else if (payCurrency != null && !payCurrency.isEmpty()) {
                    paymentTransaction.setCurrency(payCurrency);
                } else {
                    paymentTransaction.setCurrency("btc"); // Default to btc if not specified
                }
                
                // Add null check for payment_id field
                if (nowPaymentsData.containsKey("payment_id") && nowPaymentsData.get("payment_id") != null) {
                    paymentTransaction.setTransactionReference("NOWPAYMENTS-" + nowPaymentsData.get("payment_id"));
                } else {
                    paymentTransaction.setTransactionReference("NOWPAYMENTS-" + System.currentTimeMillis());
                }
                
                // Save the payment transaction
                PaymentTransactionDto savedPayment = paymentService.createPayment(paymentTransaction);
                
                // Update the cryptoTransaction with the paymentId reference
                savedCryptoTransaction.setPaymentId(savedPayment.getId());
                cryptoTransactionService.updateCryptoTransaction(savedCryptoTransaction);
                
                // 5. Prepare and return the success response
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Crypto payment processed successfully");
                response.put("cryptoTransaction", savedCryptoTransaction);
                response.put("paymentTransaction", savedPayment);
                response.put("nowPaymentsData", nowPaymentsData);
                
                // Add payment instructions for the user
                Map<String, Object> paymentInstructions = new HashMap<>();
                
                // Add null checks for all NowPayments response fields
                if (nowPaymentsData.containsKey("payment_id") && nowPaymentsData.get("payment_id") != null) {
                    paymentInstructions.put("paymentId", nowPaymentsData.get("payment_id"));
                }
                
                if (nowPaymentsData.containsKey("pay_address") && nowPaymentsData.get("pay_address") != null) {
                    paymentInstructions.put("payAddress", nowPaymentsData.get("pay_address"));
                }
                
                if (nowPaymentsData.containsKey("pay_amount") && nowPaymentsData.get("pay_amount") != null) {
                    paymentInstructions.put("payAmount", nowPaymentsData.get("pay_amount"));
                }
                
                if (nowPaymentsData.containsKey("pay_currency") && nowPaymentsData.get("pay_currency") != null) {
                    paymentInstructions.put("payCurrency", nowPaymentsData.get("pay_currency"));
                }
                
                if (nowPaymentsData.containsKey("expiration_estimate_date") && nowPaymentsData.get("expiration_estimate_date") != null) {
                    paymentInstructions.put("expirationDate", nowPaymentsData.get("expiration_estimate_date"));
                }
                
                response.put("paymentInstructions", paymentInstructions);
                
                return new ResponseEntity<>(response, HttpStatus.CREATED);
                
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Error processing crypto payment: " + e.getMessage()));
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error processing crypto payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<CryptoTransactionDto>> getAllCryptoTransactions() {
        List<CryptoTransactionDto> transactions = cryptoTransactionService.getAllCryptoTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CryptoTransactionDto> getCryptoTransactionById(@PathVariable String id) {
        return cryptoTransactionService.getCryptoTransactionById(id)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CryptoTransactionDto>> getCryptoTransactionsByUserId(@PathVariable Long userId) {
        List<CryptoTransactionDto> transactions = cryptoTransactionService.getCryptoTransactionsByUserId(userId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CryptoTransactionDto>> getCryptoTransactionsByStatus(@PathVariable String status) {
        List<CryptoTransactionDto> transactions = cryptoTransactionService.getCryptoTransactionsByStatus(status);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/status/{nowPaymentId}")
    public ResponseEntity<?> getCryptoPaymentStatus(@PathVariable String nowPaymentId) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            // Create HTTP entity
            HttpEntity<?> httpRequestEntity = new HttpEntity<>(headers);
            
            // Make the API call to NowPayments Payment Status API
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/payment/" + nowPaymentId, 
                    HttpMethod.GET, 
                    httpRequestEntity, 
                    Map.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error checking payment status: " + e.getMessage()));
        }
    }

    @PostMapping("/ipn-callback")
    public ResponseEntity<?> handleIpnCallback(@RequestBody Map<String, Object> ipnNotification) {
        try {
            // Extract payment ID from the notification
            if (!ipnNotification.containsKey("payment_id")) {
                return ResponseEntity.badRequest().body("Missing payment_id in IPN notification");
            }
            
            String nowPaymentId = ipnNotification.get("payment_id").toString();
            String status = ipnNotification.containsKey("payment_status") ? 
                    ipnNotification.get("payment_status").toString() : "unknown";
            
            // Find the crypto transaction by nowPaymentId
            Optional<CryptoTransactionDto> cryptoTransactionOptional = cryptoTransactionService.getCryptoTransactionByNowPaymentId(nowPaymentId);
            
            if (!cryptoTransactionOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No crypto transaction found for payment_id: " + nowPaymentId);
            }
            
            CryptoTransactionDto cryptoTransaction = cryptoTransactionOptional.get();
            
            // Update the transaction status
            String newStatus = status.toUpperCase();
            String oldStatus = cryptoTransaction.getPaymentStatus();
            
            // Only update if status has changed
            if (!newStatus.equals(oldStatus)) {
                cryptoTransaction.setPaymentStatus(newStatus);
                
                // Update additional information if available
                if (ipnNotification.containsKey("pay_amount") && ipnNotification.get("pay_amount") != null) {
                    try {
                        Double payAmount = Double.parseDouble(ipnNotification.get("pay_amount").toString());
                        cryptoTransaction.setPayAmount(payAmount);
                    } catch (NumberFormatException ignored) {}
                }
                
                if (ipnNotification.containsKey("actually_paid") && ipnNotification.get("actually_paid") != null) {
                    try {
                        Double actuallyPaid = Double.parseDouble(ipnNotification.get("actually_paid").toString());
                        cryptoTransaction.setAmountReceived(actuallyPaid);
                    } catch (NumberFormatException ignored) {}
                }
                
                // Save updated crypto transaction
                CryptoTransactionDto updatedTransaction = cryptoTransactionService.createCryptoTransaction(cryptoTransaction);
                
                // Find and update corresponding payment transaction
                List<PaymentTransactionDto> payments = paymentService.getPaymentsByCryptoTransactionId(cryptoTransaction.getId());
                
                for (PaymentTransactionDto payment : payments) {
                    // Map crypto payment status to our payment status
                    PaymentTransaction.PaymentStatus paymentStatus;
                    if ("CONFIRMED".equalsIgnoreCase(newStatus) || "FINISHED".equalsIgnoreCase(newStatus)) {
                        paymentStatus = PaymentTransaction.PaymentStatus.COMPLETED;
                    } else if ("WAITING".equalsIgnoreCase(newStatus) || "PENDING".equalsIgnoreCase(newStatus)) {
                        paymentStatus = PaymentTransaction.PaymentStatus.PENDING;
                    } else if ("EXPIRED".equalsIgnoreCase(newStatus) || "FAILED".equalsIgnoreCase(newStatus)) {
                        paymentStatus = PaymentTransaction.PaymentStatus.FAILED;
                    } else if ("REFUNDED".equalsIgnoreCase(newStatus)) {
                        paymentStatus = PaymentTransaction.PaymentStatus.REFUNDED;
                    } else {
                        paymentStatus = PaymentTransaction.PaymentStatus.PENDING; // Default to pending for unknown statuses
                    }
                    
                    // Update payment status
                    payment.setStatus(paymentStatus);
                    paymentService.updatePayment(payment);
                }
            }
            
            return ResponseEntity.ok("IPN Processed Successfully");
        } catch (Exception e) {
            System.err.println("Error processing IPN: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing IPN: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCryptoTransaction(@PathVariable String id) {
        cryptoTransactionService.deleteCryptoTransaction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Check if the provided cryptocurrency is supported
     *
     * @param currency Cryptocurrency code
     * @return true if supported, false otherwise
     */
    private boolean isSupportedCryptoCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        // Add supported currencies (lowercase)
        Set<String> supportedCurrencies = new HashSet<>(Arrays.asList(
            "btc", "eth", "ltc", "bch", "dash", "doge", "trx", 
            "usdt", "usdttrc20", "usdterc20", "bnb", "usdc", "xlm"
        ));
        return supportedCurrencies.contains(currency.toLowerCase());
    }
}
