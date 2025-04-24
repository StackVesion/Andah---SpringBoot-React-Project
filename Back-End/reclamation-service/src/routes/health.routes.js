const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');

// Health check endpoint for Eureka
router.get('/', (req, res) => {
  // Check MongoDB connection
  const mongoStatus = mongoose.connection.readyState === 1 ? 'UP' : 'DOWN';
  
  // Return health status
  res.json({
    status: 'UP',
    timestamp: new Date().toISOString(),
    components: {
      mongodb: {
        status: mongoStatus
      },
      service: {
        status: 'UP'
      },
      eureka: {
        status: 'UP'
      }
    }
  });
});

// Info endpoint for Eureka
router.get('/info', (req, res) => {
  res.json({
    service: 'reclamation-service',
    version: process.env.VERSION || '1.0.0',
    description: 'Reclamation management service for Andah platform',
    endpoints: [
      {
        path: '/api/reclamations',
        methods: ['GET', 'POST'],
        description: 'Get all reclamations or create a new one'
      },
      {
        path: '/api/reclamations/:id',
        methods: ['GET', 'PUT', 'DELETE'],
        description: 'Get, update or delete a specific reclamation'
      },
      {
        path: '/api/reclamations/user/:userId',
        methods: ['GET'],
        description: 'Get all reclamations for a specific user'
      },
      {
        path: '/api/reclamations/stats/overview',
        methods: ['GET'],
        description: 'Get reclamation statistics'
      },
      {
        path: '/api/reclamations/public/faqs',
        methods: ['GET'],
        description: 'Get public FAQs about reclamations (no auth required)'
      }
    ]
  });
});

module.exports = router;
