# E-commerce Microservices Project

This project implements a scalable, resilient, and secure microservices architecture for an e-commerce platform, focusing on inventory management and payment processing.

## Project Overview

The system consists of the following microservices:

1. **Service Registry (Eureka Server)**: Central service discovery for microservices
2. **API Gateway**: Entry point for all client requests with routing and OAuth2/JWT authentication
3. **Inventory Service**: Manages product stock and availability
4. **Payment Service**: Handles payment transactions and validates stock with the Inventory Service

## Technology Stack

- **Java**: Version 21
- **Spring Boot**: Version 3.2.0
- **Spring Cloud**: For microservices architecture
  - Netflix Eureka for service discovery
  - Spring Cloud Gateway for API routing
  - WebClient for service-to-service communication
- **Spring Data JPA**: For data persistence
- **MariaDB**: As the relational database
- **Kafka**: For asynchronous event processing
- **Resilience4j**: For circuit breaking and retries
- **OAuth2/JWT**: For authentication and authorization
- **Log4j2**: For structured JSON logging
- **Swagger/OpenAPI**: For API documentation
- **Maven**: For build management and dependency resolution

## Project Structure

The project follows a polyrepo pattern, where each microservice is in its own repository:

```
obvious_ai/
├── api-gateway/          # API Gateway service
├── service-registry/     # Eureka Service Registry
├── inventory-service/    # Inventory management service
└── payment-service/      # Payment processing service
```

Each service follows Domain-Driven Design (DDD) principles with a layered architecture:

```
service/
├── domain/              # Domain Layer (Core Business Logic)
│   ├── model/           # Domain entities
│   ├── repository/      # Repository interfaces
│   ├── service/         # Domain services
│   └── exception/       # Domain-specific exceptions
├── application/         # Application Layer (Use Cases)
│   ├── dto/             # Data Transfer Objects
│   └── service/         # Application services
├── infrastructure/      # Infrastructure Layer 
│   ├── config/          # Configuration classes
│   ├── client/          # External service clients
│   ├── logging/         # Logging configuration
│   └── persistence/     # Persistence implementations
└── interfaces/          # Interface Layer
    └── rest/            # REST controllers
```

## Security

The system implements OAuth2/JWT-based authentication through the API Gateway with:
- JWT validation using the issuer's JWKS endpoint
- Environment-specific configuration for different deployment environments
- Role-based authorization within individual microservices
- Predefined roles: USER, ADMIN, INVENTORY_READ, INVENTORY_WRITE, PAYMENT_READ, PAYMENT_WRITE

## Resilience Features

The system includes multiple resilience patterns:
- Circuit breakers with Resilience4j to prevent cascading failures
- Fallback methods for graceful degradation when services are unavailable
- Configurable timeouts to prevent long-running operations
- Retry mechanisms for transient failures
- Asynchronous processing with Kafka for decoupling services

## Scalability and Resilience

- All services are designed to scale horizontally
- Resilience4j provides circuit breaking and retry mechanisms
- Timeout configurations prevent long-running operations
- Asynchronous processing with Kafka for decoupling services

## Architectural Diagrams

The project includes detailed architectural and sequence diagrams to provide visual representation of the system:

- **Architectural Diagrams**: High-level system design and component relationships
- **Sequence Diagrams**: Interaction flows between components for key operations

These diagrams can be found in the `documents` folder in the project root:

```
obvious_ai/
├── documents/
```

To view the diagrams, navigate to the documents folder and open the respective files in a compatible viewer.