# Payment Service RabbitMQ Integration

This guide shows how to integrate RabbitMQ messaging with your Payment Service to communicate with the Reclamation Service and User Service.

## 1. Add RabbitMQ Dependencies

Add these dependencies to your `pom.xml`:

```xml
<!-- RabbitMQ / AMQP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

## 2. Configuration Class

Create a `RabbitMQConfig.java` file:

```java
package com.andah.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String RECLAMATION_EXCHANGE = "reclamation.exchange";
    
    // Queue names
    public static final String PAYMENT_WALLET_UPDATE_QUEUE = "payment.wallet.update.queue";
    public static final String PAYMENT_REFUND_REQUEST_QUEUE = "payment.refund.request.queue";
    public static final String PAYMENT_REFUND_RESULT_QUEUE = "payment.refund.result.queue";
    
    // Routing keys
    public static final String PAYMENT_WALLET_UPDATE_KEY = "payment.wallet.update";
    public static final String PAYMENT_REFUND_REQUEST_KEY = "payment.refund.request";
    public static final String PAYMENT_REFUND_RESULT_KEY = "payment.refund.result";
    
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }
    
    @Bean
    public TopicExchange reclamationExchange() {
        return new TopicExchange(RECLAMATION_EXCHANGE);
    }
    
    @Bean
    public Queue walletUpdateQueue() {
        return new Queue(PAYMENT_WALLET_UPDATE_QUEUE, true);
    }
    
    @Bean
    public Queue refundRequestQueue() {
        return new Queue(PAYMENT_REFUND_REQUEST_QUEUE, true);
    }
    
    @Bean
    public Queue refundResultQueue() {
        return new Queue(PAYMENT_REFUND_RESULT_QUEUE, true);
    }
    
    @Bean
    public Binding walletUpdateBinding(Queue walletUpdateQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(walletUpdateQueue).to(paymentExchange).with(PAYMENT_WALLET_UPDATE_KEY);
    }
    
    @Bean
    public Binding refundRequestBinding(Queue refundRequestQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(refundRequestQueue).to(paymentExchange).with(PAYMENT_REFUND_REQUEST_KEY);
    }
    
    @Bean
    public Binding refundResultBinding(Queue refundResultQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(refundResultQueue).to(paymentExchange).with(PAYMENT_REFUND_RESULT_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
```

## 3. Message Models

Create the following model classes for messaging:

```java
// RefundRequestMessage.java
package com.andah.paymentservice.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestMessage {
    private String reclamationId;
    private String userId;
    private Double amount;
    private String reason;
    private String requestedBy;
    private boolean refundToWallet;
}

// RefundResultMessage.java
package com.andah.paymentservice.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResultMessage {
    private String reclamationId;
    private boolean success;
    private String reason;
    private String transactionId;
    private Double amount;
}

// WalletUpdateMessage.java
package com.andah.paymentservice.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletUpdateMessage {
    private String userId;
    private String walletId;
    private Double balance;
    private String transactionId;
    private String transactionType;
}
```

## 4. Service Implementation for Message Sending

Create a service to handle message publishing:

```java
// MessagePublisherService.java
package com.andah.paymentservice.service;

import com.andah.paymentservice.config.RabbitMQConfig;
import com.andah.paymentservice.model.message.RefundResultMessage;
import com.andah.paymentservice.model.message.WalletUpdateMessage;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisherService {

    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessagePublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publishWalletUpdate(WalletUpdateMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE, 
            RabbitMQConfig.PAYMENT_WALLET_UPDATE_KEY, 
            message
        );
    }
    
    public void publishRefundResult(RefundResultMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE, 
            RabbitMQConfig.PAYMENT_REFUND_RESULT_KEY, 
            message
        );
    }
}
```

## 5. Service Implementation for Message Listening

Create a service to handle incoming messages:

```java
// MessageListenerService.java
package com.andah.paymentservice.service;

