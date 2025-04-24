const Eureka = require('eureka-js-client').Eureka;
const os = require('os');

// Get local IP address for registration
const getLocalIP = () => {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
};

// Create Eureka client
const setupEurekaClient = (app) => {
  const port = process.env.PORT || 3000;
  const hostName = process.env.HOSTNAME || getLocalIP();
  const serviceName = 'reclamation-service';
  
  // Eureka client configuration
  const client = new Eureka({
    instance: {
      app: serviceName,
      hostName: hostName,
      ipAddr: hostName,
      statusPageUrl: `http://${hostName}:${port}/info`,
      healthCheckUrl: `http://${hostName}:${port}/health`,
      port: {
        '$': port,
        '@enabled': true
      },
      vipAddress: serviceName,
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn'
      },
      registerWithEureka: true,
      fetchRegistry: true
    },
    eureka: {
      host: process.env.EUREKA_HOST || 'localhost',
      port: process.env.EUREKA_PORT || 8761,
      servicePath: '/eureka/apps/'
    }
  });

  // Start Eureka client
  client.start();

  // Handle shutdown gracefully
  process.on('SIGINT', () => {
    client.stop();
    process.exit();
  });

  // Add health and info endpoints for Eureka
  app.get('/health', (req, res) => {
    res.json({ status: 'UP' });
  });

  app.get('/info', (req, res) => {
    res.json({
      service: serviceName,
      version: process.env.VERSION || '1.0.0',
      description: 'Reclamation management service for Andah platform'
    });
  });

  // Return client for testing/reference
  return client;
};

module.exports = setupEurekaClient;
