package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.model.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Record representing a payment request.
 */
public record PaymentRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,

        @NotBlank(message = "Product code is required")
        String productCode,

        @Positive(message = "Quantity must be positive")
        int quantity,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode
) {
    // Records already provide equals, hashCode, toString, and constructor

    /**
     * Builder pattern for the record
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderId;
        private String productCode;
        private int quantity;
        private BigDecimal amount;
        private PaymentMode paymentMode;

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

        public PaymentRequest build() {
            return new PaymentRequest(orderId, productCode, quantity, amount, paymentMode);
        }
    }
} 