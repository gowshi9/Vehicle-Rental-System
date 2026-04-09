package com.vehiclerental.payment.gateway;

import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.dto.PaymentResponse;

public interface PaymentGateway {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse refundPayment(String transactionId, java.math.BigDecimal amount);
    PaymentResponse getPaymentStatus(String transactionId);
    boolean isSupported(com.vehiclerental.common.entity.Payment.GatewayType gatewayType);
}