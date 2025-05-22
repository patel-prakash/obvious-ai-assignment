# API Gateway

This module implements the API Gateway for the e-commerce microservices architecture.

## Purpose

The API Gateway serves as the entry point for all client requests and provides the following functionality:
- Request routing to appropriate microservices
- Authentication and authorization using OAuth2/JWT
- Load balancing via Eureka service discovery
- Cross-cutting concerns handling
- Request path rewriting

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Security OAuth2 Resource Server for JWT authentication

## Structure

The API Gateway follows a simple structure:

```
├── config
│   ├── RouteConfig.java      # Configures routes for each microservice
│   └── SecurityConfig.java   # Configures OAuth2/JWT authentication
└── ApiGatewayApplication.java # Main application class
```

## Routes

The gateway routes requests to the following services:

1. **Inventory Service**
   - **Path**: `/api/inventory/**`
   - **Rewrites to**: `/api/**` within the inventory service
   - **Target**: `lb://inventory-service`

2. **Payment Service**
   - **Path**: `/api/payment/**`
   - **Rewrites to**: `/api/**` within the payment service
   - **Target**: `lb://payment-service`

## Authentication

The gateway implements OAuth2 Resource Server with JWT-based authentication:
- Validates tokens using the JWT issuer's JWKS endpoint
- Supports environment-specific configuration of JWT properties
- Protected endpoints require a valid JWT token in the Authorization header

## Configuration

The gateway supports flexible configuration through environment variables:
- `AUTH_ISSUER_URI`: The OAuth2 issuer URI
- `AUTH_JWK_URI`: The JSON Web Key Set URI for signature validation
- `AUTH_AUDIENCE`: The expected audience claim value

## Building and Running

```bash
# Build the service
./mvnw clean package

# Run the service with default configuration
./mvnw spring-boot:run

# Run with custom OAuth2 configuration
export AUTH_ISSUER_URI=https://your-auth-provider.com/
export AUTH_AUDIENCE=your-api-audience
./mvnw spring-boot:run
```

## Notes

- The gateway runs on port 8080
- It registers itself with Eureka Server (http://localhost:8761/eureka)
- Public endpoints like `/actuator/**` are accessible without authentication
- All other endpoints require valid JWT authentication
- Individual microservices handle their own authorization 