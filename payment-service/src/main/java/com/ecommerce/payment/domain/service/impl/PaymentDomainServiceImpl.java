package com.ecommerce.payment.domain.service.impl;

import com.ecommerce.payment.domain.exception.PaymentNotFoundException;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.domain.service.PaymentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentDomainServiceImpl implements PaymentDomainService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment processPayment(Payment payment) {
        // Generate transaction ID if not provided
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId(UUID.randomUUID().toString());
        }

        // Set the timestamp
        payment.setTimestamp(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId, "Payment not found"));
    }

}