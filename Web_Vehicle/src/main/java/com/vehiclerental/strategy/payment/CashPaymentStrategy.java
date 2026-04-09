package com.vehiclerental.strategy.payment;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CashPaymentStrategy implements PaymentStrategy {
    
    @Override
    public String key() {
        return "cash";
    }
    
    @Override
    public PaymentResult pay(String rentalId, BigDecimal amount) {
        String reference = "CASH-" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentResult(true, reference, "Cash payment on pickup");
    }
}