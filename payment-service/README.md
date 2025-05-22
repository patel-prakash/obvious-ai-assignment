# Payment Service

This microservice is responsible for processing payments in the e-commerce system, with stock validation via the
Inventory Service.

## Features

- Process payments with stock validation
- Retrieve payment details by transaction ID
- Event publishing through Kafka for successful payments
- Circuit breaker pattern for resilience against Inventory Service failures
- Structured JSON logging for enterprise observability
- Fallback mechanisms for graceful degradation

## Architecture

The service follows Domain-Driven Design (DDD) principles with a layered architecture:

```
payment-service/
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
│   └── exception/       # Infrastructure exceptions
└── interfaces/          # Interface Layer
    └── rest/            # REST controllers
```

## API Endpoints

| Endpoint | Method | Description | Required Role |
|----------|--------|-------------|--------------|
| `/api/payment` | POST | Process payment | ADMIN, PAYMENT_WRITE |
| `/api/payment/{transactionId}` | GET | Retrieve payment details | USER, ADMIN, PAYMENT_READ |


## Technical Details

- **Port**: 8082
- **Service Name**: payment-service
- **Database**: MariaDB (paymentdb)
- **Service Communication**: WebClient for calling Inventory Service
- **Event Streaming**: Kafka for publishing payment events
- **Resilience**: Circuit breaker pattern using Resilience4j
- **Logging**: JSON structured logging with Log4j2
- **Documentation**: Swagger UI at `/swagger-ui.html`
- **Security**: OAuth2/JWT authentication with role-based authorization


## Resilience Features

The service implements multiple resilience patterns:

- Circuit breakers with Resilience4j to prevent cascading failures
- Fallback methods for graceful degradation when the Inventory Service is unavailable
- Configurable timeouts to prevent long-running operations
- Event-driven architecture with Kafka for asynchronous processing
