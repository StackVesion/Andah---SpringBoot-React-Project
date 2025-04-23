# Eureka Server

## Overview
The Eureka Server provides service discovery and registration for all microservices within the Andah Scooter Rental Platform. It enables the services to locate and communicate with each other without hardcoding hostnames and ports.

## Features
- Service registration and discovery
- Health monitoring of registered services
- Dynamic scaling of service instances
- Self-preservation mode to handle network partitions
- Dashboard UI for monitoring service status

## Tech Stack
- **Spring Boot**: Application framework
- **Spring Cloud Netflix Eureka Server**: Service discovery implementation

## Configuration
The Eureka Server's configuration is defined in its `application.properties` file:

```properties
spring.application.name=eureka-server
server.port=8761

# Eureka Server Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.wait-time-in-ms-when-sync-empty=0
eureka.instance.hostname=localhost

# Dashboard configuration
eureka.dashboard.enabled=true
eureka.dashboard.path=/dashboard

# Security settings (if needed)
# spring.security.user.name=admin
# spring.security.user.password=admin
```

## Client Configuration
Microservices that register with Eureka need to include the following configuration:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

## Running the Service
The Eureka Server should be started before any other microservices:

```bash
mvn spring-boot:run
```

## Dashboard
The Eureka Dashboard is available at `http://localhost:8761` and provides a visual representation of:
- All registered applications
- Status of each instance
- Instance metadata
- Recent registry changes

## Resilience Features
- **Self-preservation mode**: Protects registry during network partitions
- **Health checks**: Automatically detects unhealthy instances
- **Registry synchronization**: Maintains consistency across Eureka clusters

## Production Considerations
For a production environment:
1. Deploy multiple Eureka Server instances for high availability
2. Configure appropriate security measures (HTTPS, authentication)
3. Adjust timeouts and heartbeat intervals based on network characteristics
4. Enable detailed logging for troubleshooting
