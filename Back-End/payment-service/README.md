# Payment Service

## Overview
The Payment Service handles all financial transactions within the Andah Scooter Rental Platform. It processes payments for scooter reservations, manages transaction records, and provides payment history. This service now supports different payment methods, including credit card and cryptocurrency transactions. It integrates with Stripe for card payments and NowPayments.io for cryptocurrency transactions.

## Features
- Payment processing for reservations
- Transaction record management
- Multiple payment method support (Credit Card, Cash, Crypto)
- Payment status tracking (Pending, Completed, Failed, Refunded)
- User payment history
- Card transaction tracking with Stripe integration
- Crypto transaction tracking with NowPayments.io integration
- Secure handling of payment information
- Masked card numbers for security

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Data MongoDB**: Database access
- **MongoDB**: Database for transaction storage
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Cloud OpenFeign**: Inter-service communication
- **Stripe API**: For credit card payment processing
- **NowPayments.io API**: For cryptocurrency payment processing

## Database Schema
The service uses MongoDB with the following collections:
- `payment_transactions`: Contains payment records including amount, date, payment method, user ID, scooter ID, reservation ID
- `card_transactions`: Credit card payment details with sensitive data masked
- `crypto_transactions`: Cryptocurrency payment details and tracking information

## API Endpoints Reference

### Payment Management

#### 1. Create a Payment
- **Endpoint**: `POST /api/payments`
- **Description**: Process a new payment for a reservation
- **Request Body**:
```json
{
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "scooterId": 5001,
  "reservationId": 3001,
  "status": "PENDING",
  "cardTransactionId": "609a8b9d112c7832e7bce416",
  "currency": "USD"
}
```
- **Notes**: For CREDIT_CARD payments, cardTransactionId is required. For CRYPTO payments, cryptoTransactionId is required.
- **Response** (201 Created):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "scooterId": 5001,
  "reservationId": 3001,
  "status": "PENDING",
  "cardTransactionId": "609a8b9d112c7832e7bce416",
  "currency": "USD"
}
```

#### 2. Get All Payments
- **Endpoint**: `GET /api/payments`
- **Description**: Retrieve all payment transactions
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "userId": 1001,
    "status": "COMPLETED"
  },
  {
    "id": "609a8c2a112c7832e7bce418",
    "timestamp": "2023-06-15T15:45:30",
    "paymentMethod": "CRYPTO",
    "amount": 18.75,
    "userId": 1002,
    "status": "PENDING"
  }
]
```

#### 3. Get Payment by ID
- **Endpoint**: `GET /api/payments/{id}`
- **Description**: Retrieve a specific payment transaction by ID
- **Path Parameter**: `id` - Payment transaction ID
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "scooterId": 5001,
  "reservationId": 3001,
  "status": "COMPLETED",
  "cardTransactionId": "609a8b9d112c7832e7bce416",
  "currency": "USD"
}
```

#### 4. Get Payments by User ID
- **Endpoint**: `GET /api/payments/user/{userId}`
- **Description**: Retrieve all payments for a specific user
- **Path Parameter**: `userId` - User ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "status": "COMPLETED"
  },
  {
    "id": "609a8b9d112c7832e7bce419",
    "timestamp": "2023-06-16T09:22:10",
    "paymentMethod": "CASH",
    "amount": 15.00,
    "status": "COMPLETED"
  }
]
```

#### 5. Get Payments by Status
- **Endpoint**: `GET /api/payments/status/{status}`
- **Description**: Retrieve all payments with a specific status
- **Path Parameter**: `status` - Payment status (PENDING, COMPLETED, FAILED, REFUNDED)
- **Response** (200 OK):
```json
[
  {
    "id": "609a8c2a112c7832e7bce418",
    "timestamp": "2023-06-15T15:45:30",
    "paymentMethod": "CRYPTO",
    "amount": 18.75,
    "userId": 1002,
    "status": "PENDING"
  },
  {
    "id": "609a8c2a112c7832e7bce420",
    "timestamp": "2023-06-17T11:12:05",
    "paymentMethod": "CREDIT_CARD",
    "amount": 32.25,
    "userId": 1003,
    "status": "PENDING"
  }
]
```

