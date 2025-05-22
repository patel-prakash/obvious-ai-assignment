package com.ecommerce.payment.infrastructure.exception;

import com.ecommerce.payment.domain.exception.PaymentNotFoundException;
import com.ecommerce.payment.domain.exception.PaymentProcessingException;
import com.ecommerce.payment.domain.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for standardizing error responses across the payment service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String CONFLICT = "CONFLICT";
    private static final String FORBIDDEN = "FORBIDDEN";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    private static final String PAYMENT_ERROR = "PAYMENT_ERROR";

    /**
     * Handle PaymentNotFoundException.
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(
            PaymentNotFoundException ex, HttpServletRequest request) {
        log.error("Payment not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(NOT_FOUND)
                .message("Payment not found")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle PaymentProcessingException.
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(
            PaymentProcessingException ex, HttpServletRequest request) {
        log.error("Payment processing error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(PAYMENT_ERROR)
                .message("Payment processing failed")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle WebClientResponseException for inventory service errors.
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(
            WebClientResponseException ex, HttpServletRequest request) {
        log.error("Service communication error: {}", ex.getMessage());

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(status.is5xxServerError() ? SERVICE_UNAVAILABLE : BAD_REQUEST)
                .message("Error communicating with external service")
                .detailedMessage(ex.getMessage())
                .status(status.is5xxServerError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, response.status());
    }

    /**
     * Handle WebClientRequestException for network errors.
     */
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequestException(
            WebClientRequestException ex, HttpServletRequest request) {
        log.error("Service connection error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(SERVICE_UNAVAILABLE)
                .message("Service is currently unavailable")
                .detailedMessage("Could not connect to external service: " + ex.getMessage())
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle KafkaException for messaging errors.
     */
    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ErrorResponse> handleKafkaException(
            KafkaException ex, HttpServletRequest request) {
        log.error("Kafka messaging error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(INTERNAL_SERVER_ERROR)
                .message("Messaging service error")
                .detailedMessage("Error publishing message to Kafka: " + ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle RestClientException for REST client errors.
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(
            RestClientException ex, HttpServletRequest request) {
        log.error("REST client error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(SERVICE_UNAVAILABLE)
                .message("Error communicating with external service")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle ResourceAccessException for connection timeouts.
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(
            ResourceAccessException ex, HttpServletRequest request) {
        log.error("Resource access error: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(SERVICE_UNAVAILABLE)
                .message("External service is not responding")
                .detailedMessage("Connection timeout or service unavailable: " + ex.getMessage())
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle MethodArgumentNotValidException for validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(VALIDATION_ERROR)
                .message("Validation error")
                .detailedMessage("Validation failed for request parameters")
                .validationErrors(validationErrors)
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BindException for form validation errors.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {
        log.error("Binding error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            validationErrors.add(new ErrorResponse.ValidationError(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()));
        }

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(VALIDATION_ERROR)
                .message("Validation error")
                .detailedMessage("Form binding failed")
                .validationErrors(validationErrors)
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConstraintViolationException for validation errors.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.error("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.ValidationError(
                        getPropertyName(violation),
                        violation.getMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(VALIDATION_ERROR)
                .message("Validation error")
                .detailedMessage("Constraint violation detected")
                .validationErrors(validationErrors)
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentTypeMismatchException for type mismatch errors.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("Type mismatch: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(BAD_REQUEST)
                .message("Type mismatch")
                .detailedMessage("Parameter '" + ex.getName() + "' should be of type " +
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"))
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle DataIntegrityViolationException for database constraint violations.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(CONFLICT)
                .message("Data integrity violation")
                .detailedMessage("The operation violates database constraints")
                .status(HttpStatus.CONFLICT)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle AccessDeniedException for authorization errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(FORBIDDEN)
                .message("Access denied")
                .detailedMessage("You don't have permission to perform this operation")
                .status(HttpStatus.FORBIDDEN)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle all other exceptions not explicitly handled.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(INTERNAL_SERVER_ERROR)
                .message("Internal server error")
                .detailedMessage("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extract property name from constraint violation.
     */
    private String getPropertyName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDotIndex = propertyPath.lastIndexOf('.');
        return lastDotIndex > 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;
    }
} 