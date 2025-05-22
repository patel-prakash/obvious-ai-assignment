package com.ecommerce.payment.application.dto;

/**
 * Record representing a stock validation request.
 */
public record StockValidationRequest(
        String productCode,
        int quantity
) {
    // Records already provide equals, hashCode, toString, and constructor

    /**
     * Builder pattern for the record
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String productCode;
        private int quantity;

        public Builder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public StockValidationRequest build() {
            return new StockValidationRequest(productCode, quantity);
        }
    }
} 