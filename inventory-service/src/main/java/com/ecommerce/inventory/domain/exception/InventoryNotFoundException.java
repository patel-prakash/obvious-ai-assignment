package com.ecommerce.inventory.domain.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String message) {
        super(message);
    }

    public InventoryNotFoundException(String productCode, String message) {
        super("Product with code " + productCode + " not found: " + message);
    }
} 