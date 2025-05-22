package com.ecommerce.payment.infrastructure.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for Log4J2 specific features
 */
public class LoggingUtils {

    private static final Logger logger = LogManager.getLogger(LoggingUtils.class);

    // Define marker for selective logging
    public static final Marker PERFORMANCE_MARKER = MarkerManager.getMarker("PERFORMANCE");
    public static final Marker TRANSACTION_MARKER = MarkerManager.getMarker("TRANSACTION");

    /**
     * Sets a correlation ID in the MDC context for request tracking
     *
     * @return the generated correlation ID
     */
    public static String setCorrelationId() {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("correlationId", correlationId);
        return correlationId;
    }

    /**
     * Logs performance information with a dedicated marker
     *
     * @param className       the class name
     * @param methodName      the method name
     * @param executionTimeMs the execution time in milliseconds
     */
    public static void logPerformance(String className, String methodName, long executionTimeMs) {
        logger.info(PERFORMANCE_MARKER,
                "PERFORMANCE | {}.{} | Execution time: {}ms",
                className, methodName, executionTimeMs);

        if (executionTimeMs > 1000) {
            logger.warn(PERFORMANCE_MARKER,
                    "SLOW EXECUTION | {}.{} | Execution time: {}ms",
                    className, methodName, executionTimeMs);
        }
    }

    /**
     * Logs payment transaction information with a dedicated marker
     *
     * @param transactionId the transaction ID
     * @param orderId       the order ID
     * @param amount        the payment amount
     * @param status        the payment status
     * @param message       additional message information
     */
    public static void logTransaction(String transactionId, String orderId, String amount,
                                      String status, String message) {
        MDC.put("transactionId", transactionId);
        MDC.put("orderId", orderId);

        logger.info(TRANSACTION_MARKER,
                "TRANSACTION | ID: {} | Order: {} | Amount: {} | Status: {} | Message: {}",
                transactionId, orderId, amount, status, message);

        MDC.remove("transactionId");
        MDC.remove("orderId");
    }

    /**
     * Clears the correlation ID from MDC when request completes
     */
    public static void clearCorrelationId() {
        MDC.remove("correlationId");
    }

    /**
     * Sets a custom value in the MDC
     *
     * @param key   the MDC key
     * @param value the MDC value
     */
    public static void setMDCValue(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Clears a custom value from MDC
     *
     * @param key the MDC key to clear
     */
    public static void clearMDCValue(String key) {
        MDC.remove(key);
    }
} 