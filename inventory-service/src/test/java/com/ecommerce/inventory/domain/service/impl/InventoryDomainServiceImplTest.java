package com.ecommerce.inventory.domain.service.impl;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDomainServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryDomainServiceImpl inventoryDomainService;

    // Helper method to create test inventory items
    private Inventory createTestInventoryItem(Long id, String productCode, String productName,
                                              int quantity, String description) {
        return Inventory.builder()
                .id(id)
                .productCode(productCode)
                .productName(productName)
                .quantity(quantity)
                .description(description)
                .version(1L)
                .build();
    }

    // Method source for inventory stock validation with different quantities
    static Stream<Arguments> inventoryStockProvider() {
        return Stream.of(
                // productCode, currentStock, requestedQuantity, shouldSucceed
                Arguments.of("PROD-1", 10, 5, true),     // Enough stock
                Arguments.of("PROD-2", 10, 10, true),    // Exact stock
                Arguments.of("PROD-3", 10, 11, false),   // Not enough stock
                Arguments.of("PROD-4", 0, 1, false)      // Zero stock
        );
    }

    @Test
    @DisplayName("Should add a new inventory item")
    void addInventoryItem() {
        // Arrange
        Inventory item = createTestInventoryItem(null, "TEST-PROD", "Test Product", 100, "Test Description");
        Inventory savedItem = createTestInventoryItem(1L, "TEST-PROD", "Test Product", 100, "Test Description");

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedItem);

        // Act
        Inventory result = inventoryDomainService.addInventoryItem(item);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST-PROD", result.getProductCode());
        assertEquals(100, result.getQuantity());
        verify(inventoryRepository, times(1)).save(item);
    }

    @ParameterizedTest
    @DisplayName("Should get inventory item by product code when it exists")
    @ValueSource(strings = {"PROD-A", "PROD-B", "PROD-C"})
    void getInventoryItemByProductCodeWhenExists(String productCode) {
        // Arrange
        Inventory expectedItem = createTestInventoryItem(1L, productCode, "Test Product", 100, "Test Description");
        when(inventoryRepository.findByProductCode(productCode)).thenReturn(Optional.of(expectedItem));

        // Act
        Optional<Inventory> result = inventoryDomainService.getInventoryItemByProductCode(productCode);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(productCode, result.get().getProductCode());
        assertEquals(100, result.get().getQuantity());
        verify(inventoryRepository, times(1)).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should return empty Optional when product code does not exist")
    void getInventoryItemByProductCodeWhenNotExists() {
        // Arrange
        String productCode = "NON-EXISTENT";
        when(inventoryRepository.findByProductCode(productCode)).thenReturn(Optional.empty());

        // Act
        Optional<Inventory> result = inventoryDomainService.getInventoryItemByProductCode(productCode);

        // Assert
        assertFalse(result.isPresent());
        verify(inventoryRepository, times(1)).findByProductCode(productCode);
    }

    @ParameterizedTest
    @DisplayName("Should validate and lock stock based on available quantity")
    @MethodSource("inventoryStockProvider")
    void validateAndLockStockWithDifferentQuantities(String productCode,
                                                     int currentStock,
                                                     int requestedQuantity,
                                                     boolean shouldSucceed) {
        // Arrange
        String lockReferenceId = UUID.randomUUID().toString();
        Inventory existingItem = createTestInventoryItem(1L, productCode, "Test Product", currentStock, "Test Description");
        Inventory updatedItem = null;

        if (shouldSucceed) {
            updatedItem = createTestInventoryItem(1L, productCode, "Test Product", currentStock - requestedQuantity, "Test Description");
        }

        when(inventoryRepository.findByProductCodeWithLock(productCode)).thenReturn(Optional.of(existingItem));

        if (shouldSucceed) {
            when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedItem);
        }

        // Act
        Optional<Inventory> result = inventoryDomainService.validateAndLockStock(productCode, requestedQuantity, lockReferenceId);

        // Assert
        assertEquals(shouldSucceed, result.isPresent());

        if (shouldSucceed) {
            assertEquals(currentStock - requestedQuantity, result.get().getQuantity());
            verify(inventoryRepository, times(1)).save(any(Inventory.class));
        } else {
            verify(inventoryRepository, never()).save(any(Inventory.class));
        }

        verify(inventoryRepository, times(1)).findByProductCodeWithLock(productCode);
    }

    @ParameterizedTest
    @DisplayName("Should handle stock operations with different quantities")
    @CsvSource({
            "100, 50, 50",    // Regular reduction
            "50, 30, 20",     // Another reduction
            "20, 20, 0"       // Reducing to zero
    })
    void reduceStockWithValidQuantities(int initialQuantity, int reductionAmount, int expectedQuantity) {
        // Arrange
        Inventory item = createTestInventoryItem(1L, "TEST-PROD", "Test Product", initialQuantity, "Test Description");

        // Act
        item.reduceStock(reductionAmount);

        // Assert
        assertEquals(expectedQuantity, item.getQuantity());
    }

    @Test
    @DisplayName("Should throw exception when reducing more than available stock")
    void reduceStockThrowsExceptionWhenInsufficientStock() {
        // Arrange
        Inventory item = createTestInventoryItem(1L, "TEST-PROD", "Test Product", 10, "Test Description");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> item.reduceStock(11));
        assertEquals(10, item.getQuantity()); // Stock should remain unchanged
    }

    @ParameterizedTest
    @DisplayName("Should increase stock with different amounts")
    @ValueSource(ints = {1, 5, 10, 100})
    void increaseStock(int increaseAmount) {
        // Arrange
        int initialQuantity = 50;
        Inventory item = createTestInventoryItem(1L, "TEST-PROD", "Test Product", initialQuantity, "Test Description");

        // Act
        item.increaseStock(increaseAmount);

        // Assert
        assertEquals(initialQuantity + increaseAmount, item.getQuantity());
    }
} 