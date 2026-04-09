package com.vehiclerental.payment.gateway;

import com.vehiclerental.common.entity.Payment;
import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class MockGateway implements PaymentGateway {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing mock payment for amount: {}", request.getAmount());
        
        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock different scenarios based on card number
        String cardNumber = request.getCardNumber();
        if (cardNumber != null) {
            if (cardNumber.endsWith("0000")) {
                return createFailedResponse("Card declined - insufficient funds");
            }
            if (cardNumber.endsWith("1111")) {
                return createFailedResponse("Card expired");
            }
            if (cardNumber.endsWith("2222")) {
                return createFailedResponse("Invalid CVV");
            }
        }
        
        // Default success response
        return PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .gatewayTransactionId("MOCK_" + UUID.randomUUID().toString().substring(0, 8))
                .status(Payment.PaymentStatus.COMPLETED)
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .message("Payment processed successfully")
                .success(true)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing mock refund for transaction: {}, amount: {}", transactionId, amount);
        
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
        log.info("Checking mock payment status for transaction: {}", transactionId);
        
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(Payment.PaymentStatus.COMPLETED)
                .message("Payment completed")
                .success(true)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean isSupported(Payment.GatewayType gatewayType) {
        return gatewayType == Payment.GatewayType.MOCK_GATEWAY;
    }

    private PaymentResponse createFailedResponse(String message) {
        return PaymentResponse.builder()
                .status(Payment.PaymentStatus.FAILED)
                .message(message)
                .success(false)
                .processedAt(LocalDateTime.now())
                .build();
    }
}