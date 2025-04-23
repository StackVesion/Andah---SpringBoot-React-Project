package com.andah.paymentservice.service;

import com.andah.paymentservice.dto.CryptoTransactionDto;
import com.andah.paymentservice.model.CryptoTransaction;
import com.andah.paymentservice.repository.CryptoTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CryptoTransactionService {

    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final RestTemplate restTemplate;
    
    @Value("${nowpayments.api.key:0CQX75G-JZ2M9QJ-KSVJ3QQ-Y8KDRHD}")
    private String apiKey;
    
    @Value("${nowpayments.api.url:https://api.nowpayments.io/v1}")
    private String apiUrl;

    @Autowired
    public CryptoTransactionService(CryptoTransactionRepository cryptoTransactionRepository) {
        this.cryptoTransactionRepository = cryptoTransactionRepository;
        this.restTemplate = new RestTemplate();
    }

    public CryptoTransactionDto createCryptoTransaction(CryptoTransactionDto cryptoTransactionDto) {
        CryptoTransaction cryptoTransaction = convertToEntity(cryptoTransactionDto);
        cryptoTransaction.setDate(LocalDateTime.now());
        cryptoTransaction = cryptoTransactionRepository.save(cryptoTransaction);
        return convertToDto(cryptoTransaction);
    }

    public List<CryptoTransactionDto> getAllCryptoTransactions() {
        return cryptoTransactionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<CryptoTransactionDto> getCryptoTransactionById(String id) {
        return cryptoTransactionRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<CryptoTransactionDto> getCryptoTransactionsByUserId(Long userId) {
        return cryptoTransactionRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a crypto payment via NowPayments API
     *
     * @param userId User ID making the payment
     * @param priceAmount Amount to be paid in the price currency
     * @param priceCurrency Currency for the price (USD, EUR, etc)
     * @param payCurrency Cryptocurrency to pay with (BTC, ETH, etc)
     * @param orderId Your internal order ID
     * @param orderDescription Description of the payment
     * @param callbackUrl URL for IPN notifications
     * @return Created crypto transaction
     */
    public CryptoTransactionDto createCryptoPayment(
            Long userId,
            Double priceAmount,
            String priceCurrency,
            String payCurrency,
            String orderId,
            String orderDescription,
            String callbackUrl) {
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (priceAmount == null || priceAmount <= 0) {
            throw new IllegalArgumentException("Price amount must be greater than zero");
        }
        
        if (priceCurrency == null || priceCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Price currency is required");
        }
        
        if (payCurrency == null || payCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Pay currency is required");
        }
        
        try {
            // Log the API request
            System.out.println("Sending request to NowPayments API with key: " + apiKey);
            System.out.println("API URL: " + apiUrl + "/payment");
            
            // Prepare the request body - use exact field names as required by NowPayments API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("price_amount", priceAmount);
            requestBody.put("price_currency", priceCurrency);
            requestBody.put("pay_currency", payCurrency);
            requestBody.put("ipn_callback_url", callbackUrl);
            requestBody.put("order_id", orderId);
            requestBody.put("order_description", orderDescription);
            
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Make the API call
            ResponseEntity<Map> response;
            try {
                response = restTemplate.exchange(
                        apiUrl + "/payment",
                        HttpMethod.POST,
                        requestEntity,
                        Map.class);
            } catch (RestClientException e) {
                // Handle REST client errors
                System.err.println("REST Client Error: " + e.getMessage());
                throw new RuntimeException("NowPayments API error: " + e.getMessage());
            }
            
            // Process API response
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody == null) {
                throw new RuntimeException("Empty response received from NowPayments API");
            }
            
            // Check for error response
            if (responseBody.containsKey("statusCode") && !"200".equals(responseBody.get("statusCode").toString())) {
                throw new RuntimeException("NowPayments API error: " + 
                        responseBody.getOrDefault("message", "Unknown error"));
            }

            // Create DTO from response
            CryptoTransactionDto transactionDto = new CryptoTransactionDto();
            transactionDto.setUserId(userId);
            transactionDto.setDate(LocalDateTime.now());
            
            // Set payment ID from response
            if (responseBody.containsKey("payment_id")) {
                transactionDto.setNowPaymentId(responseBody.get("payment_id").toString());
            } else {
                throw new RuntimeException("Missing payment_id in NowPayments response");
            }
            
            // Set payment status
            if (responseBody.containsKey("payment_status")) {
                transactionDto.setPaymentStatus(responseBody.get("payment_status").toString());
            } else {
                transactionDto.setPaymentStatus("WAITING"); // Default status if not provided
            }
            
            // Set pay address
            if (responseBody.containsKey("pay_address")) {
                transactionDto.setPayAddress(responseBody.get("pay_address").toString());
            }
            
            // Set pay amount (safely handle different numeric types)
            if (responseBody.containsKey("pay_amount")) {
                Object payAmount = responseBody.get("pay_amount");
                if (payAmount instanceof Double) {
                    transactionDto.setPayAmount((Double) payAmount);
                } else if (payAmount instanceof Integer) {
                    transactionDto.setPayAmount(((Integer) payAmount).doubleValue());
                } else if (payAmount != null) {
                    try {
                        transactionDto.setPayAmount(Double.parseDouble(payAmount.toString()));
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse pay_amount: " + payAmount);
                        // Don't fail the whole transaction, just log the error
                    }
                }
            }
            
            // Set additional fields
            if (responseBody.containsKey("pay_currency")) {
                transactionDto.setPayCurrency(responseBody.get("pay_currency").toString());
            } else {
                transactionDto.setPayCurrency(payCurrency);
            }
            
            transactionDto.setPriceAmount(priceAmount);
            transactionDto.setPriceCurrency(priceCurrency);
            transactionDto.setOrderId(orderId);
            transactionDto.setOrderDescription(orderDescription);
            transactionDto.setIpnCallBack(callbackUrl);
            
            // Save the transaction to our database
            return createCryptoTransaction(transactionDto);
        } catch (Exception e) {
            System.err.println("Error creating crypto payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create crypto payment: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get payment status from NowPayments API
     *
     * @param nowPaymentId NowPayments payment ID
     * @return Payment status
     */
    public String getPaymentStatus(String nowPaymentId) {
        // Set up headers with API key
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        // Make API call
        String url = apiUrl + "/payment/" + nowPaymentId;
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );
        
        // Process response
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("No response from NowPayments API");
        }
        
        // Return the payment status
        return responseBody.get("payment_status").toString();
    }
    
    public List<CryptoTransactionDto> getCryptoTransactionsByStatus(String status) {
        return cryptoTransactionRepository.findByPaymentStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find crypto transactions by NowPayments payment ID
     *
     * @param nowPaymentId The NowPayments payment ID
     * @return List of crypto transactions with matching NowPayments ID
     */
    public List<CryptoTransactionDto> getCryptoTransactionsByNowPaymentId(String nowPaymentId) {
        if (nowPaymentId == null || nowPaymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("NowPayments ID cannot be null or empty");
        }
        
        return cryptoTransactionRepository.findByNowPaymentId(nowPaymentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void deleteCryptoTransaction(String id) {
        cryptoTransactionRepository.deleteById(id);
    }

    public CryptoTransactionDto updateCryptoTransaction(CryptoTransactionDto cryptoTransactionDto) {
        if (cryptoTransactionDto.getId() == null) {
            throw new IllegalArgumentException("CryptoTransaction ID is required for update");
        }
        
        CryptoTransaction existingTransaction = cryptoTransactionRepository.findById(cryptoTransactionDto.getId())
                .orElseThrow(() -> new RuntimeException("CryptoTransaction not found with id: " + cryptoTransactionDto.getId()));
        
        // Update the fields
        existingTransaction.setPaymentStatus(cryptoTransactionDto.getPaymentStatus());
        existingTransaction.setPayAddress(cryptoTransactionDto.getPayAddress());
        existingTransaction.setPayAmount(cryptoTransactionDto.getPayAmount());
        existingTransaction.setPriceAmount(cryptoTransactionDto.getPriceAmount());
        existingTransaction.setPriceCurrency(cryptoTransactionDto.getPriceCurrency());
        existingTransaction.setPayCurrency(cryptoTransactionDto.getPayCurrency());
        existingTransaction.setOrderId(cryptoTransactionDto.getOrderId());
        existingTransaction.setOrderDescription(cryptoTransactionDto.getOrderDescription());
        existingTransaction.setIpnCallBack(cryptoTransactionDto.getIpnCallBack());
        existingTransaction.setCreatedAt(cryptoTransactionDto.getCreatedAt());
        existingTransaction.setUpdatedAt(cryptoTransactionDto.getUpdatedAt());
        existingTransaction.setPurchaseId(cryptoTransactionDto.getPurchaseId());
        existingTransaction.setAmountReceived(cryptoTransactionDto.getAmountReceived());
        existingTransaction.setPayinExtraId(cryptoTransactionDto.getPayinExtraId());
        existingTransaction.setSmartContract(cryptoTransactionDto.getSmartContract());
        existingTransaction.setNetwork(cryptoTransactionDto.getNetwork());
        existingTransaction.setNetworkPrecision(cryptoTransactionDto.getNetworkPrecision());
        existingTransaction.setTimeLimit(cryptoTransactionDto.getTimeLimit());
        existingTransaction.setBurningPercent(cryptoTransactionDto.getBurningPercent());
        existingTransaction.setExpirationEstimateDate(cryptoTransactionDto.getExpirationEstimateDate());
        
        // Save the updated entity
        CryptoTransaction updatedTransaction = cryptoTransactionRepository.save(existingTransaction);
        return convertToDto(updatedTransaction);
    }
    
    /**
     * Get a crypto transaction by NowPayments ID
     *
     * @param nowPaymentId NowPayments payment ID
     * @return CryptoTransactionDto if found
     */
    public Optional<CryptoTransactionDto> getCryptoTransactionByNowPaymentId(String nowPaymentId) {
        if (nowPaymentId == null || nowPaymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("NowPayments ID cannot be null or empty");
        }
        
        List<CryptoTransaction> transactions = cryptoTransactionRepository.findByNowPaymentId(nowPaymentId);
        if (transactions.isEmpty()) {
            return Optional.empty();
        }
        
        // Return the first matching transaction
        return Optional.of(convertToDto(transactions.get(0)));
    }

    private CryptoTransaction convertToEntity(CryptoTransactionDto dto) {
        CryptoTransaction entity = new CryptoTransaction();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setDate(dto.getDate());
        entity.setNowPaymentId(dto.getNowPaymentId());
        entity.setPaymentStatus(dto.getPaymentStatus());
        entity.setPayAddress(dto.getPayAddress());
        entity.setPriceAmount(dto.getPriceAmount());
        entity.setPriceCurrency(dto.getPriceCurrency());
        entity.setPayAmount(dto.getPayAmount());
        entity.setPayCurrency(dto.getPayCurrency());
        entity.setOrderId(dto.getOrderId());
        entity.setOrderDescription(dto.getOrderDescription());
        entity.setIpnCallBack(dto.getIpnCallBack());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setPurchaseId(dto.getPurchaseId());
        entity.setAmountReceived(dto.getAmountReceived());
        entity.setPayinExtraId(dto.getPayinExtraId());
        entity.setSmartContract(dto.getSmartContract());
        entity.setNetwork(dto.getNetwork());
        entity.setNetworkPrecision(dto.getNetworkPrecision());
        entity.setTimeLimit(dto.getTimeLimit());
        entity.setBurningPercent(dto.getBurningPercent());
        entity.setExpirationEstimateDate(dto.getExpirationEstimateDate());
        return entity;
    }

    private CryptoTransactionDto convertToDto(CryptoTransaction entity) {
        return CryptoTransactionDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .date(entity.getDate())
                .nowPaymentId(entity.getNowPaymentId())
                .paymentStatus(entity.getPaymentStatus())
                .payAddress(entity.getPayAddress())
                .priceAmount(entity.getPriceAmount())
                .priceCurrency(entity.getPriceCurrency())
                .payAmount(entity.getPayAmount())
                .payCurrency(entity.getPayCurrency())
                .orderId(entity.getOrderId())
                .orderDescription(entity.getOrderDescription())
                .ipnCallBack(entity.getIpnCallBack())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .purchaseId(entity.getPurchaseId())
                .amountReceived(entity.getAmountReceived())
                .payinExtraId(entity.getPayinExtraId())
                .smartContract(entity.getSmartContract())
                .network(entity.getNetwork())
                .networkPrecision(entity.getNetworkPrecision())
                .timeLimit(entity.getTimeLimit())
                .burningPercent(entity.getBurningPercent())
                .expirationEstimateDate(entity.getExpirationEstimateDate())
                .build();
    }
}
