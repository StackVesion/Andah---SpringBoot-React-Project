# Payment Service API Documentation

This document provides comprehensive documentation for all endpoints available in the Payment Service of the Andah Project, including examples for Postman testing.

## Table of Contents
- [Payment Endpoints](#payment-endpoints)
- [Wallet Endpoints](#wallet-endpoints)
- [Card Transaction Endpoints](#card-transaction-endpoints)
- [Crypto Transaction Endpoints](#crypto-transaction-endpoints)
- [Error Handling](#error-handling)

## Payment Endpoints

### Create Payment Transaction

```
POST /api/payments
```

**Request Body:**
```json
{
  "userId": 123,
  "scooterId": 456,
  "reservationId": 789,
  "amount": 25.50,
  "paymentMethod": "CREDIT_CARD", // CREDIT_CARD, CASH, CRYPTO, WALLET
  "transactionReference": "TRANS-123456",
  "status": "PENDING", // PENDING, COMPLETED, FAILED, REFUNDED
  "cardTransactionId": "card123", // Required for CREDIT_CARD payments
  "cryptoTransactionId": "crypto123", // Required for CRYPTO payments
  "walletTransactionId": "wallet123", // Required for WALLET payments
  "currency": "USD" // Optional, mainly for crypto
}
```

**Response:**
```json
{
  "id": "pay123",
  "userId": 123,
  "scooterId": 456,
  "reservationId": 789,
  "amount": 25.50,
  "paymentMethod": "CREDIT_CARD",
  "transactionReference": "TRANS-123456",
  "status": "PENDING",
  "timestamp": "2025-04-10T20:00:00",
  "cardTransactionId": "card123",
  "currency": "USD"
}
```

### Get All Payments

```
GET /api/payments
```

**Response:**
```json
[
  {
    "id": "pay123",
    "userId": 123,
    "amount": 25.50,
    "paymentMethod": "CREDIT_CARD",
    "status": "PENDING",
    "timestamp": "2025-04-10T20:00:00"
  },
  {
    "id": "pay124",
    "userId": 456,
    "amount": 30.00,
    "paymentMethod": "CRYPTO",
    "status": "COMPLETED",
    "timestamp": "2025-04-10T21:00:00"
  }
]
```

### Get Payment by ID

```
GET /api/payments/{id}
```

**Response:**
```json
{
  "id": "pay123",
  "userId": 123,
  "scooterId": 456,
  "reservationId": 789,
  "amount": 25.50,
  "paymentMethod": "CREDIT_CARD",
  "transactionReference": "TRANS-123456",
  "status": "PENDING",
  "timestamp": "2025-04-10T20:00:00",
  "cardTransactionId": "card123",
  "currency": "USD"
}
```

### Get Payment by ID with Transaction Details

```
GET /api/payments/{id}/details
```

**Response for Card Payment:**
```json
{
  "payment": {
    "id": "pay123",
    "userId": 123,
    "amount": 25.50,
    "paymentMethod": "CREDIT_CARD",
    "status": "COMPLETED",
    "timestamp": "2025-04-10T20:00:00"
  },
  "cardTransaction": {
    "id": "card123",
    "cardNumber": "411111******1111",
    "dateExp": "12/25",
    "cardHolderName": "John Doe",
    "amount": 25.50,
    "transactionStatus": "COMPLETED"
  }
}
```

**Response for Crypto Payment:**
```json
{
  "payment": {
    "id": "pay124",
    "userId": 456,
    "amount": 30.00,
    "paymentMethod": "CRYPTO",
    "status": "PENDING",
    "timestamp": "2025-04-10T21:00:00"
  },
  "cryptoTransaction": {
    "id": "crypto123",
    "paymentStatus": "WAITING",
    "priceAmount": 30.00,
    "priceCurrency": "USD",
    "payCurrency": "BTC",
    "payAmount": 0.0012,
    "payAddress": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
    "nowPaymentId": "5927234"
  }
}
```

### Get Payments by User ID

```
GET /api/payments/user/{userId}
```

### Get Payments by Reservation ID

```
GET /api/payments/reservation/{reservationId}
```

### Get Payments by Status

```
GET /api/payments/status/{status}
```

### Update Payment Status

```
PUT /api/payments/{id}/status/{status}
```

**Response:**
```json
{
  "id": "pay123",
  "userId": 123,
  "status": "COMPLETED",
  "timestamp": "2025-04-10T20:00:00"
}
```

### Delete Payment

```
DELETE /api/payments/{id}
```

## Wallet Endpoints

### Get User's Wallet

```
GET /api/wallets/{userId}
```

**Response:**
```json
{
  "id": "wallet123",
  "userId": 123,
  "balance": 50.75,
  "createdAt": "2025-04-01T00:00:00",
  "updatedAt": "2025-04-10T20:00:00"
}
```

### Get Wallet Balance

```
GET /api/wallets/{walletId}/balance
```

**Response:**
```json
{
  "balance": 50.75,
  "currency": "USD"
}
```

### Get Wallet Transactions

```
GET /api/wallets/{walletId}/transactions
```

**Response:**
```json
[
  {
    "id": "wt123",
    "walletId": "wallet123",
    "amount": 25.00,
    "transactionType": "DEPOSIT",
    "status": "COMPLETED",
    "timestamp": "2025-04-05T12:30:00",
    "paymentId": "pay123",
    "cardTransactionId": "card123"
  },
  {
    "id": "wt124",
    "walletId": "wallet123",
    "amount": 10.50,
    "transactionType": "WITHDRAWAL",
    "status": "COMPLETED",
    "timestamp": "2025-04-08T15:45:00",
    "paymentId": "pay124"
  }
]
```

### Recharge Wallet with Card

```
POST /api/wallets/{userId}/recharge/card
```

**Request Body:**
```json
{
  "amount": 25.00,
  "cardNumber": "4111111111111111",
  "dateExp": "12/25",
  "cardHolderName": "John Doe",
  "billingAddress": "123 Main St",
  "zipCode": "10001",
  "state": "NY",
  "region": "USA"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Wallet recharged successfully",
  "walletTransaction": {
    "id": "wt123",
    "walletId": "wallet123",
    "amount": 25.00,
    "transactionType": "DEPOSIT",
    "status": "COMPLETED",
    "timestamp": "2025-04-10T20:00:00",
    "cardTransactionId": "card123"
  },
  "newBalance": 75.75
}
```

### Recharge Wallet with Crypto

```
POST /api/wallets/{userId}/recharge/crypto
```

**Request Body:**
```json
{
  "amount": 25.00,
  "priceCurrency": "USD",
  "payCurrency": "BTC",
  "orderId": "crypto-order-123",
  "orderDescription": "Wallet recharge"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Crypto payment initiated",
  "walletTransaction": {
    "id": "wt124",
    "walletId": "wallet123",
    "amount": 25.00,
    "transactionType": "DEPOSIT",
    "status": "PENDING",
    "timestamp": "2025-04-10T20:00:00",
    "cryptoTransactionId": "crypto123"
  },
  "cryptoPayment": {
    "paymentStatus": "WAITING",
    "payAmount": 0.0012,
    "payCurrency": "BTC",
    "payAddress": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
    "nowPaymentId": "5927234"
  }
}
```

### Pay with Wallet

```
POST /api/wallets/{userId}/pay
```

**Request Body:**
```json
{
  "amount": 15.00,
  "reservationId": 789,
  "scooterId": 456
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment successful",
  "walletTransaction": {
    "id": "wt125",
    "walletId": "wallet123",
    "amount": 15.00,
    "transactionType": "WITHDRAWAL",
    "status": "COMPLETED",
    "timestamp": "2025-04-10T20:00:00"
  },
  "payment": {
    "id": "pay125",
    "userId": 123,
    "scooterId": 456,
    "reservationId": 789,
    "amount": 15.00,
    "paymentMethod": "WALLET",
    "status": "COMPLETED",
    "timestamp": "2025-04-10T20:00:00",
    "walletTransactionId": "wt125"
  },
  "newBalance": 60.75
}
```

## Card Transaction Endpoints

### Create Card Transaction

```
POST /api/card-transactions
```

**Request Body:**
```json
{
  "userId": 123,
  "paymentId": "pay123",
  "cardNumber": "4111111111111111",
  "dateExp": "12/25",
  "amount": 25.50,
  "cardHolderName": "John Doe",
  "billingAddress": "123 Main St",
  "zipCode": "10001",
  "state": "NY",
  "region": "USA"
}
```

**Response:**
```json
{
  "id": "card123",
  "userId": 123,
  "paymentId": "pay123",
  "cardNumber": "411111******1111",
  "dateExp": "12/25",
  "amount": 25.50,
  "cardHolderName": "John Doe",
  "transactionDate": "2025-04-10T20:00:00",
  "transactionStatus": "COMPLETED"
}
```

### Get Card Transaction by ID

```
GET /api/card-transactions/{id}
```

### Get Card Transactions by User ID

```
GET /api/card-transactions/user/{userId}
```

## Crypto Transaction Endpoints

### Create Crypto Transaction

```
POST /api/crypto-transactions
```

**Request Body:**
```json
{
  "userId": 123,
  "paymentId": "pay123",
  "paymentStatus": "PENDING",
  "priceAmount": 25.50,
  "priceCurrency": "USD",
  "payCurrency": "BTC",
  "orderId": "crypto-order-123",
  "orderDescription": "Payment for reservation"
}
```

**Response:**
```json
{
  "id": "crypto123",
  "userId": 123,
  "paymentId": "pay123",
  "paymentStatus": "PENDING",
  "priceAmount": 25.50,
  "priceCurrency": "USD",
  "payCurrency": "BTC",
  "orderId": "crypto-order-123",
  "orderDescription": "Payment for reservation",
  "date": "2025-04-10T20:00:00"
}
```

### Create Crypto Payment via NowPayments API

```
POST /api/crypto-transactions/nowpayments/create
```

**Request Body:**
```json
{
  "userId": 123,
  "priceAmount": 25.50,
  "priceCurrency": "USD",
  "payCurrency": "BTC",
  "orderId": "order-123456",
  "orderDescription": "Payment for reservation 789",
  "ipnCallbackUrl": "https://your-api.com/ipn/crypto"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Crypto payment created successfully",
  "transaction": {
    "id": "crypto123",
    "userId": 123,
    "paymentStatus": "WAITING",
    "priceAmount": 25.50,
    "priceCurrency": "USD",
    "payCurrency": "BTC",
    "payAmount": 0.0012,
    "payAddress": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
    "orderId": "order-123456",
    "orderDescription": "Payment for reservation 789",
    "nowPaymentId": "5927234",
    "date": "2025-04-10T20:00:00"
  },
  "paymentAddress": "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
  "payAmount": 0.0012,
  "payCurrency": "BTC",
  "nowPaymentId": "5927234",
  "status": "WAITING"
}
```

### Handle IPN Callback

```
POST /api/crypto-transactions/nowpayments/ipn-callback
```

**Request Body (sent by NowPayments):**
```json
{
  "payment_id": "5927234",
  "payment_status": "CONFIRMED",
  "pay_amount": 0.0012,
  "actually_paid": 0.0012,
  "pay_currency": "BTC",
  "order_id": "order-123456"
}
```

**Response:**
```text
IPN Processed Successfully
```

### Get Crypto Transaction by ID

```
GET /api/crypto-transactions/{id}
```

### Get Crypto Transactions by User ID

```
GET /api/crypto-transactions/user/{userId}
```

### Get Crypto Transactions by Status

```
GET /api/crypto-transactions/status/{status}
```

## Error Handling

All API endpoints return error responses in JSON format with HTTP status codes:

```json
{
  "timestamp": "2025-04-10T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Card transaction ID is required for credit card payments",
  "path": "/api/payments"
}
```

### Common Error Validations

#### Payment Creation
- When creating a payment with CREDIT_CARD method, cardTransactionId is required
- When creating a payment with CRYPTO method, cryptoTransactionId is required
- When creating a payment with WALLET method, walletTransactionId is required

#### Crypto Payment Creation
- UserId is required
- PriceAmount must be greater than zero
- PriceCurrency is required
- PayCurrency must be a supported cryptocurrency

#### Wallet Operations
- Insufficient balance for payment operations
- Invalid wallet ID
- Invalid user ID

## Testing in Postman

### Setup

1. Import the following collection into Postman: [Andah Payment Service.postman_collection.json](https://github.com/andah-project/payment-service/blob/main/Andah%20Payment%20Service.postman_collection.json)

2. Create an environment with the following variables:
   - `baseUrl`: http://localhost:8080
   - `userId`: 123 (example user ID)

### Testing Flow

1. **Create a Card Transaction**
   - Use the "Create Card Transaction" request
   - Save the returned `id` as `cardTransactionId`

2. **Create a Payment with Card**
   - Use the "Create Payment" request with the saved `cardTransactionId`
   - Set `paymentMethod` to "CREDIT_CARD"

3. **Create a Crypto Payment via NowPayments**
   - Use the "Create Crypto Payment" request
   - Save the returned `transaction.id` as `cryptoTransactionId`
   - Save the returned `nowPaymentId`

4. **Manually update status (for testing)**
   - Use the "Update Payment Status" request
   - Change status to "COMPLETED"

5. **Get Payment Details**
   - Use the "Get Payment by ID with Details" request to see transaction details

6. **Test Wallet Operations**
   - Create a wallet (if not exists) using "Get User's Wallet"
   - Recharge wallet using card or crypto
   - Make a payment using the wallet
   - Check wallet balance and transactions