import com.andah.paymentservice.config.RabbitMQConfig;
import com.andah.paymentservice.model.message.RefundRequestMessage;
import com.andah.paymentservice.model.message.RefundResultMessage;
import com.andah.paymentservice.service.wallet.WalletService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageListenerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageListenerService.class);
    
    private final WalletService walletService;
    private final MessagePublisherService publisherService;
    
    @Autowired
    public MessageListenerService(WalletService walletService, MessagePublisherService publisherService) {
        this.walletService = walletService;
        this.publisherService = publisherService;
    }
    
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_REFUND_REQUEST_QUEUE)
    public void processRefundRequest(RefundRequestMessage request) {
        logger.info("Received refund request for reclamation: {}", request.getReclamationId());
        
        try {
            // Process the refund using your existing wallet service
            boolean success = false;
            String transactionId = null;
            String failureReason = null;
            
            if (request.isRefundToWallet()) {
                // Use your wallet service to add funds to user's wallet
                try {
                    // This would be your actual wallet service implementation
                    transactionId = walletService.addFundsToWallet(
                        request.getUserId(), 
                        request.getAmount(), 
                        "Refund for reclamation: " + request.getReclamationId()
                    );
                    success = transactionId != null;
                } catch (Exception e) {
                    failureReason = e.getMessage();
                    logger.error("Error processing wallet refund: {}", e.getMessage(), e);
                }
            } else {
                // Handle other refund methods if needed
                failureReason = "Non-wallet refunds not implemented";
            }
            
            // Send result back
            RefundResultMessage result = new RefundResultMessage(
                request.getReclamationId(),
                success,
                success ? "Refund processed successfully" : "Refund failed: " + failureReason,
                transactionId,
                request.getAmount()
            );
            
            publisherService.publishRefundResult(result);
            
        } catch (Exception e) {
            logger.error("Error processing refund request: {}", e.getMessage(), e);
            // Send failure message
            RefundResultMessage failureResult = new RefundResultMessage(
                request.getReclamationId(),
                false,
                "Internal error: " + e.getMessage(),
                null,
                request.getAmount()
            );
            publisherService.publishRefundResult(failureResult);
        }
    }
}
```

## 6. Integration with Wallet Service

Update your existing `WalletService` to notify about wallet changes:

```java
// In your existing WalletService.java

// After updating wallet balance, publish a message
public void notifyWalletUpdate(String userId, String walletId, Double newBalance, String transactionId) {
    WalletUpdateMessage message = new WalletUpdateMessage(
        userId,
        walletId,
        newBalance,
        transactionId,
        "UPDATE" // or DEPOSIT, WITHDRAWAL, REFUND, etc.
    );
    messagePublisherService.publishWalletUpdate(message);
}
```

## Usage Example

Here's how to integrate this into your wallet operations:

```java
// In a controller or service that adds funds to a wallet
@Service
public class WalletOperationService {
    
    private final WalletService walletService;
    private final MessagePublisherService messageService;
    
    @Autowired
    public WalletOperationService(WalletService walletService, MessagePublisherService messageService) {
        this.walletService = walletService;
        this.messageService = messageService;
    }
    
    public void processRefund(String userId, Double amount, String reclamationId) {
        // 1. Add funds to wallet
        String transactionId = walletService.addFundsToWallet(userId, amount, "Refund for reclamation: " + reclamationId);
        
        // 2. Get updated wallet
        Wallet wallet = walletService.getWalletByUserId(userId);
        
        // 3. Notify about wallet update
        WalletUpdateMessage updateMessage = new WalletUpdateMessage(
            userId,
            wallet.getId(),
            wallet.getBalance(),
            transactionId,
            "REFUND"
        );
        messageService.publishWalletUpdate(updateMessage);
        
        // 4. Notify about refund result
        RefundResultMessage resultMessage = new RefundResultMessage(
            reclamationId,
            true,
            "Refund processed successfully",
            transactionId,
            amount
        );
        messageService.publishRefundResult(resultMessage);
    }
}
```

This implementation connects your Payment Service with the Reclamation Service using RabbitMQ messaging.
