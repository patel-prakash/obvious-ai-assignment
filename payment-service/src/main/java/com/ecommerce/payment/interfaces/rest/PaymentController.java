package com.ecommerce.payment.interfaces.rest;

import com.ecommerce.payment.application.dto.PaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.service.PaymentApplicationService;
import com.ecommerce.payment.domain.model.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentApplicationService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "payment", fallbackMethod = "processPaymentFallback")
    @PreAuthorize("hasRole('PAYMENT_WRITE')")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @GetMapping("/{transactionId}")
    @CircuitBreaker(name = "payment", fallbackMethod = "getPaymentDetailsFallback")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getPaymentByTransactionId(transactionId));
    }

    // Fallback methods

    public ResponseEntity<PaymentResponse> processPaymentFallback(PaymentRequest request, Exception ex) {
        log.error("Circuit breaker triggered for payment processing: {}", ex.getMessage());

        PaymentResponse degradedResponse = PaymentResponse.builder()
                .transactionId("CIRCUIT_OPEN_" + System.currentTimeMillis())
                .orderId(request.orderId())
                .productCode(request.productCode())
                .quantity(request.quantity())
                .amount(request.amount())
                .paymentMode(request.paymentMode())
                .status(PaymentStatus.PENDING)
                .failureReason("Payment service temporarily unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(degradedResponse);
    }

    public ResponseEntity<PaymentResponse> getPaymentDetailsFallback(String transactionId, Exception ex) {
        log.error("Circuit breaker triggered for payment details retrieval: {}", ex.getMessage());

        PaymentResponse degradedResponse = PaymentResponse.builder()
                .transactionId(transactionId)
                .status(PaymentStatus.UNKNOWN)
                .failureReason("Payment service temporarily unavailable: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(degradedResponse);
    }
}