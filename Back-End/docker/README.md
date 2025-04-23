# Docker Configuration

## Overview
This directory contains Docker configurations for deploying the entire Andah Scooter Rental Platform as a set of containerized services. The Docker setup enables consistent deployment across different environments and simplifies the development and testing process.

## Components
The Docker configuration includes:

1. **Microservice Containers**:
   - User Service
   - Station Service
   - Scooter Service
   - Reservation Service
   - Payment Service
   - Rating Service
   - API Gateway
   - Config Server
   - Eureka Server

2. **Database Containers**:
   - PostgreSQL (for User, Station, Scooter, and Rating services)
   - MySQL (for Reservation service)
   - MongoDB (for Payment service)

3. **Supporting Services**:
   - Keycloak (for authentication and authorization)
   - Prometheus (for monitoring)
   - Grafana (for visualization)

## Docker Compose Configuration
The `docker-compose.yml` file defines the complete stack, including service dependencies, network configuration, volume mounts, and environment variables.

Key features:
- Isolated networks for service-to-service communication
- Persistent volumes for database storage
- Health checks for service readiness
- Environment variable configuration
- Exposed ports for external access

## Usage Instructions

### Starting the Entire Stack
```bash
docker-compose up -d
```

### Starting Only Infrastructure Services
```bash
docker-compose up -d eureka-server config-server keycloak postgres-user-db postgres-station-db postgres-scooter-db mysql-reservation-db mongodb-payment-db postgres-rating-db
```

### Starting Individual Services
```bash
docker-compose up -d user-service
docker-compose up -d station-service
# etc.
```

### Checking Service Logs
```bash
docker-compose logs -f [service-name]
```

### Stopping Services
```bash
docker-compose down
```

### Cleaning Up (removes volumes)
```bash
docker-compose down -v
```

## Environment Variables
The Docker environment variables for each service are defined in the `docker-compose.yml` file. These include:
- Database connection strings
- Service discovery URLs
- Configuration server URLs
- Application settings

## Development Workflow
For development, you can:
1. Run infrastructure services using Docker
2. Run microservices directly from your IDE
3. Update the Docker image for a specific service:
   ```bash
   docker-compose build [service-name]
   docker-compose up -d [service-name]
   ```

## Production Considerations
For production deployment:
1. Use separate Docker Compose files for different environments
2. Configure appropriate CPU and memory limits
3. Set up proper logging and monitoring
4. Use secrets management for sensitive information
5. Enable TLS for secure communication
