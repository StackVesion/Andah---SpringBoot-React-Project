package com.andah.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "crypto_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTransaction {
    @Id
    private String id;
    private Long userId;
    private LocalDateTime date;
    private String nowPaymentId;         // payment_id from API
    private String paymentStatus;        // payment_status from API
    private String payAddress;           // pay_address from API
    private Double priceAmount;          // price_amount from API
    private String priceCurrency;        // price_currency from API (e.g., "usd")
    private Double payAmount;            // pay_amount from API
    private String payCurrency;          // pay_currency from API (btc, usdttrc20, ltc, eth)
    private String orderId;              // order_id from API
    private String orderDescription;     // order_description from API
    private String ipnCallBack;          // ipn_callback_url from API
    private String createdAt;            // created_at from API
    private String updatedAt;            // updated_at from API
    private String purchaseId;           // purchase_id from API
    private Double amountReceived;       // amount_received from API (can be null)
    private String payinExtraId;         // payin_extra_id from API (can be null)
    private String smartContract;        // smart_contract from API
    private String network;              // network from API
    private Integer networkPrecision;    // network_precision from API
    private String timeLimit;            // time_limit from API (can be null)
    private Double burningPercent;       // burning_percent from API (can be null)
    private String expirationEstimateDate; // expiration_estimate_date from API
}
