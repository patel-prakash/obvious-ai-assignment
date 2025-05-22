package com.ecommerce.payment.domain.exception;

/**
 * Exception thrown when a requested payment transaction cannot be found.
 */
public class PaymentNotFoundException extends RuntimeException {

    private final String transactionId;

    public PaymentNotFoundException(String transactionId) {
        super("Payment with transaction ID " + transactionId + " not found");
        this.transactionId = transactionId;
    }

    public PaymentNotFoundException(String transactionId, String message) {
        super("Payment with transaction ID " + transactionId + " not found: " + message);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
} 