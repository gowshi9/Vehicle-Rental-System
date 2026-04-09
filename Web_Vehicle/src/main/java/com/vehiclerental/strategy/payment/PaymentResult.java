package com.vehiclerental.strategy.payment;

public record PaymentResult(
    boolean success,
    String reference,
    String message
) {}