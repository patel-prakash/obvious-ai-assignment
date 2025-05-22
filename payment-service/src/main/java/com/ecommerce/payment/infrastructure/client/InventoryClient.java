package com.ecommerce.payment.infrastructure.client;

import com.ecommerce.payment.application.dto.StockValidationRequest;
import com.ecommerce.payment.application.dto.StockValidationResponse;

public interface InventoryClient {

    /**
     * Validates if a product is in stock with the requested quantity
     * and locks the stock if available
     *
     * @param request   The stock validation request
     * @param authToken The authentication token
     * @return The stock validation response
     */
    StockValidationResponse validateStock(StockValidationRequest request, String authToken);

    /**
     * Unlocks previously locked stock
     *
     * @param lockReferenceId The lock reference ID
     * @param authToken       The authentication token
     * @return true if unlocked successfully, false otherwise
     */
    boolean unlockStock(String lockReferenceId, String authToken);
} 