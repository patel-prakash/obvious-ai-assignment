package com.ecommerce.payment.infrastructure.client;

import com.ecommerce.payment.application.dto.StockValidationRequest;
import com.ecommerce.payment.application.dto.StockValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class InventoryClientImpl implements InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${service.inventory.baseUrl:http://inventory-service}")
    private String baseUrl;

    @Value("${service.inventory.apiPath:/api/inventory}")
    private String apiPath;

    public InventoryClientImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public StockValidationResponse validateStock(StockValidationRequest request, String authToken) {
        log.info("Validating and locking stock for product: {} with quantity: {}", request.productCode(), request.quantity());

        String inventoryUrl = baseUrl + apiPath + "/validate";

        WebClient.RequestHeadersSpec<?> requestSpec = webClientBuilder.build()
                .post()
                .uri(inventoryUrl)
                .bodyValue(request);

        // Add the Authorization header if token is available
        if (authToken != null) {
            requestSpec = requestSpec.header("Authorization", "Bearer " + authToken);
        }

        return requestSpec
                .retrieve()
                .bodyToMono(StockValidationResponse.class)
                .onErrorResume(error -> {
                    log.error("Error validating stock: {}", error.getMessage());
                    return Mono.just(StockValidationResponse.builder()
                            .productCode(request.productCode())
                            .inStock(false)
                            .locked(false)
                            .lockReferenceId(null)
                            .availableQuantity(0)
                            .requestedQuantity(request.quantity())
                            .build());
                })
                .block();
    }

    @Override
    public boolean unlockStock(String lockReferenceId, String authToken) {
        log.info("Unlocking stock with reference ID: {}", lockReferenceId);

        String inventoryUrl = baseUrl + apiPath + "/unlock/" + lockReferenceId;

        WebClient.RequestHeadersSpec<?> requestSpec = webClientBuilder.build()
                .post()
                .uri(inventoryUrl);

        // Add the Authorization header if token is available
        if (authToken != null) {
            requestSpec = requestSpec.header("Authorization", "Bearer " + authToken);
        }

        return requestSpec
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(error -> {
                    log.error("Error unlocking stock: {}", error.getMessage());
                    return Mono.just(false);
                })
                .block();
    }
} 