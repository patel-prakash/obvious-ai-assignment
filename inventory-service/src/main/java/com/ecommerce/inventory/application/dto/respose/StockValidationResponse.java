package com.ecommerce.inventory.application.dto.respose;

/**
 * Record representing a stock validation response.
 */
public record StockValidationResponse(
        String productCode,
        boolean inStock,
        boolean locked,
        String lockReferenceId,
        int requestedQuantity,
        int availableQuantity,
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
        private String productCode;
        private boolean inStock;
        private boolean locked;
        private String lockReferenceId;
        private int requestedQuantity;
        private int availableQuantity;
        private String errorMessage;

        public Builder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder inStock(boolean inStock) {
            this.inStock = inStock;
            return this;
        }

        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder lockReferenceId(String lockReferenceId) {
            this.lockReferenceId = lockReferenceId;
            return this;
        }

        public Builder requestedQuantity(int requestedQuantity) {
            this.requestedQuantity = requestedQuantity;
            return this;
        }

        public Builder availableQuantity(int availableQuantity) {
            this.availableQuantity = availableQuantity;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public StockValidationResponse build() {
            return new StockValidationResponse(
                    productCode, inStock, locked, lockReferenceId, requestedQuantity, availableQuantity, errorMessage);
        }
    }
} 