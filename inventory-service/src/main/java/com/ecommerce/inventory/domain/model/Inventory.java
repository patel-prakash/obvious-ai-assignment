package com.ecommerce.inventory.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Product name is required")
    private String productName;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    private String description;

    /**
     * Version field for optimistic locking.
     * This prevents concurrent modifications by detecting version mismatches.
     * If two transactions try to update the same record, the second will fail with
     * an OptimisticLockingFailureException if the version has changed.
     */
    @Version
    private Long version;

    public boolean hasStock(int requiredQuantity) {
        return this.quantity >= requiredQuantity;
    }

    public void reduceStock(int quantity) {
        if (this.quantity < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        this.quantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.quantity += quantity;
    }
} 