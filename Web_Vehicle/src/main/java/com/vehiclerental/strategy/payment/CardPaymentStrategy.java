package com.vehiclerental.strategy.payment;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public String key() {
        return "card";
    }
    
    @Override
    public PaymentResult pay(String rentalId, BigDecimal amount) {
        // Simulate card payment processing
        String reference = "CARD-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentResult(true, reference, "Card payment approved");
    }
}