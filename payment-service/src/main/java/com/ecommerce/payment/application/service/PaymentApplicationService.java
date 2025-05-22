package com.ecommerce.payment.application.service;

import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.StockValidationRequest;
import com.ecommerce.payment.application.dto.StockValidationResponse;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentStatus;
import com.ecommerce.payment.domain.service.PaymentDomainService;
import com.ecommerce.payment.infrastructure.client.InventoryClient;
import com.ecommerce.payment.infrastructure.logging.LoggingUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentApplicationService {


    private static final String PAYMENT_TOPIC = "payment-events";
    private final PaymentDomainService paymentDomainService;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    @CircuitBreaker(name = "inventory", fallbackMethod = "processPaymentWithoutInventoryCheck")
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String correlationId = LoggingUtils.setCorrelationId();
        log.info("Processing payment for order: {}", request.orderId());
        String authToken = extractAuthToken();

        // 1. Validate and lock stock in a single operation
        StockValidationRequest validationRequest = StockValidationRequest.builder()
                .productCode(request.productCode())
                .quantity(request.quantity())
                .build();

        StockValidationResponse validationResponse = inventoryClient.validateStock(validationRequest, authToken);

        // Create payment entity
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .productCode(request.productCode())
                .quantity(request.quantity())
                .amount(request.amount())
                .paymentMode(request.paymentMode())
                .build();

        if (!validationResponse.inStock()) {
            // Stock not available, payment failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient stock for product: " + request.productCode() +
                    ". Available: " + validationResponse.availableQuantity() +
                    ", Requested: " + validationResponse.requestedQuantity());
            payment = paymentDomainService.processPayment(payment);

            LoggingUtils.logTransaction(
                    payment.getTransactionId(),
                    payment.getOrderId(),
                    payment.getAmount().toString(),
                    payment.getStatus().toString(),
                    "Payment failed: Insufficient stock"
            );

            LoggingUtils.clearCorrelationId();
            return mapToResponse(payment);
        }

        if (!validationResponse.locked()) {
            // Could not lock stock, payment failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Failed to lock stock for product: " + request.productCode());
            payment = paymentDomainService.processPayment(payment);

            LoggingUtils.logTransaction(
                    payment.getTransactionId(),
                    payment.getOrderId(),
                    payment.getAmount().toString(),
                    payment.getStatus().toString(),
                    "Payment failed: Unable to lock stock"
            );

            LoggingUtils.clearCorrelationId();
            return mapToResponse(payment);
        }

        try {
            // 2. Process payment
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setStockLockReferenceId(validationResponse.lockReferenceId());
            payment = paymentDomainService.processPayment(payment);

            // Log successful transaction
            LoggingUtils.logTransaction(
                    payment.getTransactionId(),
                    payment.getOrderId(),
                    payment.getAmount().toString(),
                    payment.getStatus().toString(),
                    "Payment successful"
            );

            // 3. Publish payment success event
            PaymentResponse response = mapToResponse(payment);
            kafkaTemplate.send(PAYMENT_TOPIC, response);

            LoggingUtils.clearCorrelationId();
            return response;
        } catch (Exception e) {
            // 4. If payment processing fails, unlock stock
            log.error("Payment processing failed, unlocking stock: {}", validationResponse.lockReferenceId(), e);
            inventoryClient.unlockStock(validationResponse.lockReferenceId(), authToken);

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing error: " + e.getMessage());
            payment = paymentDomainService.processPayment(payment);

            LoggingUtils.logTransaction(
                    payment.getTransactionId(),
                    payment.getOrderId(),
                    payment.getAmount().toString(),
                    payment.getStatus().toString(),
                    "Payment failed: " + e.getMessage()
            );

            LoggingUtils.clearCorrelationId();
            return mapToResponse(payment);
        }
    }

    private String extractAuthToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return "Bearer " + ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
        }
        return null;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrderId())
                .productCode(payment.getProductCode())
                .quantity(payment.getQuantity())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .status(payment.getStatus())
                .timestamp(payment.getTimestamp())
                .failureReason(payment.getFailureReason())
                .stockLockReferenceId(payment.getStockLockReferenceId())
                .build();
    }

    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentDomainService.getPaymentByTransactionId(transactionId);
        return mapToResponse(payment);
    }

    // Fallback method if inventory service is down
    public PaymentResponse processPaymentWithoutInventoryCheck(PaymentRequest request, Exception ex) {
        String correlationId = LoggingUtils.setCorrelationId();
        log.warn("Inventory service is down. Processing payment without stock check. Exception: {}", ex.getMessage());

        // Create payment in pending status
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .productCode(request.productCode())
                .quantity(request.quantity())
                .amount(request.amount())
                .paymentMode(request.paymentMode())
                .status(PaymentStatus.PENDING)
                .failureReason("Inventory service unavailable. Payment will be processed when service is restored.")
                .build();

        payment = paymentDomainService.processPayment(payment);

        LoggingUtils.logTransaction(
                payment.getTransactionId(),
                payment.getOrderId(),
                payment.getAmount().toString(),
                payment.getStatus().toString(),
                "Payment pending: Inventory service unavailable"
        );

        LoggingUtils.clearCorrelationId();
        return mapToResponse(payment);
    }
} 