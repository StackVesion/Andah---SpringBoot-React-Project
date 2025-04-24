const amqp = require('amqplib');

// RabbitMQ connection details
const host = process.env.RABBITMQ_HOST || 'localhost';
const port = process.env.RABBITMQ_PORT || 5672;
const user = process.env.RABBITMQ_USER || 'andah';
const password = process.env.RABBITMQ_PASSWORD || 'andah123';
const vhost = process.env.RABBITMQ_VHOST || '/';

// Connection URL
const url = `amqp://${user}:${password}@${host}:${port}${vhost}`;

// Exchange names
const EXCHANGES = {
  RECLAMATION: 'reclamation.exchange',
  PAYMENT: 'payment.exchange',
  USER: 'user.exchange',
  NOTIFICATION: 'notification.exchange'
};

// Queue names
const QUEUES = {
  // Reclamation service queues
  RECLAMATION_CREATED: 'reclamation.created.queue',
  RECLAMATION_UPDATED: 'reclamation.updated.queue',
  RECLAMATION_RESOLVED: 'reclamation.resolved.queue',
  
  // Payment-related queues
  PAYMENT_WALLET_UPDATE: 'payment.wallet.update.queue',
  PAYMENT_REFUND_REQUEST: 'payment.refund.request.queue',
  PAYMENT_REFUND_RESULT: 'payment.refund.result.queue',
  
  // User-related queues
  USER_NOTIFICATION: 'user.notification.queue'
};

// Routing keys
const ROUTING_KEYS = {
  RECLAMATION_CREATED: 'reclamation.created',
  RECLAMATION_UPDATED: 'reclamation.updated',
  RECLAMATION_RESOLVED: 'reclamation.resolved',
  
  PAYMENT_WALLET_UPDATE: 'payment.wallet.update',
  PAYMENT_REFUND_REQUEST: 'payment.refund.request',
  PAYMENT_REFUND_RESULT: 'payment.refund.result',
  
  USER_NOTIFICATION: 'user.notification'
};

// Connection to RabbitMQ
let connection = null;
let channel = null;

// Initialize RabbitMQ connection
const initRabbitMQ = async () => {
  try {
    // Create connection
    connection = await amqp.connect(url);
    console.log('Connected to RabbitMQ');
    
    // Create channel
    channel = await connection.createChannel();
    console.log('RabbitMQ channel created');
    
    // Setup exchanges
    await Promise.all([
      channel.assertExchange(EXCHANGES.RECLAMATION, 'topic', { durable: true }),
      channel.assertExchange(EXCHANGES.PAYMENT, 'topic', { durable: true }),
      channel.assertExchange(EXCHANGES.USER, 'topic', { durable: true }),
      channel.assertExchange(EXCHANGES.NOTIFICATION, 'topic', { durable: true })
    ]);
    console.log('RabbitMQ exchanges created');
    
    // Setup queues and bindings
    await setupQueuesAndBindings();
    
    // Handle connection errors and reconnection
    connection.on('error', (err) => {
      console.error('RabbitMQ connection error:', err);
      setTimeout(initRabbitMQ, 5000);
    });
    
    connection.on('close', () => {
      console.warn('RabbitMQ connection closed. Trying to reconnect...');
      setTimeout(initRabbitMQ, 5000);
    });
    
    return { connection, channel };
  } catch (error) {
    console.error('Failed to connect to RabbitMQ:', error);
    setTimeout(initRabbitMQ, 5000);
  }
};

