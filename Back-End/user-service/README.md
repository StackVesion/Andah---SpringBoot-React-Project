# User Service

## Overview
The User Service is responsible for handling all user-related operations within the Andah Scooter Rental Platform. It manages user authentication, registration, and profile management, as well as station owner applications.

## Features
- User registration and authentication
- User profile management
- Station owner application processing
- Role-based access control (User, StationOwner, Admin)
- Secure password handling with BCrypt encryption

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Data JPA**: Database access
- **Spring Security**: Authentication and authorization
- **PostgreSQL**: Database
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Cloud OpenFeign**: Inter-service communication

## Database Schema
The service uses PostgreSQL with the following main entities:
- `User`: Stores user information including name, email, password, role
- `Application`: Manages station owner applications

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate a user
- `GET /api/auth/me` - Get current authenticated user info

### User Management
- `GET /api/users` - Get all users (admin only)
- `GET /api/users/{id}` - Get specific user
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user (admin only)

### Station Owner Applications
- `POST /api/applications` - Submit station owner application
- `GET /api/applications` - List all applications (admin only)
- `GET /api/applications/user/{userId}` - Get application by user
- `PUT /api/applications/{id}/approve` - Approve application (admin only)
- `PUT /api/applications/{id}/reject` - Reject application (admin only)

## Configuration
Key application properties include:
```properties
spring.application.name=user-service
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/andah_users
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
- **Station Service**: When approving station owner applications
- **API Gateway**: For routing requests
- **Config Server**: For configuration properties
- **Eureka Server**: For service registration and discovery
