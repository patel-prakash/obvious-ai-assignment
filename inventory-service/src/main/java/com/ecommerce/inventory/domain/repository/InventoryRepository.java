package com.ecommerce.inventory.domain.repository;

import com.ecommerce.inventory.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductCode(String productCode);

    /**
     * Find inventory item by product code with pessimistic write lock.
     * This will lock the row in the database until the transaction completes,
     * preventing concurrent modifications.
     *
     * @param productCode The product code to find
     * @return The inventory item if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productCode = :productCode")
    Optional<Inventory> findByProductCodeWithLock(@Param("productCode") String productCode);

    boolean existsByProductCode(String productCode);
} 