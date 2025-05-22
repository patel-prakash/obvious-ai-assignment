package com.ecommerce.inventory.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Record representing a stock validation request.
 */
public record StockValidationRequest(
        @NotBlank(message = "Product code is required")
        String productCode,

        @Min(value = 1, message = "Quantity must be at least 1")
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