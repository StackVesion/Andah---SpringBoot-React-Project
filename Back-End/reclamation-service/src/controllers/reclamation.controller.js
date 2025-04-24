const Reclamation = require('../models/reclamation.model');
const messageService = require('../services/message.service');

// Create a new reclamation
exports.create = async (req, res) => {
  try {
    const { title, description, user_id, category, priority } = req.body;
    
    // Validate required fields
    if (!title || !description || !user_id) {
      return res.status(400).json({
        success: false,
        message: 'Please provide title, description, and user_id'
      });
    }
    
    // Create new reclamation
    const reclamation = new Reclamation({
      title,
      description,
      user_id,
      category: category || 'other',
      priority: priority || 'medium',
      date: new Date()
    });
    
    // Save reclamation to database
    const savedReclamation = await reclamation.save();
    
    // Notify other services about the new reclamation using RabbitMQ
    await messageService.notifyNewReclamation(savedReclamation);
    
    return res.status(201).json({
      success: true,
      data: savedReclamation,
      message: 'Reclamation created successfully'
    });
  } catch (error) {
    console.error('Error creating reclamation:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to create reclamation',
      error: error.message
    });
  }
};

// Get all reclamations (with pagination)
exports.findAll = async (req, res) => {
  try {
    const { page = 1, limit = 10, status, user_id, category, priority, sort = 'date' } = req.query;
    
    // Build query filters
    const query = {};
    if (status) query.status = status;
    if (user_id) query.user_id = user_id;
    if (category) query.category = category;
    if (priority) query.priority = priority;
    
    // Build sort options
    const sortOptions = {};
    sortOptions[sort] = sort === 'date' ? -1 : 1; // Default sort by date descending
    
    // Execute query with pagination
    const reclamations = await Reclamation.find(query)
      .sort(sortOptions)
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit))
      .exec();
    
    // Get total count for pagination
    const count = await Reclamation.countDocuments(query);
    
    return res.status(200).json({
      success: true,
      data: reclamations,
      pagination: {
        total: count,
        page: parseInt(page),
        pages: Math.ceil(count / parseInt(limit)),
        limit: parseInt(limit)
      }
    });
  } catch (error) {
    console.error('Error fetching reclamations:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch reclamations',
      error: error.message
    });
  }
};

// Get a single reclamation by ID
exports.findOne = async (req, res) => {
  try {
    const { id } = req.params;
    
    const reclamation = await Reclamation.findById(id);
    
    if (!reclamation) {
      return res.status(404).json({
        success: false,
        message: `Reclamation with id ${id} not found`
      });
    }
    
    return res.status(200).json({
      success: true,
      data: reclamation
    });
  } catch (error) {
    console.error(`Error fetching reclamation with id ${req.params.id}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch reclamation',
      error: error.message
    });
  }
};

// Update a reclamation
exports.update = async (req, res) => {
  try {
    const { id } = req.params;
    const { title, description, status, category, priority, response } = req.body;
    
    // Find reclamation first to check if it exists
    const reclamation = await Reclamation.findById(id);
    
    if (!reclamation) {
      return res.status(404).json({
        success: false,
        message: `Reclamation with id ${id} not found`
      });
    }
    
    // Track previous status for notification
    const previousStatus = reclamation.status;
    
    // Update fields
    if (title) reclamation.title = title;
    if (description) reclamation.description = description;
    if (status) reclamation.status = status;
    if (category) reclamation.category = category;
    if (priority) reclamation.priority = priority;
    
    // Handle response separately (for admin responses)
    if (response && response.text) {
      reclamation.response = {
        text: response.text,
        date: new Date(),
        admin_id: response.admin_id
      };
      
      // If responding, update status to in-progress if it was pending
      if (reclamation.status === 'pending') {
        reclamation.status = 'in-progress';
      }
    }
    
    // Save updated reclamation
    const updatedReclamation = await reclamation.save();
    
    // Notify other services about the update
    if (reclamation.status === 'resolved' && previousStatus !== 'resolved') {
      await messageService.notifyReclamationUpdate(updatedReclamation, 'resolved');
    } else {
      await messageService.notifyReclamationUpdate(updatedReclamation, 'updated');
    }
    
    return res.status(200).json({
      success: true,
      data: updatedReclamation,
      message: 'Reclamation updated successfully'
    });
  } catch (error) {
    console.error(`Error updating reclamation with id ${req.params.id}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to update reclamation',
      error: error.message
    });
  }
};

