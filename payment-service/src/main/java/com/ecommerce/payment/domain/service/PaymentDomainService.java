package com.ecommerce.payment.domain.service;

import com.ecommerce.payment.domain.model.Payment;

public interface PaymentDomainService {

    Payment processPayment(Payment payment);

    Payment getPaymentByTransactionId(String transactionId);
}