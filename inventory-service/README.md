# Inventory Service

This microservice is responsible for managing product inventory in the e-commerce system.

## Features

- Add/update inventory items
- Get inventory item details by product code
- Validate stock availability for a product
- Unlock stock for completed or failed payments
- Structured JSON logging for enterprise observability
- Circuit breaker pattern for resilience

## Architecture

The service follows Domain-Driven Design (DDD) principles with a layered architecture:

```
inventory-service/
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
│   ├── logging/         # Logging configuration
│   └── exception/       # Infrastructure exceptions
└── interfaces/          # Interface Layer
    └── rest/            # REST controllers
```

## API Endpoints

| Endpoint | Method | Description | Required Role |
|----------|--------|-------------|--------------|
| `/api/inventory` | POST | Add or update product stock | ADMIN, INVENTORY_WRITE |
| `/api/inventory/{productCode}` | GET | Get product stock details | USER, ADMIN, INVENTORY_READ |
| `/api/inventory/validate` | POST | Check stock availability | PAYMENT_WRITE |
| `/api/inventory/unlock/{lockReferenceId}` | POST | Unlock previously locked stock | INVENTORY_WRITE |

### Request/Response Models

#### InventoryItemRequest
```json
{
  "productCode": "string",
  "quantity": "integer",
  "reorderThreshold": "integer"
}
```

#### InventoryItemResponse
```json
{
  "productCode": "string",
  "quantity": "integer",
  "status": "string",
  "locked": "boolean",
  "reorderThreshold": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "errorMessage": "string"
}
```

#### StockValidationRequest
```json
{
  "productCode": "string",
  "quantity": "integer"
}
```

#### StockValidationResponse
```json
{
  "inStock": "boolean",
  "locked": "boolean",
  "availableQuantity": "integer",
  "requestedQuantity": "integer",
  "errorMessage": "string"
}
```

## Technical Details

- **Port**: 8081
- **Service Name**: inventory-service
- **Database**: MariaDB (inventorydb)
- **Resilience**: Circuit breaker pattern using Resilience4j
- **Logging**: JSON structured logging with Log4j2
- **Documentation**: Swagger UI at `/swagger-ui.html`
- **Security**: OAuth2/JWT authentication with role-based authorization

## Resilience Features

The service implements multiple resilience patterns:

- Circuit breakers with Resilience4j to prevent cascading failures
- Fallback methods for graceful degradation
- Configurable timeouts to prevent long-running operations
- Structured error handling with domain-specific exceptions

## Logging

The service implements enterprise-grade JSON structured logging with:

- Correlation IDs for distributed tracing
- Inventory operation tracking
- Asynchronous logging for performance
- Separate log files for application, errors, and performance
- Log file rotation based on size and time

## Building and Running

```bash
# Build the service
./mvnw clean package

# Run the service with default profile
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Environment Configuration

The service uses Spring profiles for environment-specific configuration:
- `dev`: Local development environment
- `test`: Test environment
- `prod`: Production environment

## Docker Deployment

```bash
# Build Docker image
docker build -t inventory-service:latest .

# Run Docker container
docker run -p 8081:8081 inventory-service:latest
```

## Notes

- The service registers with Eureka Service Registry on startup
- It implements circuit breaker patterns to handle failures gracefully
- API documentation is available through Swagger UI
- The service follows Domain-Driven Design principles with a layered architecture 