// Delete a reclamation
exports.delete = async (req, res) => {
  try {
    const { id } = req.params;
    
    const reclamation = await Reclamation.findByIdAndDelete(id);
    
    if (!reclamation) {
      return res.status(404).json({
        success: false,
        message: `Reclamation with id ${id} not found`
      });
    }
    
    return res.status(200).json({
      success: true,
      message: 'Reclamation deleted successfully'
    });
  } catch (error) {
    console.error(`Error deleting reclamation with id ${req.params.id}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to delete reclamation',
      error: error.message
    });
  }
};

// Get user reclamations
exports.getUserReclamations = async (req, res) => {
  try {
    const { userId } = req.params;
    const { page = 1, limit = 10, status } = req.query;
    
    // Build query filters
    const query = { user_id: userId };
    if (status) query.status = status;
    
    // Execute query with pagination
    const reclamations = await Reclamation.find(query)
      .sort({ date: -1 })
      .limit(parseInt(limit))
      .skip((parseInt(page) - 1) * parseInt(limit))
      .exec();
    
    // Get total count for pagination
    const count = await Reclamation.countDocuments(query);
    
    return res.status(200).json({
      success: true,
      data: reclamations,
      pagination: {
        total: count,
        page: parseInt(page),
        pages: Math.ceil(count / parseInt(limit)),
        limit: parseInt(limit)
      }
    });
  } catch (error) {
    console.error(`Error fetching reclamations for user ${req.params.userId}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch user reclamations',
      error: error.message
    });
  }
};

// Get reclamation statistics
exports.getStats = async (req, res) => {
  try {
    // Count by status
    const statusStats = await Reclamation.aggregate([
      { $group: { _id: '$status', count: { $sum: 1 } } }
    ]);
    
    // Count by category
    const categoryStats = await Reclamation.aggregate([
      { $group: { _id: '$category', count: { $sum: 1 } } }
    ]);
    
    // Count by priority
    const priorityStats = await Reclamation.aggregate([
      { $group: { _id: '$priority', count: { $sum: 1 } } }
    ]);
    
    // Recent activity (last 30 days)
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    
    const recentActivity = await Reclamation.aggregate([
      { 
        $match: { 
          createdAt: { $gte: thirtyDaysAgo } 
        } 
      },
      {
        $group: {
          _id: { 
            year: { $year: '$createdAt' },
            month: { $month: '$createdAt' },
            day: { $dayOfMonth: '$createdAt' }
          },
          count: { $sum: 1 }
        }
      },
      { $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 } }
    ]);
    
    return res.status(200).json({
      success: true,
      data: {
        statusStats,
        categoryStats,
        priorityStats,
        recentActivity
      }
    });
  } catch (error) {
    console.error('Error fetching reclamation statistics:', error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch reclamation statistics',
      error: error.message
    });
  }
};

// Process a refund for a payment-related reclamation
exports.processRefund = async (req, res) => {
  try {
    const { id } = req.params;
    const { amount, reason } = req.body;
    
    if (!amount || !reason) {
      return res.status(400).json({
        success: false,
        message: 'Please provide amount and reason for the refund'
      });
    }
    
    // Find reclamation
    const reclamation = await Reclamation.findById(id);
    
    if (!reclamation) {
      return res.status(404).json({
        success: false,
        message: `Reclamation with id ${id} not found`
      });
    }
    
    // Check if it's a payment-related reclamation
    if (reclamation.category !== 'payment') {
      return res.status(400).json({
        success: false,
        message: 'Refunds can only be processed for payment-related reclamations'
      });
    }
    
    // Request refund via message service
    const result = await messageService.requestRefundForReclamation(reclamation, amount, reason);
    
    return res.status(200).json({
      success: true,
      message: 'Refund request has been initiated',
      data: result.reclamation
    });
  } catch (error) {
    console.error(`Error processing refund for reclamation ${req.params.id}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to process refund',
      error: error.message
    });
  }
};

// Get user wallet information via message service
exports.getUserWallet = async (req, res) => {
  try {
    const { userId } = req.params;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: 'User ID is required'
      });
    }
    
    // Get wallet info via message service
    const walletInfo = await messageService.getUserWalletInfo(userId);
    
    if (!walletInfo) {
      return res.status(404).json({
        success: false,
        message: 'Wallet information not found or error fetching wallet data'
      });
    }
    
    return res.status(200).json({
      success: true,
      data: walletInfo
    });
  } catch (error) {
    console.error(`Error fetching wallet info for user ${req.params.userId}:`, error);
    return res.status(500).json({
      success: false,
      message: 'Failed to fetch wallet information',
      error: error.message
    });
  }
};
