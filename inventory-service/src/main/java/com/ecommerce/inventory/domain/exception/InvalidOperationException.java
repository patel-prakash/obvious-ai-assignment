package com.ecommerce.inventory.domain.exception;

/**
 * Exception thrown when an operation is invalid in the current context.
 * For example, trying to reduce stock below zero or attempting an operation
 * that violates business rules.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 