#### 6. Get Payments by Reservation ID
- **Endpoint**: `GET /api/payments/reservation/{reservationId}`
- **Description**: Retrieve all payments for a specific reservation
- **Path Parameter**: `reservationId` - Reservation ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "userId": 1001,
    "reservationId": 3001,
    "status": "COMPLETED"
  }
]
```

#### 7. Update Payment Status
- **Endpoint**: `PUT /api/payments/{id}/status`
- **Description**: Update the status of a payment transaction
- **Path Parameter**: `id` - Payment transaction ID
- **Request Body**:
```json
{
  "status": "COMPLETED"
}
```
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "status": "COMPLETED",
  "message": "Payment status updated successfully"
}
```

#### 8. Delete Payment
- **Endpoint**: `DELETE /api/payments/{id}`
- **Description**: Delete a payment transaction
- **Path Parameter**: `id` - Payment transaction ID
- **Response** (204 No Content)

### Card Transaction Management

#### 1. Create a Card Transaction
- **Endpoint**: `POST /api/card-transactions`
- **Description**: Process a new card transaction using Stripe
- **Request Body**:
```json
{
  "userId": 1001,
  "cardNumber": "4111111111111111",
  "dateExp": "12/25",
  "cvv": "123",
  "amount": 25.50,
  "billingAddress": "123 Main St",
  "zipCode": "10001",
  "cardHolderName": "John Doe",
  "state": "NY",
  "region": "North America"
}
```
- **Response** (201 Created):
```json
{
  "id": "609a8b9d112c7832e7bce416",
  "userId": 1001,
  "cardNumber": "XXXX-XXXX-XXXX-1111",
  "dateExp": "12/25",
  "amount": 25.50,
  "date": "2023-06-15T14:30:45",
  "transactionId": "pi_1O6EgqJMnO1PV0IaJcYvTGhP",
  "status": "succeeded"
}
```

#### 2. Get All Card Transactions
- **Endpoint**: `GET /api/card-transactions`
- **Description**: Retrieve all card transactions
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce416",
    "userId": 1001,
    "cardNumber": "XXXX-XXXX-XXXX-1111",
    "dateExp": "12/25",
    "amount": 25.50,
    "date": "2023-06-15T14:30:45"
  },
  {
    "id": "609a8c2a112c7832e7bce422",
    "userId": 1003,
    "cardNumber": "XXXX-XXXX-XXXX-4444",
    "dateExp": "03/24",
    "amount": 32.25,
    "date": "2023-06-17T11:10:30"
  }
]
```

#### 3. Get Card Transaction by ID
- **Endpoint**: `GET /api/card-transactions/{id}`
- **Description**: Retrieve a specific card transaction by ID
- **Path Parameter**: `id` - Card transaction ID
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce416",
  "userId": 1001,
  "cardNumber": "XXXX-XXXX-XXXX-1111",
  "dateExp": "12/25",
  "amount": 25.50,
  "date": "2023-06-15T14:30:45",
  "transactionId": "pi_1O6EgqJMnO1PV0IaJcYvTGhP",
  "status": "succeeded"
}
```

#### 4. Get Card Transactions by User ID
- **Endpoint**: `GET /api/card-transactions/user/{userId}`
- **Description**: Retrieve all card transactions for a specific user
- **Path Parameter**: `userId` - User ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce416",
    "userId": 1001,
    "cardNumber": "XXXX-XXXX-XXXX-1111",
    "dateExp": "12/25",
    "amount": 25.50,
    "date": "2023-06-15T14:30:45"
  }
]
```

#### 5. Get Card Transactions by Payment ID
- **Endpoint**: `GET /api/card-transactions/payment/{paymentId}`
- **Description**: Retrieve all card transactions for a specific payment
- **Path Parameter**: `paymentId` - Payment ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce416",
    "userId": 1001,
    "cardNumber": "XXXX-XXXX-XXXX-1111",
    "dateExp": "12/25",
    "amount": 25.50,
    "date": "2023-06-15T14:30:45",
    "paymentId": "609a8b9d112c7832e7bce417"
  }
]
```

