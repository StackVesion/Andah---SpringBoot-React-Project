# API Gateway

## Overview
The API Gateway acts as the single entry point for all client requests to the Andah Scooter Rental Platform. It routes requests to appropriate microservices, handles cross-cutting concerns such as authentication and rate limiting, and provides a unified API interface for frontend applications.

## Features
- Request routing to appropriate microservices
- Load balancing
- API composition
- Authentication and authorization
- Request/response transformation
- Rate limiting and circuit breaking
- Logging and monitoring

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Cloud Gateway**: API Gateway implementation
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Security**: Authentication and authorization

## Configuration
The API Gateway is configured to route requests to the appropriate microservices based on path patterns:

```properties
spring.application.name=api-gateway
server.port=8090

# Eureka Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Config Server
spring.config.import=optional:configserver:http://localhost:8888

# Routes Configuration
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**, /api/auth/**, /api/applications/**

spring.cloud.gateway.routes[1].id=station-service
spring.cloud.gateway.routes[1].uri=lb://station-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/stations/**

spring.cloud.gateway.routes[2].id=scooter-service
spring.cloud.gateway.routes[2].uri=lb://scooter-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/scooters/**

spring.cloud.gateway.routes[3].id=reservation-service
spring.cloud.gateway.routes[3].uri=lb://reservation-service
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/reservations/**

spring.cloud.gateway.routes[4].id=payment-service
spring.cloud.gateway.routes[4].uri=lb://payment-service
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/payments/**

spring.cloud.gateway.routes[5].id=rating-service
spring.cloud.gateway.routes[5].uri=lb://rating-service
spring.cloud.gateway.routes[5].predicates[0]=Path=/api/ratings/**
```

## Security Configuration
The API Gateway handles authentication and authorization for all requests. It validates JWT tokens and ensures that clients have appropriate permissions to access endpoints.

## Running the Service
1. Start Eureka Server and Config Server
2. Run the Gateway:
   ```bash
   mvn spring-boot:run
   ```

## Dependencies
The API Gateway interacts with:
- All microservices in the system for routing requests
- **Config Server**: For configuration properties
- **Eureka Server**: For service discovery
- **Keycloak**: For authentication and authorization (if applicable)

## API Documentation
The Gateway can be configured to expose a Swagger/OpenAPI documentation endpoint that aggregates API documentation from all microservices.

## Monitoring
The Gateway includes:
- Actuator endpoints for health monitoring
- Request/response logging
- Performance metrics collection
