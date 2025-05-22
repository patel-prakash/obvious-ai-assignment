package com.ecommerce.inventory.domain.exception;

/**
 * Exception thrown when there's an error updating inventory stock.
 */
public class StockUpdateException extends RuntimeException {

    private final String productCode;

    public StockUpdateException(String productCode, String message) {
        super("Failed to update stock for product " + productCode + ": " + message);
        this.productCode = productCode;
    }

    public StockUpdateException(String productCode, String message, Throwable cause) {
        super("Failed to update stock for product " + productCode + ": " + message, cause);
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }
} 