### Crypto Transaction Management

#### 1. Create a Crypto Transaction
- **Endpoint**: `POST /api/crypto-transactions/nowpayments/create`
- **Description**: Create a new cryptocurrency payment via NowPayments.io API
- **Request Body**:
```json
{
  "userId": 1002,
  "priceAmount": 150.00,
  "priceCurrency": "usd",
  "payCurrency": "btc",
  "orderId": "ORDER-12345",
  "orderDescription": "Payment for scooter rental",
  "ipnCallbackUrl": "https://your-api.com/ipn/crypto"
}
```
- **Response** (201 Created):
```json
{
  "id": "609a8c2a112c7832e7bce425",
  "userId": 1002,
  "date": "2023-06-15T16:45:22",
  "nowPaymentId": "NP123456",
  "paymentStatus": "WAITING",
  "payAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "payAmount": 0.00045,
  "payCurrency": "BTC",
  "priceAmount": 150.00,
  "priceCurrency": "USD",
  "orderId": "ORDER-12345"
}
```

#### 2. Get Crypto Payment Status
- **Endpoint**: `GET /api/crypto-transactions/nowpayments/status/{nowPaymentId}`
- **Description**: Check the status of a cryptocurrency payment with NowPayments.io
- **Path Parameter**: `nowPaymentId` - NowPayments payment ID
- **Response** (200 OK):
```json
{
  "status": "WAITING"
}
```

#### 3. Get All Crypto Transactions
- **Endpoint**: `GET /api/crypto-transactions`
- **Description**: Retrieve all cryptocurrency transactions
- **Response** (200 OK):
```json
[
  {
    "id": "609a8c2a112c7832e7bce425",
    "userId": 1002,
    "date": "2023-06-15T16:45:22",
    "nowPaymentId": "NP123456",
    "paymentStatus": "WAITING",
    "payAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    "payCurrency": "BTC"
  }
]
```

#### 4. Get Crypto Transaction by ID
- **Endpoint**: `GET /api/crypto-transactions/{id}`
- **Description**: Retrieve a specific cryptocurrency transaction by ID
- **Path Parameter**: `id` - Crypto transaction ID
- **Response** (200 OK):
```json
{
  "id": "609a8c2a112c7832e7bce425",
  "userId": 1002,
  "date": "2023-06-15T16:45:22",
  "nowPaymentId": "NP123456",
  "paymentStatus": "WAITING",
  "payAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "payAmount": 0.00045,
  "payCurrency": "BTC",
  "priceAmount": 150.00,
  "priceCurrency": "USD",
  "orderId": "ORDER-12345"
}
```

#### 5. Get Crypto Transactions by User ID
- **Endpoint**: `GET /api/crypto-transactions/user/{userId}`
- **Description**: Retrieve all cryptocurrency transactions for a specific user
- **Path Parameter**: `userId` - User ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8c2a112c7832e7bce425",
    "userId": 1002,
    "date": "2023-06-15T16:45:22",
    "paymentStatus": "WAITING",
    "payCurrency": "BTC",
    "priceAmount": 150.00
  }
]
```

#### 6. Get Crypto Transactions by Payment ID
- **Endpoint**: `GET /api/crypto-transactions/payment/{paymentId}`
- **Description**: Retrieve all cryptocurrency transactions for a specific payment
- **Path Parameter**: `paymentId` - Payment ID
- **Response** (200 OK):
```json
[
  {
    "id": "609a8c2a112c7832e7bce425",
    "userId": 1002,
    "paymentId": "609a8c2a112c7832e7bce418",
    "date": "2023-06-15T16:45:22",
    "payCurrency": "BTC",
    "priceAmount": 150.00,
    "paymentStatus": "WAITING"
  }
]
```

#### 7. Get Crypto Transactions by Status
- **Endpoint**: `GET /api/crypto-transactions/status/{status}`
- **Description**: Retrieve all cryptocurrency transactions with a specific status
- **Path Parameter**: `status` - Payment status
- **Response** (200 OK):
```json
[
  {
    "id": "609a8c2a112c7832e7bce425",
    "userId": 1002,
    "paymentId": "609a8c2a112c7832e7bce418",
    "date": "2023-06-15T16:45:22",
    "payCurrency": "BTC",
    "priceAmount": 18.75,
    "nowPaymentId": "NP123456",
    "paymentStatus": "COMPLETED",
    "payAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    "payAmount": 0.00045
  }
]
```

## Installation and Setup

### Prerequisites
- Java 17
- MongoDB
- Maven
- Eureka Server (for service discovery)

### Configuration
The following environment variables or application.properties settings are required:

```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=andah_payments
spring.data.mongodb.username=mongodb
spring.data.mongodb.password=mongodb

