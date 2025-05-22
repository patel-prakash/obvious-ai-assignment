# Service Registry (Eureka Server)

This module implements a Service Registry using Netflix Eureka for the e-commerce microservices architecture.

## Purpose

The Service Registry is a central hub that allows microservices to:
- Register themselves as available services
- Discover and look up other services by their application name rather than hard-coded URLs
- Dynamically scale up and down without changing configurations

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- Spring Cloud Netflix Eureka Server

## Configuration

The service registry runs on port 8761 (default Eureka port) and is configured not to register itself as a client.

## Building and Running

```bash
# Build the service
./mvnw clean package

# Run the service
./mvnw spring-boot:run
```

## Accessing the Dashboard

Once running, the Eureka dashboard is available at:
```
http://localhost:8761
```

This dashboard shows all registered services and their status. 