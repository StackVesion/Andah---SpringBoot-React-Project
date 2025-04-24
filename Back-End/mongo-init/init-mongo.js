// MongoDB initialization script for Andah Payment Service
db = db.getSiblingDB('andah_payments');

// Create user with read/write access to the payment database
db.createUser({
  user: 'payment_app',
  pwd: 'payment_password',
  roles: [
    { role: 'readWrite', db: 'andah_payments' }
  ]
});

// Create collections for payment service
db.createCollection('paymentTransaction');
db.createCollection('cryptoTransaction');
db.createCollection('cardTransaction');
db.createCollection('wallet');
db.createCollection('walletTransaction');

// Create indexes for better query performance
db.paymentTransaction.createIndex({ "userId": 1 });
db.paymentTransaction.createIndex({ "status": 1 });
db.paymentTransaction.createIndex({ "createdAt": -1 });

db.cryptoTransaction.createIndex({ "userId": 1 });
db.cryptoTransaction.createIndex({ "status": 1 });
db.cryptoTransaction.createIndex({ "nowPaymentsId": 1 }, { unique: true, sparse: true });

db.cardTransaction.createIndex({ "userId": 1 });
db.cardTransaction.createIndex({ "status": 1 });

db.wallet.createIndex({ "userId": 1 }, { unique: true });
db.walletTransaction.createIndex({ "userId": 1 });
db.walletTransaction.createIndex({ "walletId": 1 });
db.walletTransaction.createIndex({ "type": 1 });
db.walletTransaction.createIndex({ "createdAt": -1 });

// Insert initial wallet data for testing if needed
/*
db.wallet.insertOne({
  userId: "test-user-id",
  balance: 0,
  currency: "USD",
  createdAt: new Date(),
  updatedAt: new Date()
});
*/

print("MongoDB initialization completed successfully");
