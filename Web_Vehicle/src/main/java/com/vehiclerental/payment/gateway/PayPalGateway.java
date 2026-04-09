package com.vehiclerental.payment.gateway;

import com.vehiclerental.common.entity.Payment;
import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PayPalGateway implements PaymentGateway {

    @Value("${payment.paypal.client-id}")
    private String clientId;

    @Value("${payment.paypal.client-secret}")
    private String clientSecret;

    @Value("${payment.paypal.base-url}")
    private String baseUrl;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing PayPal payment for amount: {}", request.getAmount());
        
        try {
            // Mock PayPal API call
            String paypalTransactionId = "PAYPAL_" + UUID.randomUUID().toString().substring(0, 8);
            String approvalUrl = generatePayPalApprovalUrl(paypalTransactionId, request);
            
            // Simulate PayPal response
            return PaymentResponse.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .gatewayTransactionId(paypalTransactionId)
                    .status(Payment.PaymentStatus.PENDING)
                    .amount(request.getAmount())
                    .currencyCode(request.getCurrencyCode())
                    .redirectUrl(approvalUrl)
                    .message("Redirect to PayPal for approval")
                    .success(true)
                    .processedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("PayPal payment failed", e);
            return PaymentResponse.builder()
                    .status(Payment.PaymentStatus.FAILED)
                    .message("PayPal payment failed: " + e.getMessage())
                    .success(false)
                    .processedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing PayPal refund for transaction: {}, amount: {}", transactionId, amount);
        
        // Mock refund process
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .gatewayTransactionId("REFUND_" + UUID.randomUUID().toString().substring(0, 8))
                .status(Payment.PaymentStatus.REFUNDED)
                .amount(amount)
                .message("Refund processed successfully")
                .success(true)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("Checking PayPal payment status for transaction: {}", transactionId);
        
        // Mock status check - randomly return completed or failed
        boolean isCompleted = Math.random() > 0.1; // 90% success rate
        
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(isCompleted ? Payment.PaymentStatus.COMPLETED : Payment.PaymentStatus.FAILED)
                .message(isCompleted ? "Payment completed" : "Payment failed")
                .success(isCompleted)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean isSupported(Payment.GatewayType gatewayType) {
        return gatewayType == Payment.GatewayType.PAYPAL;
    }

    private String generatePayPalApprovalUrl(String paypalTransactionId, PaymentRequest request) {
        // Mock PayPal approval URL
        return String.format("/payment/paypal/approve?token=%s&amount=%s&booking=%d", 
                paypalTransactionId, request.getAmount(), request.getBookingId());
    }

    public PaymentResponse handleApproval(String token, String payerId) {
        log.info("Handling PayPal approval for token: {}, payer: {}", token, payerId);
        
        // Mock approval handling
        return PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .gatewayTransactionId(token)
                .status(Payment.PaymentStatus.COMPLETED)
                .message("Payment approved and completed")
                .success(true)
                .processedAt(LocalDateTime.now())
                .build();
    }
}