package com.ecommerce.inventory.domain.exception;

/**
 * Exception thrown when a concurrency conflict occurs during data updates.
 * This can happen with optimistic locking (version conflicts) or
 * when a resource is locked by another transaction.
 */
public class ConcurrencyException extends RuntimeException {

    private final String resourceId;

    public ConcurrencyException(String resourceId, String message) {
        super("Concurrency conflict for resource " + resourceId + ": " + message);
        this.resourceId = resourceId;
    }

    public ConcurrencyException(String resourceId, String message, Throwable cause) {
        super("Concurrency conflict for resource " + resourceId + ": " + message, cause);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
} 