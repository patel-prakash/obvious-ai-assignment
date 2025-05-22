package com.ecommerce.inventory.domain.service.impl;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import com.ecommerce.inventory.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDomainServiceImpl implements InventoryDomainService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public Inventory addInventoryItem(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> getInventoryItemByProductCode(String productCode) {
        return inventoryRepository.findByProductCode(productCode);
    }

    /**
     * Validates and locks stock in a single atomic operation.
     * Using SERIALIZABLE isolation and pessimistic locking to prevent concurrent transactions.
     * The combination of:
     * 1. SERIALIZABLE isolation
     * 2. Pessimistic locking with SELECT FOR UPDATE
     * 3. Optimistic locking via @Version
     * ensures the highest level of concurrency control.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<Inventory> validateAndLockStock(String productCode, int quantity, String lockReferenceId) {
        try {
            // Using functional approach with flatMap
            return inventoryRepository.findByProductCodeWithLock(productCode)
                    .flatMap(item -> {
                        // Check if we have sufficient stock
                        if (!item.hasStock(quantity)) {
                            log.warn("Insufficient stock for product {}: requested={}, available={}",
                                    productCode, quantity, item.getQuantity());
                            return Optional.empty();
                        }

                        // Lock the stock by reducing the quantity
                        item.reduceStock(quantity);
                        log.info("Locking stock for product {}: quantity={}, reference={}",
                                productCode, quantity, lockReferenceId);

                        // Save will update the version due to @Version annotation
                        return Optional.of(inventoryRepository.save(item));
                    })
                    .or(() -> {
                        // Handle case when product code not found
                        log.warn("Product not found in inventory: {}", productCode);
                        return Optional.empty();
                    });
        } catch (Exception e) {
            log.error("Error while locking stock for product {}: {}",
                    productCode, e.getMessage());
            return Optional.empty();
        }
    }
}