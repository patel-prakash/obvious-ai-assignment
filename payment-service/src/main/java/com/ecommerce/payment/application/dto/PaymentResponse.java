package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.model.PaymentMode;
import com.ecommerce.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record representing a payment response.
 */
public record PaymentResponse(
        String transactionId,
        String orderId,
        String productCode,
        int quantity,
        BigDecimal amount,
        PaymentMode paymentMode,
        PaymentStatus status,
        LocalDateTime timestamp,
        String failureReason,
        String stockLockReferenceId
) {
    // Records already provide equals, hashCode, toString, and constructor

    /**
     * Builder pattern for the record
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String transactionId;
        private String orderId;
        private String productCode;
        private int quantity;
        private BigDecimal amount;
        private PaymentMode paymentMode;
        private PaymentStatus status;
        private LocalDateTime timestamp;
        private String failureReason;
        private String stockLockReferenceId;

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder paymentMode(PaymentMode paymentMode) {
            this.paymentMode = paymentMode;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder stockLockReferenceId(String stockLockReferenceId) {
            this.stockLockReferenceId = stockLockReferenceId;
            return this;
        }

        public PaymentResponse build() {
            return new PaymentResponse(
                    transactionId, orderId, productCode, quantity, amount,
                    paymentMode, status, timestamp, failureReason, stockLockReferenceId);
        }
    }
} 