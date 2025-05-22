package com.ecommerce.inventory.infrastructure.logging;

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

    // Define markers for selective logging
    public static final Marker PERFORMANCE_MARKER = MarkerManager.getMarker("PERFORMANCE");
    public static final Marker INVENTORY_MARKER = MarkerManager.getMarker("INVENTORY");
    public static final Marker SECURITY_MARKER = MarkerManager.getMarker("SECURITY");
    public static final Marker INTEGRATION_MARKER = MarkerManager.getMarker("INTEGRATION");

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
     * Sets a specific correlation ID in the MDC context for distributed tracing
     *
     * @param correlationId the correlation ID to set
     * @return the provided correlation ID
     */
    public static String setCorrelationId(String correlationId) {
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
     * Logs inventory operation information with a dedicated marker
     *
     * @param productCode the product code
     * @param operation   the operation (validate, lock, unlock)
     * @param quantity    the quantity
     * @param success     whether the operation was successful
     * @param message     additional message information
     */
    public static void logInventoryOperation(String productCode, String operation, int quantity,
                                             boolean success, String message) {
        MDC.put("productCode", productCode);

        String level = success ? "INFO" : "WARN";
        String status = success ? "SUCCESS" : "FAILED";

        logger.info(INVENTORY_MARKER,
                "INVENTORY | Operation: {} | Product: {} | Quantity: {} | Status: {} | Message: {}",
                operation, productCode, quantity, status, message);

        MDC.remove("productCode");
    }

    /**
     * Logs security information with a dedicated marker
     *
     * @param username the username
     * @param action   the security action (login, logout, access-denied)
     * @param resource the resource being accessed
     * @param message  additional message information
     */
    public static void logSecurityEvent(String username, String action, String resource, String message) {
        MDC.put("username", username);
        MDC.put("action", action);

        logger.info(SECURITY_MARKER,
                "SECURITY | User: {} | Action: {} | Resource: {} | Message: {}",
                username, action, resource, message);

        MDC.remove("username");
        MDC.remove("action");
    }

    /**
     * Logs integration events with external services
     *
     * @param service   the external service name
     * @param operation the operation
     * @param status    the status
     * @param message   additional message information
     */
    public static void logIntegration(String service, String operation, String status, String message) {
        MDC.put("service", service);
        MDC.put("operation", operation);

        logger.info(INTEGRATION_MARKER,
                "INTEGRATION | Service: {} | Operation: {} | Status: {} | Message: {}",
                service, operation, status, message);

        MDC.remove("service");
        MDC.remove("operation");
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