# Stripe API Configuration
stripe.api.key=your_stripe_api_key

# NowPayments API Configuration
nowpayments.api.key=your_nowpayments_api_key
nowpayments.api.url=https://api.nowpayments.io/v1
nowpayments.ipn.callback.url=https://your-api.com/api/crypto-transactions/nowpayments/ipn-callback
```

## Docker Build & Deployment Instructions

### After Making Code Changes
When you make code changes to the payment service, you need to follow these steps to apply them to the Docker container:

1. **Compile the code first**:
   ```bash
   cd "c:\Users\nihed\Desktop\Andah Project\BackEnd\payment-service"
   ./mvnw clean package -DskipTests
   ```

2. **Rebuild the Docker image**:
   ```bash
   cd "c:\Users\nihed\Desktop\Andah Project\BackEnd\payment-service"
   docker build -t payment-service .
   ```

3. **Restart the container** (from the project root):
   ```bash
   cd "c:\Users\nihed\Desktop\Andah Project\BackEnd"
   docker-compose up -d --no-deps --build payment-service
   ```
   - This command rebuilds and restarts only the payment-service container
   - The `--no-deps` flag prevents dependent services from being recreated
   - The `--build` flag ensures the image is rebuilt

### Quick Commands

**One-line rebuild and restart**:
```bash
cd "c:\Users\nihed\Desktop\Andah Project\BackEnd\payment-service" ; ./mvnw clean package -DskipTests ; docker build -t payment-service . ; cd .. ; docker-compose up -d --no-deps --build payment-service
```

### Troubleshooting

- **If you see serialization errors**: This usually means the Docker container is still running an old version of the code. Make sure to follow all the steps above.

- **Checking logs**: To see the payment service logs after deployment:
  ```bash
  docker-compose logs -f payment-service
  ```

- **Container status**: To confirm the container is running the new version:
  ```bash
  docker-compose ps payment-service
  ```

## Running the Service

### Prerequisites
1. MongoDB database (version 4.4+)
2. Eureka Server running on port 8761
3. Config Server running on port 8888
4. JDK 17
5. Maven 3.8+

### Local Development
1. Ensure MongoDB is running
   ```bash
   docker run -d --name mongodb -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=mongodb -e MONGO_INITDB_ROOT_PASSWORD=mongodb mongo:latest
   ```

2. Start Eureka Server and Config Server
   ```bash
   cd eureka-server
   mvn spring-boot:run

   cd ../config-server
   mvn spring-boot:run
   ```

3. Run the payment service:
   ```bash
   cd payment-service
   mvn spring-boot:run
   ```

### Docker Deployment
1. Build the Docker image:
   ```bash
   docker build -t andah/payment-service .
   ```

2. Run the service in Docker:
   ```bash
   docker run -p 8085:8085 --network andah-network andah/payment-service
   ```

3. Using Docker Compose:
   ```bash
   docker-compose up -d payment-service
   ```

## Dependencies
This service communicates with:
- **User Service**: To validate user information
- **Reservation Service**: To validate reservation information and status
- **API Gateway**: For routing requests
- **Config Server**: For configuration properties
- **Eureka Server**: For service registration and discovery

## Troubleshooting
- If you encounter connection issues with MongoDB, verify MongoDB is running and credentials are correct
- Ensure the service can connect to Eureka and Config Server
- Check logs for specific error messages using:
  ```bash
  docker logs payment-service
  ```

## Data Models

### PaymentTransaction
```java
public class PaymentTransactionDto {
    private String id;
    private LocalDateTime timestamp;
    private PaymentMethod paymentMethod; // CREDIT_CARD, PAYPAL, MOBILE_MONEY
    private Double amount;
    private String transactionReference;
    private Long userId;
    private String userName;
    private Long scooterId;
    private String scooterName;
    private Long reservationId;
    private PaymentStatus status; // PENDING, COMPLETED, FAILED, REFUNDED
}
```

## Configuration
Key application properties include:
```properties
spring.application.name=payment-service
server.port=8085
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=andah_payments
spring.data.mongodb.username=mongodb
spring.data.mongodb.password=mongodb
spring.data.mongodb.authentication-database=admin
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.config.import=optional:configserver:http://localhost:8888
```

## Payment Operations (Additional Endpoints)

#### Process a Refund
- **Endpoint**: `POST /api/payments/refund/{id}`
- **Description**: Process a refund for a specific payment
- **Path Parameter**: `id` - Payment ID to refund
- **Example**: `POST /api/payments/refund/609a8b9d112c7832e7bce417`
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345-REFUND",
  "userId": 1001,
  "userName": "John Doe",
  "scooterId": 5001,
  "scooterName": "Scooter X-200",
  "reservationId": 3001,
  "status": "REFUNDED"
}
```

