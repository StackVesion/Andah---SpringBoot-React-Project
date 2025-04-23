# Station Service

## Overview
The Station Service manages all station-related functionality within the Andah Scooter Rental Platform. It handles station creation, management, and retrieval, as well as the association of stations with their owners.

## Features
- Station registration and management
- Station owner association
- Station location and details management
- Station status tracking
- Station search by location and other criteria

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Data JPA**: Database access
- **PostgreSQL**: Database
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Cloud OpenFeign**: Inter-service communication

## Database Schema
The service uses PostgreSQL with the following main entities:
- `Station`: Contains station information including name, location, capacity, and owner ID
- `StationAddress`: Detailed address information for stations

## API Endpoints

### Station Management
- `GET /api/stations` - Get all stations
- `GET /api/stations/{id}` - Get specific station details
- `GET /api/stations/owner/{ownerId}` - Get stations by owner
- `POST /api/stations/owner/{ownerId}` - Create new station
- `PUT /api/stations/{id}` - Update station details
- `DELETE /api/stations/{id}` - Remove a station
- `GET /api/stations/nearby` - Find nearby stations by coordinates

### Station Status
- `PUT /api/stations/{id}/status` - Update station status (active/inactive)
- `GET /api/stations/status/{status}` - Get stations by status

## Configuration
Key application properties include:
```properties
spring.application.name=station-service
server.port=8082
spring.datasource.url=jdbc:postgresql://localhost:5432/andah_stations
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
- **User Service**: To validate station owner information
- **Scooter Service**: For managing scooters at each station
- **Rating Service**: For station ratings
- **API Gateway**: For routing requests
- **Config Server**: For configuration properties
- **Eureka Server**: For service registration and discovery
