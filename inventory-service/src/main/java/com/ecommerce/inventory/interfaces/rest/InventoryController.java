package com.ecommerce.inventory.interfaces.rest;

import com.ecommerce.inventory.application.dto.request.InventoryItemRequest;
import com.ecommerce.inventory.application.dto.request.StockValidationRequest;
import com.ecommerce.inventory.application.dto.respose.InventoryItemResponse;
import com.ecommerce.inventory.application.dto.respose.StockValidationResponse;
import com.ecommerce.inventory.application.service.InventoryApplicationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryApplicationService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "addOrUpdateInventoryFallback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryItemResponse> addOrUpdateInventory(
            @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addOrUpdateInventory(request));
    }

    @GetMapping("/{productCode}")
    @CircuitBreaker(name = "inventory", fallbackMethod = "getInventoryByProductCodeFallback")
    public ResponseEntity<InventoryItemResponse> getInventoryByProductCode(@PathVariable String productCode) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductCode(productCode));
    }

    @PostMapping("/validate")
    @CircuitBreaker(name = "inventory", fallbackMethod = "validateStockFallback")
    public ResponseEntity<StockValidationResponse> validateStock(@Valid @RequestBody StockValidationRequest request) {
        return ResponseEntity.ok(inventoryService.validateStock(request));
    }

    @PostMapping("/unlock/{lockReferenceId}")
    @CircuitBreaker(name = "inventory", fallbackMethod = "unlockStockFallback")
    public ResponseEntity<Boolean> unlockStock(@PathVariable String lockReferenceId) {
        return ResponseEntity.ok(inventoryService.unlockStock(lockReferenceId));
    }

    // Fallback methods

    public ResponseEntity<InventoryItemResponse> addOrUpdateInventoryFallback(
            InventoryItemRequest request, Exception ex) {
        log.error("Circuit breaker triggered for inventory create/update: {}", ex.getMessage());

        InventoryItemResponse degradedResponse = InventoryItemResponse.builder()
                .productCode(request.productCode())
                .quantity(0)
                .status("UNAVAILABLE")
                .errorMessage("Inventory service temporarily unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(degradedResponse);
    }

    public ResponseEntity<InventoryItemResponse> getInventoryByProductCodeFallback(String productCode, Exception ex) {
        log.error("Circuit breaker triggered for inventory retrieval: {}", ex.getMessage());

        InventoryItemResponse degradedResponse = InventoryItemResponse.builder()
                .productCode(productCode)
                .quantity(0)
                .status("UNAVAILABLE")
                .errorMessage("Inventory service temporarily unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(degradedResponse);
    }

    public ResponseEntity<StockValidationResponse> validateStockFallback(StockValidationRequest request, Exception ex) {
        log.error("Circuit breaker triggered for stock validation: {}", ex.getMessage());

        StockValidationResponse degradedResponse = StockValidationResponse.builder()
                .inStock(false)
                .locked(false)
                .availableQuantity(0)
                .requestedQuantity(request.quantity())
                .errorMessage("Inventory service temporarily unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(degradedResponse);
    }

    public ResponseEntity<Boolean> unlockStockFallback(String lockReferenceId, Exception ex) {
        log.error("Circuit breaker triggered for stock unlock: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(false);
    }
}