#### Update Payment Status
- **Endpoint**: `PUT /api/payments/{id}/status`
- **Description**: Update the status of a payment
- **Path Parameter**: `id` - Payment ID
- **Request Body**:
```json
{
  "status": "COMPLETED"
}
```
- **Example**: `PUT /api/payments/609a8b9d112c7832e7bce417/status`
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "userName": "John Doe",
  "scooterId": 5001,
  "scooterName": "Scooter X-200",
  "reservationId": 3001,
  "status": "COMPLETED"
}
```

#### Get Payments by Status
- **Endpoint**: `GET /api/payments/status/{status}`
- **Description**: Get all payments with a specific status
- **Path Parameter**: `status` - Payment status (PENDING, COMPLETED, FAILED, REFUNDED)
- **Example**: `GET /api/payments/status/COMPLETED`
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "transactionReference": "TXN12345",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5001,
    "scooterName": "Scooter X-200",
    "reservationId": 3001,
    "status": "COMPLETED"
  },
  {
    "id": "609a8c2a112c7832e7bce418",
    "timestamp": "2023-06-15T16:45:22",
    "paymentMethod": "PAYPAL",
    "amount": 18.75,
    "transactionReference": "TXN12346",
    "userId": 1002,
    "userName": "Jane Smith",
    "scooterId": 5002,
    "scooterName": "Scooter Y-100",
    "reservationId": 3002,
    "status": "COMPLETED"
  }
]
```

#### Get All Payments
- **Endpoint**: `GET /api/payments`
- **Description**: Retrieve all payment transactions (admin only)
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "transactionReference": "TXN12345",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5001,
    "scooterName": "Scooter X-200",
    "reservationId": 3001,
    "status": "COMPLETED"
  },
  {
    "id": "609a8c2a112c7832e7bce418",
    "timestamp": "2023-06-15T16:45:22",
    "paymentMethod": "PAYPAL",
    "amount": 18.75,
    "transactionReference": "TXN12346",
    "userId": 1002,
    "userName": "Jane Smith",
    "scooterId": 5002,
    "scooterName": "Scooter Y-100",
    "reservationId": 3002,
    "status": "COMPLETED"
  }
]
```

#### Get Payment by ID
- **Endpoint**: `GET /api/payments/{id}`
- **Description**: Get details of a specific payment transaction
- **Path Parameter**: `id` - Payment transaction ID
- **Example**: `GET /api/payments/609a8b9d112c7832e7bce417`
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "userName": "John Doe",
  "scooterId": 5001,
  "scooterName": "Scooter X-200",
  "reservationId": 3001,
  "status": "COMPLETED"
}
```
- **Response** (404 Not Found):
```json
{
  "message": "Payment not found with id: 609a8b9d112c7832e7bce417"
}
```

