package com.ecommerce.payment.domain.exception;

/**
 * Exception thrown when a payment processing operation fails.
 */
public class PaymentProcessingException extends RuntimeException {

    private final String orderId;

    public PaymentProcessingException(String orderId, String message) {
        super("Failed to process payment for order " + orderId + ": " + message);
        this.orderId = orderId;
    }

    public PaymentProcessingException(String orderId, String message, Throwable cause) {
        super("Failed to process payment for order " + orderId + ": " + message, cause);
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
} 