package com.vehiclerental.payment.dto;

import com.vehiclerental.common.entity.Payment;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String transactionId;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
    private String currencyCode;
    private String gatewayTransactionId;
    private String redirectUrl; // For PayPal redirects
    private String message;
    private boolean success;
    private LocalDateTime processedAt;
}