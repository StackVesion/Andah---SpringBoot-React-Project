# scooter Service

## Overview
The scooter Service manages all scooter-related functionality within the Andah Scooter Rental Platform. It handles scooter creation, management, and retrieval, as well as the association of scooters with their owners.

## Features
- scooter registration and management
- scooter owner association
- scooter location and details management
- scooter status tracking
- scooter search by location and other criteria

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Data JPA**: Database access
- **PostgreSQL**: Database
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Cloud OpenFeign**: Inter-service communication

## Database Schema
The service uses PostgreSQL with the following main entities:
- `scooter`: Contains scooter information including name, location, capacity, and owner ID
- `scooterAddress`: Detailed address information for scooters

## API Endpoints

### scooter Management
- `GET /api/scooters` - Get all scooters
- `GET /api/scooters/{id}` - Get specific scooter details
- `GET /api/scooters/owner/{ownerId}` - Get scooters by owner
- `POST /api/scooters/owner/{ownerId}` - Create new scooter
- `PUT /api/scooters/{id}` - Update scooter details
- `DELETE /api/scooters/{id}` - Remove a scooter
- `GET /api/scooters/nearby` - Find nearby scooters by coordinates

### scooter Status
- `PUT /api/scooters/{id}/status` - Update scooter status (active/inactive)
- `GET /api/scooters/status/{status}` - Get scooters by status

## Configuration
Key application properties include:
```properties
spring.application.name=scooter-service
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5432/andah_scooters
spring.jpa.hibernate.ddl-auto=update
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.config.import=optional:configserver:http://localhost:8888
```

## Running the Service
1. Ensure PostgreSQL is running
2. Start Eureka Server and Config Server
3. Run the service:
   ```bash
   mvn spring-boot:run
   ```

## Dependencies
This service communicates with:
- **User Service**: To validate scooter owner information
- **Scooter Service**: For managing scooters at each scooter
- **Rating Service**: For scooter ratings
- **API Gateway**: For routing requests
- **Config Server**: For configuration properties
- **Eureka Server**: For service registration and discovery
