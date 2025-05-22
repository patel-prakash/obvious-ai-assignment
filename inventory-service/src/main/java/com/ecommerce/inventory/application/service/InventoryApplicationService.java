package com.ecommerce.inventory.application.service;

import com.ecommerce.inventory.application.dto.request.InventoryItemRequest;
import com.ecommerce.inventory.application.dto.request.StockValidationRequest;
import com.ecommerce.inventory.application.dto.respose.InventoryItemResponse;
import com.ecommerce.inventory.application.dto.respose.StockValidationResponse;
import com.ecommerce.inventory.domain.exception.InventoryNotFoundException;
import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryApplicationService {

    private final InventoryDomainService inventoryDomainService;

    // Thread-safe concurrent map for lock registry, replace this with distributed storage for resiliency
    private final Map<String, StockLockInfo> lockRegistry = new ConcurrentHashMap<>();

    public InventoryItemResponse addOrUpdateInventory(InventoryItemRequest request) {
        Inventory savedItem = Optional.ofNullable(request.productCode())
                .flatMap(inventoryDomainService::getInventoryItemByProductCode)
                .map(existingItem -> {
                    // Update existing item
                    existingItem.setProductName(request.productName());
                    existingItem.setQuantity(request.quantity());
                    existingItem.setDescription(request.description());
                    return inventoryDomainService.addInventoryItem(existingItem);
                })
                .orElseGet(() -> {
                    // Create new inventory item
                    Inventory newItem = Inventory.builder()
                            .productCode(request.productCode())
                            .productName(request.productName())
                            .quantity(request.quantity())
                            .description(request.description())
                            .build();

                    return inventoryDomainService.addInventoryItem(newItem);
                });

        return mapToResponse(savedItem);
    }

    private InventoryItemResponse mapToResponse(Inventory item) {
        return Optional.ofNullable(item)
                .map(i -> InventoryItemResponse.builder()
                        .id(i.getId())
                        .productCode(i.getProductCode())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .description(i.getDescription())
                        .build())
                .orElseThrow(() -> new IllegalArgumentException("Inventory item cannot be null"));
    }

    public InventoryItemResponse getInventoryByProductCode(String productCode) {
        return Optional.ofNullable(productCode)
                .flatMap(inventoryDomainService::getInventoryItemByProductCode)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product code cannot be null"));
    }

    /**
     * Validates and locks stock for a product.
     * Using SERIALIZABLE isolation level to prevent dirty reads, non-repeatable
     * reads, and phantom reads.
     * This ensures that concurrent transactions don't interfere with each other.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StockValidationResponse validateStock(StockValidationRequest request) {
        log.info("Validating and locking stock for product: {} with quantity: {}",
                request.productCode(), request.quantity());

        String lockReferenceId = UUID.randomUUID().toString();

        try {
            // Get product to check available quantity
            Inventory item = inventoryDomainService.getInventoryItemByProductCode(request.productCode())
                    .orElseThrow(() -> new InventoryNotFoundException("Product not found"));
            int availableQuantity = item.getQuantity();

            // Using a functional approach with map() and orElse()
            return inventoryDomainService.validateAndLockStock(
                            request.productCode(),
                            request.quantity(),
                            lockReferenceId)
                    .map(updatedItem -> {
                        // Stock was successfully locked
                        lockRegistry.put(lockReferenceId, new StockLockInfo(
                                request.productCode(),
                                request.quantity()));

                        return StockValidationResponse.builder()
                                .productCode(request.productCode())
                                .inStock(true)
                                .locked(true)
                                .lockReferenceId(lockReferenceId)
                                .requestedQuantity(request.quantity())
                                .availableQuantity(availableQuantity)
                                .build();
                    })
                    .orElse(
                            // Not enough stock available
                            StockValidationResponse.builder()
                                    .productCode(request.productCode())
                                    .inStock(false)
                                    .locked(false)
                                    .lockReferenceId(null)
                                    .requestedQuantity(request.quantity())
                                    .availableQuantity(availableQuantity)
                                    .build());

        } catch (Exception e) {
            // Handle all exceptions with the same response pattern
            log.error("Error validating and locking stock: {}", e.getMessage());
            return StockValidationResponse.builder()
                    .productCode(request.productCode())
                    .inStock(false)
                    .locked(false)
                    .lockReferenceId(null)
                    .requestedQuantity(request.quantity())
                    .availableQuantity(0)
                    .build();
        }
    }

    /**
     * Unlocks previously locked stock.
     * Using SERIALIZABLE isolation to ensure consistent updates.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean unlockStock(String lockReferenceId) {
        log.info("Unlocking stock with reference ID: {}", lockReferenceId);

        return Optional.ofNullable(lockRegistry.remove(lockReferenceId))
                .map(lockInfo -> {
                    try {
                        return inventoryDomainService.validateAndLockStock(
                                        lockInfo.productCode, 0, "unlock-" + lockReferenceId)
                                .map(item -> {
                                    // Increase the stock
                                    item.increaseStock(lockInfo.quantity);
                                    inventoryDomainService.addInventoryItem(item);

                                    log.info("Successfully unlocked stock for product: {} with quantity: {}",
                                            lockInfo.productCode, lockInfo.quantity);
                                    return true;
                                })
                                .orElseGet(() -> {
                                    // If we couldn't get a lock on the item, put the lock info back
                                    lockRegistry.put(lockReferenceId, lockInfo);
                                    log.error("Could not obtain lock for product {} to unlock stock", lockInfo.productCode);
                                    return false;
                                });
                    } catch (Exception e) {
                        // If unlocking fails, put the lock info back in the registry
                        lockRegistry.put(lockReferenceId, lockInfo);
                        log.error("Error unlocking stock: {}", e.getMessage());
                        return false;
                    }
                })
                .orElseGet(() -> {
                    log.warn("No lock found with reference ID: {}", lockReferenceId);
                    return false;
                });
    }

    private record StockLockInfo(String productCode, int quantity) {
    }
}