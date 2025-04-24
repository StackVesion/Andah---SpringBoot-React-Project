const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const bodyParser = require('body-parser');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

// Import configs
const connectDB = require('./src/config/db.config');
const setupEurekaClient = require('./src/config/eureka.config');

// Import routes
const reclamationRoutes = require('./src/routes/reclamation.routes');
const healthRoutes = require('./src/routes/health.routes');
const publicRoutes = require('./src/routes/public.routes');

// Import services
const messageService = require('./src/services/message.service');

// Initialize Express app
const app = express();

// Setup middleware
app.use(cors());
app.use(morgan('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Register with Eureka
const eurekaClient = setupEurekaClient(app);

// API routes
app.use('/api/reclamations', reclamationRoutes);
app.use('/api/reclamations/public', publicRoutes);
app.use('/health', healthRoutes);

// Root route
app.get('/', (req, res) => {
  res.json({
    message: 'Welcome to Andah Reclamation Service',
    service: 'reclamation-service',
    version: process.env.VERSION || '1.0.0',
    status: 'running'
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    success: false,
    message: 'Internal Server Error',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// Start server
const PORT = process.env.PORT || 3001;

// Connect to MongoDB first, then start server and initialize message handlers
connectDB()
  .then(async () => {
    // Start the server
    app.listen(PORT, () => {
      console.log(`⚡️ Reclamation service running on port ${PORT}`);
    });
    
    // Initialize RabbitMQ message handlers
    try {
      await messageService.initMessageHandlers();
      console.log('RabbitMQ message handlers initialized successfully');
    } catch (error) {
      console.error('Error initializing RabbitMQ message handlers:', error);
      console.log('Service will continue running, but RabbitMQ messaging may not work correctly');
    }
  })
  .catch(err => {
    console.error('Failed to start server:', err);
    process.exit(1);
  });

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('Unhandled Promise Rejection:', err);
  // Close server & exit process
  process.exit(1);
});
