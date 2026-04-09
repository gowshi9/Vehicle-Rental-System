package com.vehiclerental.decorator;

import com.vehiclerental.dto.RentalQuoteInput;
import java.math.BigDecimal;

public class LoyaltyDecorator implements CostCalculator {
    
    private final CostCalculator next;
    private final BigDecimal fixedDiscount;
    
    public LoyaltyDecorator(CostCalculator next, BigDecimal fixedDiscount) {
        this.next = next;
        this.fixedDiscount = fixedDiscount;
    }
    
    @Override
    public BigDecimal cost(RentalQuoteInput input) {
        var baseCost = next.cost(input);
        var result = baseCost.subtract(fixedDiscount);
        return result.max(BigDecimal.ZERO); // Don't go below zero
    }
}