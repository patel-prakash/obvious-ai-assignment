package com.ecommerce.payment.infrastructure.client;

import com.ecommerce.payment.infrastructure.logging.LoggingUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for external payment gateway interactions with circuit breaker protection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    /**
     * Process payment through the payment gateway with circuit breaker protection.
     *
     * @param merchantId Merchant ID
     * @param amount     Payment amount
     * @param currency   Payment currency
     * @param cardToken  Tokenized card information
     * @return Transaction ID if successful
     */
    @CircuitBreaker(name = "payment_gateway", fallbackMethod = "processPaymentFallback")
    public String processPayment(String merchantId, double amount, String currency, String cardToken) {
        log.info("Processing payment of {} {} through payment gateway", amount, currency);

        // In a real implementation, this would call the external payment gateway API
        // For demonstration purposes, we'll simulate a successful response

        // Simulate latency
        try {
            // Simulate random latency between 100ms and 300ms
            Thread.sleep((long) (Math.random() * 200 + 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate a transaction ID
        String transactionId = UUID.randomUUID().toString();
        log.info("Payment processed successfully with transaction ID: {}", transactionId);

        return transactionId;
    }

    /**
     * Fallback method for payment processing when the circuit breaker is open.
     *
     * @param merchantId Merchant ID
     * @param amount     Payment amount
     * @param currency   Payment currency
     * @param cardToken  Tokenized card information
     * @param ex         Exception that triggered the fallback
     * @return Fallback transaction ID
     */
    public String processPaymentFallback(String merchantId, double amount, String currency, String cardToken, Exception ex) {
        String fallbackTransactionId = "FB-" + UUID.randomUUID().toString();
        log.warn("Payment gateway unavailable. Using fallback for payment of {} {}. Error: {}",
                amount, currency, ex.getMessage());

        // Log the fallback transaction for reconciliation
        LoggingUtils.logTransaction(
                fallbackTransactionId,
                "FALLBACK",
                String.valueOf(amount),
                "PENDING",
                "Payment gateway unavailable: " + ex.getMessage()
        );

        return fallbackTransactionId;
    }

    /**
     * Verify payment status with circuit breaker protection.
     *
     * @param transactionId Transaction ID to verify
     * @return Payment status
     */
    @CircuitBreaker(name = "payment_gateway", fallbackMethod = "verifyPaymentStatusFallback")
    public String verifyPaymentStatus(String transactionId) {
        log.info("Verifying payment status for transaction: {}", transactionId);

        // In a real implementation, this would call the external payment gateway API
        // For demonstration purposes, we'll simulate a successful response

        // Simulate latency
        try {
            // Simulate random latency between 50ms and 150ms
            Thread.sleep((long) (Math.random() * 100 + 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "COMPLETED";
    }

    /**
     * Fallback method for payment verification when the circuit breaker is open.
     *
     * @param transactionId Transaction ID to verify
     * @param ex            Exception that triggered the fallback
     * @return Fallback status
     */
    public String verifyPaymentStatusFallback(String transactionId, Exception ex) {
        log.warn("Payment gateway unavailable for status verification of transaction {}. Error: {}",
                transactionId, ex.getMessage());

        // Return a "UNKNOWN" status when the payment gateway is unavailable
        // The application should handle this gracefully
        return "UNKNOWN";
    }
} 