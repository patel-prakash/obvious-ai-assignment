package com.ecommerce.payment.integration;

import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.StockValidationRequest;
import com.ecommerce.payment.application.dto.StockValidationResponse;
import com.ecommerce.payment.application.service.PaymentApplicationService;
import com.ecommerce.payment.config.TestConfig;
import com.ecommerce.payment.domain.model.PaymentMode;
import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.infrastructure.client.InventoryClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@Import(TestConfig.class)
@Tag("integration")
class PaymentInventoryIntegrationTest {

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Common setup for tests
        // Reset mocks before each test
        reset(inventoryClient, kafkaTemplate);

        // Reset all circuit breakers before each test - use the context to get the registry
        CircuitBreakerRegistry registry = applicationContext.getBean(CircuitBreakerRegistry.class);
        registry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);

        // Mock KafkaTemplate to return a successful CompletableFuture
        CompletableFuture<SendResult<String, PaymentResponse>> future = new CompletableFuture<>();
        // Complete the future successfully to avoid timeouts
        future.complete(Mockito.mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(PaymentResponse.class))).thenReturn(future);

        // Configure mockito to be lenient
        Mockito.lenient();
    }

    @Test
    @DisplayName("Payment should be successful when inventory confirms stock availability")
    void paymentSuccessfulWhenStockAvailable() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId("integration-order-123")
                .productCode("PROD-001")
                .quantity(2)
                .amount(new BigDecimal("100.00"))
                .paymentMode(PaymentMode.CREDIT_CARD)
                .build();

        StockValidationResponse stockValidationResponse = StockValidationResponse.builder()
                .productCode("PROD-001")
                .inStock(true)
                .locked(true)
                .lockReferenceId("integration-lock-ref-123")
                .requestedQuantity(2)
                .availableQuantity(10)
                .build();

        // Use doReturn() instead of when() to avoid issues with matchers
        doReturn(stockValidationResponse)
                .when(inventoryClient).validateStock(any(StockValidationRequest.class), any());

        // Act
        PaymentResponse response = paymentApplicationService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals("PROD-001", response.productCode());
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals("integration-lock-ref-123", response.stockLockReferenceId());
        assertNotNull(response.transactionId());

        // Verify KafkaTemplate was called
        verify(kafkaTemplate).send(eq("payment-events"), any(PaymentResponse.class));
    }

    @Test
    @DisplayName("Payment should fail when inventory reports insufficient stock")
    void paymentFailsWhenInsufficientStock() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId("integration-order-456")
                .productCode("PROD-002")
                .quantity(20)
                .amount(new BigDecimal("2000.00"))
                .paymentMode(PaymentMode.CREDIT_CARD)
                .build();

        StockValidationResponse stockValidationResponse = StockValidationResponse.builder()
                .productCode("PROD-002")
                .inStock(false)
                .locked(false)
                .lockReferenceId(null)
                .requestedQuantity(20)
                .availableQuantity(5)
                .build();

        doReturn(stockValidationResponse)
                .when(inventoryClient).validateStock(any(StockValidationRequest.class), any());

        // Act
        PaymentResponse response = paymentApplicationService.processPayment(request);

        // Assert - note that we now check for PENDING status due to circuit breaker fallback behavior
        assertNotNull(response);
        assertEquals("PROD-002", response.productCode());
        // The test was failing because circuit breaker is open, causing fallback to PENDING
        assertEquals(PaymentStatus.FAILED, response.status());

        // Verify KafkaTemplate was not called
        verify(kafkaTemplate, never()).send(anyString(), any(PaymentResponse.class));
    }


    @Test
    @DisplayName("Payment should be processed with fallback when inventory service is down")
    void paymentFallbackWhenInventoryServiceDown() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .orderId("integration-order-fallback")
                .productCode("PROD-004")
                .quantity(3)
                .amount(new BigDecimal("150.00"))
                .paymentMode(PaymentMode.CREDIT_CARD)
                .build();

        // Simulate inventory service being down by throwing exception on stock validation
        doThrow(new RuntimeException("Inventory service unavailable"))
                .when(inventoryClient).validateStock(any(StockValidationRequest.class), any());

        // Act - this should trigger the fallback method
        PaymentResponse response = paymentApplicationService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.status());
        assertTrue(response.failureReason().contains("Inventory service unavailable"));
    }
} 