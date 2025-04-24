# MongoDB Compass Connection Guide for Andah Project

## Connection Details

To connect to the MongoDB database running in Docker using MongoDB Compass:

1. **Connection String Method**:
   ```
   mongodb://mongodb:mongodb@localhost:27017/andah_payments?authSource=admin
   ```

2. **Advanced Connection Method**:
   - **Hostname**: localhost
   - **Port**: 27017
   - **Authentication**: Username/Password
   - **Username**: mongodb
   - **Password**: mongodb
   - **Authentication Database**: admin
   - **Database**: andah_payments

## Troubleshooting Connection Issues

If you still have issues connecting to MongoDB:

1. **Ensure Docker containers are running**:
   ```
   docker-compose ps
   ```

2. **Check MongoDB logs**:
   ```
   docker-compose logs mongodb-payment-db
   ```

3. **Verify network connectivity**:
   ```
   docker exec -it mongodb-payment-db mongosh --host localhost --port 27017 -u mongodb -p mongodb --authenticationDatabase admin
   ```

4. **Restart MongoDB container if needed**:
   ```
   docker-compose restart mongodb-payment-db
   ```

## Collections Overview

The payment service uses the following collections:

1. **paymentTransaction**: Records all payment transactions
2. **cryptoTransaction**: Tracks cryptocurrency payments
3. **cardTransaction**: Stores credit/debit card payments
4. **wallet**: Maintains user wallet balances
5. **walletTransaction**: Tracks all wallet deposits and withdrawals

## Common MongoDB Compass Operations

1. **Viewing Data**: Click on any collection to browse documents
2. **Filtering**: Use the filter bar to query documents (e.g., `{userId: "123"}`)
3. **Aggregation**: Use the Aggregation tab for complex queries
4. **Schema Analysis**: Use the Schema tab to understand document structure
5. **Indexing**: Manage indexes from the Indexes tab

## Security Note

The default credentials are for development only. For production environments:
- Use strong, unique passwords
- Enable TLS/SSL encryption
- Implement IP whitelisting
- Use MongoDB Atlas for managed MongoDB with enhanced security
