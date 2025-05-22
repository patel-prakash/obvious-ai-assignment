package com.ecommerce.inventory.domain.service;

import com.ecommerce.inventory.domain.model.Inventory;

import java.util.Optional;

/**
 * Domain service for inventory operations using functional programming paradigm.
 * Methods return Optional where appropriate to handle the absence of values.
 */
public interface InventoryDomainService {

    /**
     * Add or update an inventory item
     */
    Inventory addInventoryItem(Inventory inventory);

    /**
     * Find an inventory item by product code
     * Returns the item or throws InventoryNotFoundException if not found
     */
    Optional<Inventory> getInventoryItemByProductCode(String productCode);

    /**
     * Validate and lock stock in one atomic operation
     * Returns Optional with the updated item if successful, empty Optional otherwise
     */
    Optional<Inventory> validateAndLockStock(String productCode, int quantity, String lockReferenceId);
}