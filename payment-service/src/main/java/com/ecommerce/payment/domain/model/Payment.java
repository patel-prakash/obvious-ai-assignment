package com.ecommerce.payment.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Product code is required")
    private String productCode;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime timestamp;

    private String failureReason;

    // Reference to locked stock in inventory service
    private String stockLockReferenceId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
} 