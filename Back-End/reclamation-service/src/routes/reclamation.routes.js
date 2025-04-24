const express = require('express');
const router = express.Router();
const reclamationController = require('../controllers/reclamation.controller');

// Create a new reclamation
router.post('/', reclamationController.create);

// Get all reclamations (with pagination and filters)
router.get('/', reclamationController.findAll);

// Get reclamation statistics (for admin dashboard)
router.get('/stats/overview', reclamationController.getStats);

// Process a refund for a payment-related reclamation
router.post('/:id/refund', reclamationController.processRefund);

// Get all reclamations for a specific user
router.get('/user/:userId', reclamationController.getUserReclamations);

// Get user wallet information
router.get('/user/:userId/wallet', reclamationController.getUserWallet);

// Get a specific reclamation by ID
router.get('/:id', reclamationController.findOne);

// Update a reclamation
router.put('/:id', reclamationController.update);

// Delete a reclamation
router.delete('/:id', reclamationController.delete);

module.exports = router;
