package com.vehiclerental.payment.dto;

import com.vehiclerental.common.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    @NotNull(message = "Gateway type is required")
    private Payment.GatewayType gatewayType;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currencyCode = "USD";

    // Credit Card fields
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;

    // PayPal fields
    private String paypalEmail;
    private String returnUrl;
    private String cancelUrl;

    // Saved card
    private Long savedCardId;

    private boolean saveCard = false;
}