#!/bin/bash

# Stop the relevant services
echo "Stopping services..."
docker-compose stop api-gateway user-service payment-service reclamation-service

# Rebuild the services with our changes
echo "Rebuilding services..."
docker-compose build api-gateway user-service payment-service reclamation-service

# Start the services again
echo "Starting services..."
docker-compose up -d api-gateway user-service payment-service reclamation-service

echo "Waiting for services to become available..."
sleep 30

echo "Services restarted successfully. You can now test the API endpoints."
