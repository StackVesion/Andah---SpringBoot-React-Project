# Config Server

## Overview
The Config Server provides centralized configuration management for all microservices within the Andah Scooter Rental Platform. It stores configuration properties in a version-controlled Git repository and serves them to the microservices at runtime.

## Features
- Centralized configuration management
- Environment-specific configuration (dev, test, prod)
- Configuration versioning with Git
- Runtime property refreshing without service restart
- Configuration security with encryption
- Support for configuration profiles

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Cloud Config Server**: Configuration server implementation
- **Spring Cloud Netflix Eureka Client**: Service discovery
- **Git**: Configuration storage

## Configuration
The Config Server itself has configuration properties in its `application.properties` file:

```properties
spring.application.name=config-server
server.port=8888

# Git backend repository for configuration storage
spring.cloud.config.server.git.uri=https://github.com/andah-team/config-repo
spring.cloud.config.server.git.default-label=main
spring.cloud.config.server.git.clone-on-start=true
# For local file system use instead:
# spring.cloud.config.server.native.search-locations=file:./config-repo

# Eureka Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

## Repository Structure
The Git repository should have the following structure:
```
config-repo/
├── application.properties       # Shared properties for all services
├── application-dev.properties   # Shared properties for dev environment
├── application-prod.properties  # Shared properties for prod environment
├── user-service.properties      # Service-specific properties
├── station-service.properties
├── scooter-service.properties
├── reservation-service.properties
├── payment-service.properties
├── rating-service.properties
└── api-gateway.properties
```

## Running the Service
1. Start the Eureka Server (if using Eureka)
2. Run the Config Server:
   ```bash
   mvn spring-boot:run
   ```
3. Ensure it's running before starting other microservices

## Security
For production environments, sensitive properties should be encrypted. The Config Server supports encryption and decryption of property values using symmetric or asymmetric keys.

## Accessing Configuration
Configurations can be accessed via REST endpoints:
- `GET /{application}/{profile}` - Retrieve properties for a specific application and profile
- `GET /{application}/{profile}/{label}` - Retrieve properties for a specific branch/version

For example:
```
http://localhost:8888/user-service/dev
http://localhost:8888/payment-service/prod
```

## Client Configuration
Client services need to include the Spring Cloud Config Client dependency and configure the Config Server URL:

```properties
spring.config.import=optional:configserver:http://localhost:8888
```
