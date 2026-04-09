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
public class StripeGateway implements PaymentGateway {

    @Value("${payment.stripe.secret-key}")
    private String secretKey;

    @Value("${payment.stripe.public-key}")
    private String publicKey;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing Stripe payment for amount: {}", request.getAmount());
        
        try {
            // Mock Stripe API call
            String stripeChargeId = "ch_" + UUID.randomUUID().toString().substring(0, 24);
            
            // Simulate card validation
            if (!isValidCard(request.getCardNumber())) {
                return PaymentResponse.builder()
                        .status(Payment.PaymentStatus.FAILED)
                        .message("Invalid card number")
                        .success(false)
                        .processedAt(LocalDateTime.now())
                        .build();
            }
            
            // Simulate processing
            boolean isSuccessful = Math.random() > 0.05; // 95% success rate
            
            return PaymentResponse.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .gatewayTransactionId(stripeChargeId)
                    .status(isSuccessful ? Payment.PaymentStatus.COMPLETED : Payment.PaymentStatus.FAILED)
                    .amount(request.getAmount())
                    .currencyCode(request.getCurrencyCode())
                    .message(isSuccessful ? "Payment processed successfully" : "Card declined")
                    .success(isSuccessful)
                    .processedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Stripe payment failed", e);
            return PaymentResponse.builder()
                    .status(Payment.PaymentStatus.FAILED)
                    .message("Stripe payment failed: " + e.getMessage())
                    .success(false)
                    .processedAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public PaymentResponse refundPayment(String transactionId, BigDecimal amount) {
        log.info("Processing Stripe refund for transaction: {}, amount: {}", transactionId, amount);
        
        String refundId = "re_" + UUID.randomUUID().toString().substring(0, 24);
        
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .gatewayTransactionId(refundId)
                .status(Payment.PaymentStatus.REFUNDED)
                .amount(amount)
                .message("Refund processed successfully")
                .success(true)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse getPaymentStatus(String transactionId) {
        log.info("Checking Stripe payment status for transaction: {}", transactionId);
        
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
        return gatewayType == Payment.GatewayType.STRIPE;
    }

    private boolean isValidCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Remove spaces and dashes
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        // Check if all digits
        if (!cardNumber.matches("\\d+")) {
            return false;
        }
        
        // Luhn algorithm check
        return isValidLuhn(cardNumber);
    }

    private boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }

    public String getCardType(String cardNumber) {
        if (cardNumber == null) return "UNKNOWN";
        
        cardNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        if (cardNumber.startsWith("6")) return "DISCOVER";
        
        return "UNKNOWN";
    }
}