package com.ecommerce.inventory.domain.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response model for API exceptions.
 * Used to provide consistent error responses across the application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errorCode,
        String message,
        String detailedMessage,
        List<ValidationError> validationErrors,
        HttpStatus status,
        int statusCode,
        String path,
        LocalDateTime timestamp
) {
    /**
     * Represents a validation error for a specific field.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ValidationError(
            String field,
            String message
    ) {
    }

    /**
     * Builder for creating ErrorResponse instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String errorCode;
        private String message;
        private String detailedMessage;
        private List<ValidationError> validationErrors;
        private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        private String path;
        private LocalDateTime timestamp = LocalDateTime.now();

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder detailedMessage(String detailedMessage) {
            this.detailedMessage = detailedMessage;
            return this;
        }

        public Builder validationErrors(List<ValidationError> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(
                    errorCode,
                    message,
                    detailedMessage,
                    validationErrors,
                    status,
                    status.value(),
                    path,
                    timestamp
            );
        }
    }
} 