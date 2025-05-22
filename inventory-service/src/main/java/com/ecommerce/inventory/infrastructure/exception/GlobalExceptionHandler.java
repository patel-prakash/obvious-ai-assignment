package com.ecommerce.inventory.infrastructure.exception;

import com.ecommerce.inventory.domain.exception.ConcurrencyException;
import com.ecommerce.inventory.domain.exception.InvalidOperationException;
import com.ecommerce.inventory.domain.exception.InventoryNotFoundException;
import com.ecommerce.inventory.domain.exception.StockUpdateException;
import com.ecommerce.inventory.domain.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for standardizing error responses across the application.
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

    /**
     * Handle InventoryNotFoundException.
     */
    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInventoryNotFoundException(
            InventoryNotFoundException ex, HttpServletRequest request) {
        log.error("Inventory not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(NOT_FOUND)
                .message("Inventory item not found")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle StockUpdateException.
     */
    @ExceptionHandler(StockUpdateException.class)
    public ResponseEntity<ErrorResponse> handleStockUpdateException(
            StockUpdateException ex, HttpServletRequest request) {
        log.error("Error updating stock: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(BAD_REQUEST)
                .message("Error updating inventory stock")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConcurrencyException.
     */
    @ExceptionHandler(ConcurrencyException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyException(
            ConcurrencyException ex, HttpServletRequest request) {
        log.error("Concurrency conflict: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(CONFLICT)
                .message("Concurrency conflict detected")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.CONFLICT)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle InvalidOperationException.
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException ex, HttpServletRequest request) {
        log.error("Invalid operation: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(BAD_REQUEST)
                .message("Invalid operation")
                .detailedMessage(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle OptimisticLockingFailureException.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.error("Optimistic locking failure: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(CONFLICT)
                .message("The resource was updated by another transaction")
                .detailedMessage("Optimistic locking error: The resource was modified by another transaction. Please retry.")
                .status(HttpStatus.CONFLICT)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle PessimisticLockingFailureException.
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handlePessimisticLockingFailureException(
            PessimisticLockingFailureException ex, HttpServletRequest request) {
        log.error("Pessimistic locking failure: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(CONFLICT)
                .message("The resource is locked by another transaction")
                .detailedMessage("Pessimistic locking error: The resource is currently locked by another transaction. Please retry later.")
                .status(HttpStatus.CONFLICT)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
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