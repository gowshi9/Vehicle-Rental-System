package com.vehiclerental.strategy.pricing;

import com.vehiclerental.dto.RentalQuoteInput;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Month;

@Component
public class SeasonalPricing implements PricingStrategy {
    
    @Override
    public String key() {
        return "seasonal";
    }
    
    @Override
    public BigDecimal calculate(RentalQuoteInput input) {
        var month = input.pickupAt().getMonth();
        var factor = switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> BigDecimal.valueOf(1.25); // Winter premium
            case JUNE, JULY, AUGUST -> BigDecimal.valueOf(1.20); // Summer premium
            case MARCH, APRIL, MAY, SEPTEMBER, OCTOBER, NOVEMBER -> BigDecimal.valueOf(1.0); // Regular
        };
        
        var baseCost = input.baseRatePerDay()
            .multiply(BigDecimal.valueOf(input.rentalDaysRoundedUp()))
            .multiply(factor);
            
        var kmCost = input.perKmCharge()
            .multiply(BigDecimal.valueOf(input.expectedKm()));
            
        return baseCost.add(kmCost);
    }
}