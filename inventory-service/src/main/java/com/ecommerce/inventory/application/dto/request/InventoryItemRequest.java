package com.ecommerce.inventory.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Record representing an inventory item request.
 */
public record InventoryItemRequest(
        @NotBlank(message = "Product code is required")
        String productCode,

        @NotBlank(message = "Product name is required")
        String productName,

        @Min(value = 0, message = "Quantity cannot be negative")
        int quantity,

        String description
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
        private String productName;
        private int quantity;
        private String description;

        public Builder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public InventoryItemRequest build() {
            return new InventoryItemRequest(productCode, productName, quantity, description);
        }
    }
} 