// Setup queues and bindings
const setupQueuesAndBindings = async () => {
  try {
    // Reclamation service queues
    await channel.assertQueue(QUEUES.RECLAMATION_CREATED, { durable: true });
    await channel.assertQueue(QUEUES.RECLAMATION_UPDATED, { durable: true });
    await channel.assertQueue(QUEUES.RECLAMATION_RESOLVED, { durable: true });
    
    // Payment-related queues (for wallet integration)
    await channel.assertQueue(QUEUES.PAYMENT_WALLET_UPDATE, { durable: true });
    await channel.assertQueue(QUEUES.PAYMENT_REFUND_REQUEST, { durable: true });
    await channel.assertQueue(QUEUES.PAYMENT_REFUND_RESULT, { durable: true });
    
    // User notification queue
    await channel.assertQueue(QUEUES.USER_NOTIFICATION, { durable: true });
    
    // Binding queues to exchanges
    await channel.bindQueue(
      QUEUES.RECLAMATION_CREATED,
      EXCHANGES.RECLAMATION,
      ROUTING_KEYS.RECLAMATION_CREATED
    );
    
    await channel.bindQueue(
      QUEUES.RECLAMATION_UPDATED,
      EXCHANGES.RECLAMATION,
      ROUTING_KEYS.RECLAMATION_UPDATED
    );
    
    await channel.bindQueue(
      QUEUES.RECLAMATION_RESOLVED,
      EXCHANGES.RECLAMATION,
      ROUTING_KEYS.RECLAMATION_RESOLVED
    );
    
    await channel.bindQueue(
      QUEUES.PAYMENT_WALLET_UPDATE,
      EXCHANGES.PAYMENT,
      ROUTING_KEYS.PAYMENT_WALLET_UPDATE
    );
    
    await channel.bindQueue(
      QUEUES.PAYMENT_REFUND_REQUEST,
      EXCHANGES.PAYMENT,
      ROUTING_KEYS.PAYMENT_REFUND_REQUEST
    );
    
    await channel.bindQueue(
      QUEUES.PAYMENT_REFUND_RESULT,
      EXCHANGES.PAYMENT,
      ROUTING_KEYS.PAYMENT_REFUND_RESULT
    );
    
    await channel.bindQueue(
      QUEUES.USER_NOTIFICATION,
      EXCHANGES.USER,
      ROUTING_KEYS.USER_NOTIFICATION
    );
    
    console.log('RabbitMQ queues and bindings set up successfully');
  } catch (error) {
    console.error('Error setting up RabbitMQ queues and bindings:', error);
    throw error;
  }
};

// Publish a message to an exchange with a routing key
const publishMessage = async (exchange, routingKey, message) => {
  try {
    if (!channel) {
      await initRabbitMQ();
    }
    
    const messageBuffer = Buffer.from(JSON.stringify(message));
    const result = channel.publish(exchange, routingKey, messageBuffer, {
      persistent: true,
      contentType: 'application/json'
    });
    
    return result;
  } catch (error) {
    console.error(`Error publishing message to exchange ${exchange} with routing key ${routingKey}:`, error);
    throw error;
  }
};

// Consume messages from a queue
const consumeMessages = async (queue, callback) => {
  try {
    if (!channel) {
      await initRabbitMQ();
    }
    
    await channel.consume(queue, async (message) => {
      if (message) {
        try {
          const content = JSON.parse(message.content.toString());
          await callback(content);
          channel.ack(message);
        } catch (error) {
          console.error(`Error processing message from queue ${queue}:`, error);
          // Negative acknowledge after 5 seconds to retry
          setTimeout(() => channel.nack(message), 5000);
        }
      }
    });
    
    console.log(`Consumer set up for queue: ${queue}`);
  } catch (error) {
    console.error(`Error consuming messages from queue ${queue}:`, error);
    throw error;
  }
};

// Close connection
const closeConnection = async () => {
  try {
    if (channel) {
      await channel.close();
    }
    if (connection) {
      await connection.close();
    }
    console.log('RabbitMQ connection closed');
  } catch (error) {
    console.error('Error closing RabbitMQ connection:', error);
  }
};

// Handle graceful shutdown
process.on('SIGINT', async () => {
  await closeConnection();
  process.exit(0);
});

process.on('SIGTERM', async () => {
  await closeConnection();
  process.exit(0);
});

// Export functions and constants
module.exports = {
  initRabbitMQ,
  publishMessage,
  consumeMessages,
  closeConnection,
  EXCHANGES,
  QUEUES,
  ROUTING_KEYS
};
