package com.vehiclerental.strategy.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String key();
    PaymentResult pay(String rentalId, BigDecimal amount);
}