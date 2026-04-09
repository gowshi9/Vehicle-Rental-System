package com.vehiclerental.strategy.pricing;

import com.vehiclerental.dto.RentalQuoteInput;
import java.math.BigDecimal;

public interface PricingStrategy {
    String key();
    BigDecimal calculate(RentalQuoteInput input);
}