#### Get Payments by User ID
- **Endpoint**: `GET /api/payments/user/{userId}`
- **Description**: Get all payments made by a specific user
- **Path Parameter**: `userId` - User identifier
- **Example**: `GET /api/payments/user/1001`
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "transactionReference": "TXN12345",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5001,
    "scooterName": "Scooter X-200",
    "reservationId": 3001,
    "status": "COMPLETED"
  },
  {
    "id": "609a9c3d112c7832e7bce419",
    "timestamp": "2023-06-16T10:15:30",
    "paymentMethod": "MOBILE_MONEY",
    "amount": 32.75,
    "transactionReference": "TXN12350",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5003,
    "scooterName": "Scooter Z-300",
    "reservationId": 3005,
    "status": "COMPLETED"
  }
]
```

#### Get Payments by Reservation ID
- **Endpoint**: `GET /api/payments/reservation/{reservationId}`
- **Description**: Get all payments associated with a specific reservation
- **Path Parameter**: `reservationId` - Reservation identifier
- **Example**: `GET /api/payments/reservation/3001`
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "transactionReference": "TXN12345",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5001,
    "scooterName": "Scooter X-200",
    "reservationId": 3001,
    "status": "COMPLETED"
  }
]
```

### Payment Operations (Additional Endpoints)

#### Process a Refund
- **Endpoint**: `POST /api/payments/refund/{id}`
- **Description**: Process a refund for a specific payment
- **Path Parameter**: `id` - Payment ID to refund
- **Example**: `POST /api/payments/refund/609a8b9d112c7832e7bce417`
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345-REFUND",
  "userId": 1001,
  "userName": "John Doe",
  "scooterId": 5001,
  "scooterName": "Scooter X-200",
  "reservationId": 3001,
  "status": "REFUNDED"
}
```

#### Update Payment Status
- **Endpoint**: `PUT /api/payments/{id}/status`
- **Description**: Update the status of a payment
- **Path Parameter**: `id` - Payment ID
- **Request Body**:
```json
{
  "status": "COMPLETED"
}
```
- **Example**: `PUT /api/payments/609a8b9d112c7832e7bce417/status`
- **Response** (200 OK):
```json
{
  "id": "609a8b9d112c7832e7bce417",
  "timestamp": "2023-06-15T14:32:15",
  "paymentMethod": "CREDIT_CARD",
  "amount": 25.50,
  "transactionReference": "TXN12345",
  "userId": 1001,
  "userName": "John Doe",
  "scooterId": 5001,
  "scooterName": "Scooter X-200",
  "reservationId": 3001,
  "status": "COMPLETED"
}
```

#### Get Payments by Status
- **Endpoint**: `GET /api/payments/status/{status}`
- **Description**: Get all payments with a specific status
- **Path Parameter**: `status` - Payment status (PENDING, COMPLETED, FAILED, REFUNDED)
- **Example**: `GET /api/payments/status/COMPLETED`
- **Response** (200 OK):
```json
[
  {
    "id": "609a8b9d112c7832e7bce417",
    "timestamp": "2023-06-15T14:32:15",
    "paymentMethod": "CREDIT_CARD",
    "amount": 25.50,
    "transactionReference": "TXN12345",
    "userId": 1001,
    "userName": "John Doe",
    "scooterId": 5001,
    "scooterName": "Scooter X-200",
    "reservationId": 3001,
    "status": "COMPLETED"
  },
  {
    "id": "609a8c2a112c7832e7bce418",
    "timestamp": "2023-06-15T16:45:22",
    "paymentMethod": "PAYPAL",
    "amount": 18.75,
    "transactionReference": "TXN12346",
    "userId": 1002,
    "userName": "Jane Smith",
    "scooterId": 5002,
    "scooterName": "Scooter Y-100",
    "reservationId": 3002,
    "status": "COMPLETED"
  }
]
```
