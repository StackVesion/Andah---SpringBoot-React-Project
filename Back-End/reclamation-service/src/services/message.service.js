const rabbitmq = require('../config/rabbitmq.config');
const Reclamation = require('../models/reclamation.model');
const fetch = require('node-fetch');

// Initialize message handlers
const initMessageHandlers = async () => {
  try {
    await rabbitmq.initRabbitMQ();
    
    // Handle wallet update notifications from payment-service
    await rabbitmq.consumeMessages(
      rabbitmq.QUEUES.PAYMENT_WALLET_UPDATE,
      handleWalletUpdate
    );
    
    // Handle payment refund results
    await rabbitmq.consumeMessages(
      rabbitmq.QUEUES.PAYMENT_REFUND_RESULT,
      handleRefundResult
    );
    
    // Handle user notifications
    await rabbitmq.consumeMessages(
      rabbitmq.QUEUES.USER_NOTIFICATION,
      handleUserNotification
    );
    
    console.log('Message handlers initialized successfully');
  } catch (error) {
    console.error('Error initializing message handlers:', error);
  }
};

// Handle wallet updates (for payment-related reclamations)
const handleWalletUpdate = async (message) => {
  try {
    const { userId, walletId, balance, transactionId, transactionType } = message;
    
    console.log(`Received wallet update for user ${userId}: Balance: ${balance}`);
    
    // Find any reclamations related to wallet
    if (transactionType === 'REFUND' && transactionId) {
      const reclamation = await Reclamation.findOne({ 
        'response.transactionId': transactionId 
      });
      
      if (reclamation) {
        // Update reclamation status to resolved if it was a refund
        reclamation.status = 'resolved';
        reclamation.response = {
          ...reclamation.response,
          text: `Refund processed. New wallet balance: ${balance}`,
          date: new Date()
        };
        
        await reclamation.save();
        
        // Notify user about successful refund
        await rabbitmq.publishMessage(
          rabbitmq.EXCHANGES.USER,
          rabbitmq.ROUTING_KEYS.USER_NOTIFICATION,
          {
            userId: reclamation.user_id,
            subject: 'Reclamation Resolved - Refund Processed',
            message: `Your reclamation #${reclamation._id} has been resolved with a refund. Your new wallet balance is ${balance}.`,
            type: 'RECLAMATION_RESOLVED'
          }
        );
      }
    }
  } catch (error) {
    console.error('Error handling wallet update:', error);
    throw error;
  }
};

// Handle refund results from payment service
const handleRefundResult = async (message) => {
  try {
    const { reclamationId, success, reason, transactionId, amount } = message;
    
    if (!reclamationId) {
      console.warn('Received refund result without reclamationId, ignoring');
      return;
    }
    
    const reclamation = await Reclamation.findById(reclamationId);
    
    if (!reclamation) {
      console.warn(`Reclamation not found for ID: ${reclamationId}`);
      return;
    }
    
    if (success) {
      reclamation.status = 'resolved';
      reclamation.response = {
        ...reclamation.response,
        text: `Refund of ${amount} processed successfully. Transaction ID: ${transactionId}`,
        date: new Date(),
        transactionId
      };
    } else {
      reclamation.status = 'in-progress';
      reclamation.response = {
        ...reclamation.response,
        text: `Refund failed: ${reason}. The team will contact you to resolve this issue.`,
        date: new Date()
      };
    }
    
    await reclamation.save();
    
    // Notify user about refund status
    await rabbitmq.publishMessage(
      rabbitmq.EXCHANGES.USER,
      rabbitmq.ROUTING_KEYS.USER_NOTIFICATION,
      {
        userId: reclamation.user_id,
        subject: `Reclamation ${success ? 'Resolved' : 'Update'} - Refund ${success ? 'Processed' : 'Failed'}`,
        message: success 
          ? `Your refund of ${amount} has been processed successfully.` 
          : `Your refund could not be processed: ${reason}. Our team will contact you shortly.`,
        type: success ? 'REFUND_SUCCESS' : 'REFUND_FAILED'
      }
    );
  } catch (error) {
    console.error('Error handling refund result:', error);
    throw error;
  }
};

