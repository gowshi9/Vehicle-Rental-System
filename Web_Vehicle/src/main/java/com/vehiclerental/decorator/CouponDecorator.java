package com.vehiclerental.decorator;

import com.vehiclerental.dto.RentalQuoteInput;
import java.math.BigDecimal;

public class CouponDecorator implements CostCalculator {
    
    private final CostCalculator next;
    private final BigDecimal discountPercent;
    
    public CouponDecorator(CostCalculator next, BigDecimal discountPercent) {
        this.next = next;
        this.discountPercent = discountPercent;
    }
    
    @Override
    public BigDecimal cost(RentalQuoteInput input) {
        var baseCost = next.cost(input);
        var discount = BigDecimal.ONE.subtract(discountPercent);
        return baseCost.multiply(discount);
    }
}