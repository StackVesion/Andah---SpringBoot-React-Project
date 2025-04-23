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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class NowPaymentsService {

    private final RestTemplate restTemplate;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    
    @Value("${nowpayments.api.key:0CQX75G-JZ2M9QJ-KSVJ3QQ-Y8KDRHD}")
    private String apiKey;
    
    @Value("${nowpayments.api.url:https://api.nowpayments.io/v1}")
    private String apiUrl;
    
    @Autowired
    public NowPaymentsService(CryptoTransactionRepository cryptoTransactionRepository) {
        this.restTemplate = new RestTemplate();
        this.cryptoTransactionRepository = cryptoTransactionRepository;
    }
    
    /**
     * Create a crypto payment using NowPayments API
     * 
     * @param userId User ID making the payment
     * @param priceAmount Amount to be paid in fiat currency
     * @param priceCurrency Fiat currency code (e.g., USD)
     * @param payCurrency Crypto currency code (e.g., BTC, ETH)
     * @param orderId Unique order identifier
     * @param orderDescription Description of the order
     * @param callbackUrl URL for IPN callbacks
     * @return CryptoTransactionDto with payment details
     */
    public CryptoTransactionDto createCryptoPayment(
            Long userId, 
            Double priceAmount, 
            String priceCurrency, 
            String payCurrency, 
            String orderId, 
            String orderDescription,
            String callbackUrl) {
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // Prepare request body
        Map<String, Object> body = new HashMap<>();
        body.put("price_amount", priceAmount);
        body.put("price_currency", priceCurrency);
        body.put("pay_currency", payCurrency);
        body.put("ipn_callback_url", callbackUrl);
        body.put("order_id", orderId);
        body.put("order_description", orderDescription);
        
        // Create HTTP entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        // Make the API call
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payment", 
                HttpMethod.POST, 
                requestEntity, 
                Map.class);
        
        // Process response and create CryptoTransaction
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            CryptoTransaction transaction = new CryptoTransaction();
            transaction.setUserId(userId);
            transaction.setDate(LocalDateTime.now());
            
            // Map API response fields to CryptoTransaction object
            transaction.setNowPaymentId(responseBody.get("payment_id").toString());
            transaction.setPaymentStatus((String) responseBody.get("payment_status"));
            transaction.setPayAddress((String) responseBody.get("pay_address"));
            transaction.setPriceAmount(Double.valueOf(responseBody.get("price_amount").toString()));
            transaction.setPriceCurrency((String) responseBody.get("price_currency"));
            transaction.setPayAmount(Double.valueOf(responseBody.get("pay_amount").toString()));
            transaction.setPayCurrency((String) responseBody.get("pay_currency"));
            transaction.setOrderId((String) responseBody.get("order_id"));
            transaction.setOrderDescription((String) responseBody.get("order_description"));
            transaction.setIpnCallBack((String) responseBody.get("ipn_callback_url"));
            transaction.setCreatedAt((String) responseBody.get("created_at"));
            transaction.setUpdatedAt((String) responseBody.get("updated_at"));
            transaction.setPurchaseId((String) responseBody.get("purchase_id"));
            
            // Handle nullable fields
            if (responseBody.get("amount_received") != null) {
                transaction.setAmountReceived(Double.valueOf(responseBody.get("amount_received").toString()));
            }
            transaction.setPayinExtraId((String) responseBody.get("payin_extra_id"));
            transaction.setSmartContract((String) responseBody.get("smart_contract"));
            transaction.setNetwork((String) responseBody.get("network"));
            if (responseBody.get("network_precision") != null) {
                transaction.setNetworkPrecision(Integer.valueOf(responseBody.get("network_precision").toString()));
            }
            transaction.setTimeLimit((String) responseBody.get("time_limit"));
            if (responseBody.get("burning_percent") != null) {
                transaction.setBurningPercent(Double.valueOf(responseBody.get("burning_percent").toString()));
            }
            transaction.setExpirationEstimateDate((String) responseBody.get("expiration_estimate_date"));
            
            // Save the transaction in the database
            CryptoTransaction savedTransaction = cryptoTransactionRepository.save(transaction);
            
            // Convert to DTO and return
            CryptoTransactionDto dto = new CryptoTransactionDto();
            dto.setId(savedTransaction.getId());
            dto.setUserId(savedTransaction.getUserId());
            dto.setDate(savedTransaction.getDate());
            dto.setNowPaymentId(savedTransaction.getNowPaymentId());
            dto.setPaymentStatus(savedTransaction.getPaymentStatus());
            dto.setPayAddress(savedTransaction.getPayAddress());
            dto.setPriceAmount(savedTransaction.getPriceAmount());
            dto.setPriceCurrency(savedTransaction.getPriceCurrency());
            dto.setPayAmount(savedTransaction.getPayAmount());
            dto.setPayCurrency(savedTransaction.getPayCurrency());
            dto.setOrderId(savedTransaction.getOrderId());
            dto.setOrderDescription(savedTransaction.getOrderDescription());
            dto.setIpnCallBack(savedTransaction.getIpnCallBack());
            dto.setCreatedAt(savedTransaction.getCreatedAt());
            dto.setUpdatedAt(savedTransaction.getUpdatedAt());
            dto.setPurchaseId(savedTransaction.getPurchaseId());
            dto.setAmountReceived(savedTransaction.getAmountReceived());
            dto.setPayinExtraId(savedTransaction.getPayinExtraId());
            dto.setSmartContract(savedTransaction.getSmartContract());
            dto.setNetwork(savedTransaction.getNetwork());
            dto.setNetworkPrecision(savedTransaction.getNetworkPrecision());
            dto.setTimeLimit(savedTransaction.getTimeLimit());
            dto.setBurningPercent(savedTransaction.getBurningPercent());
            dto.setExpirationEstimateDate(savedTransaction.getExpirationEstimateDate());
            
            return dto;
        }
        
        throw new RuntimeException("Failed to create crypto payment");
    }
    
    /**
     * Get payment status from NowPayments API
     * 
     * @param paymentId NowPayments payment ID
     * @return Updated payment status
     */
    public String getPaymentStatus(String paymentId) {
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        // Create HTTP entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // Make the API call
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payment/" + paymentId, 
                HttpMethod.GET, 
                requestEntity, 
                Map.class);
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("payment_status")) {
            return (String) responseBody.get("payment_status");
        }
        
        return "UNKNOWN";
    }
}