// Handle user notifications
const handleUserNotification = async (message) => {
  try {
    console.log('Received user notification:', message);
    // This service just logs these messages, but in a real implementation
    // you might store them or forward them to a notification service
  } catch (error) {
    console.error('Error handling user notification:', error);
    throw error;
  }
};

// Request a refund for a valid payment reclamation
const requestRefundForReclamation = async (reclamation, amount, reason) => {
  try {
    if (reclamation.category !== 'payment') {
      throw new Error('Refund can only be processed for payment-related reclamations');
    }
    
    if (!reclamation.user_id) {
      throw new Error('User ID is required for refund processing');
    }
    
    // Send a refund request to payment service via RabbitMQ
    await rabbitmq.publishMessage(
      rabbitmq.EXCHANGES.PAYMENT,
      rabbitmq.ROUTING_KEYS.PAYMENT_REFUND_REQUEST,
      {
        reclamationId: reclamation._id.toString(),
        userId: reclamation.user_id,
        amount,
        reason,
        requestedBy: reclamation.response?.admin_id || 'system',
        refundToWallet: true  // Use wallet system for refund
      }
    );
    
    // Update reclamation to indicate refund is in progress
    reclamation.status = 'in-progress';
    reclamation.response = {
      ...reclamation.response,
      text: `Refund request of ${amount} initiated. Processing...`,
      date: new Date()
    };
    
    await reclamation.save();
    
    return {
      success: true,
      message: 'Refund request sent to payment service',
      reclamation
    };
  } catch (error) {
    console.error('Error requesting refund:', error);
    throw error;
  }
};

// Notify about a new reclamation
const notifyNewReclamation = async (reclamation) => {
  try {
    // Publish message to reclamation exchange
    await rabbitmq.publishMessage(
      rabbitmq.EXCHANGES.RECLAMATION,
      rabbitmq.ROUTING_KEYS.RECLAMATION_CREATED,
      {
        reclamationId: reclamation._id.toString(),
        userId: reclamation.user_id,
        title: reclamation.title,
        category: reclamation.category,
        priority: reclamation.priority,
        date: reclamation.date
      }
    );
    
    // If it's a payment-related reclamation, also notify payment service
    if (reclamation.category === 'payment') {
      await rabbitmq.publishMessage(
        rabbitmq.EXCHANGES.PAYMENT,
        rabbitmq.ROUTING_KEYS.PAYMENT_WALLET_UPDATE,
        {
          type: 'RECLAMATION_CREATED',
          userId: reclamation.user_id,
          reclamationId: reclamation._id.toString(),
          category: reclamation.category
        }
      );
    }
    
    return { success: true };
  } catch (error) {
    console.error('Error notifying about new reclamation:', error);
    return { success: false, error: error.message };
  }
};

// Notify about updates to a reclamation
const notifyReclamationUpdate = async (reclamation, updateType = 'updated') => {
  try {
    const routingKey = updateType === 'resolved' 
      ? rabbitmq.ROUTING_KEYS.RECLAMATION_RESOLVED 
      : rabbitmq.ROUTING_KEYS.RECLAMATION_UPDATED;
    
    await rabbitmq.publishMessage(
      rabbitmq.EXCHANGES.RECLAMATION,
      routingKey,
      {
        reclamationId: reclamation._id.toString(),
        userId: reclamation.user_id,
        title: reclamation.title,
        status: reclamation.status,
        response: reclamation.response,
        updatedAt: new Date()
      }
    );
    
    return { success: true };
  } catch (error) {
    console.error(`Error notifying about reclamation ${updateType}:`, error);
    return { success: false, error: error.message };
  }
};

// Get user wallet information via API call to user service
const getUserWalletInfo = async (userId) => {
  try {
    const response = await fetch(`http://user-service:8083/api/users/${userId}/wallet`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`Failed to get wallet info: ${response.status} ${response.statusText}`);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching user wallet info:', error);
    return null;
  }
};

module.exports = {
  initMessageHandlers,
  requestRefundForReclamation,
  notifyNewReclamation,
  notifyReclamationUpdate,
  getUserWalletInfo
};
