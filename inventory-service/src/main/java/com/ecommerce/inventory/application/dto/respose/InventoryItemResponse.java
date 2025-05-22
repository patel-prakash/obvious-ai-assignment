package com.ecommerce.inventory.application.dto.respose;

/**
 * Record representing an inventory item response.
 */
public record InventoryItemResponse(
        Long id,
        String productCode,
        String productName,
        int quantity,
        String description,
        String status,
        String errorMessage
) {
    // Records already provide equals, hashCode, toString, and constructor

    /**
     * Builder pattern for the record
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String productCode;
        private String productName;
        private int quantity;
        private String description;
        private String status;
        private String errorMessage;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public InventoryItemResponse build() {
            return new InventoryItemResponse(id, productCode, productName, quantity, description, status, errorMessage);
        }
    }
} 