package com.vehiclerental.strategy.pricing;

import com.vehiclerental.dto.RentalQuoteInput;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
public class WeekdayPricing implements PricingStrategy {
    
    @Override
    public String key() {
        return "weekday";
    }
    
    @Override
    public BigDecimal calculate(RentalQuoteInput input) {
        var factor = switch (input.pickupAt().getDayOfWeek()) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> BigDecimal.valueOf(0.95);
            default -> BigDecimal.valueOf(1.0);
        };
        
        var baseCost = input.baseRatePerDay()
            .multiply(BigDecimal.valueOf(input.rentalDaysRoundedUp()))
            .multiply(factor);
            
        var kmCost = input.perKmCharge()
            .multiply(BigDecimal.valueOf(input.expectedKm()));
            
        return baseCost.add(kmCost);
    }
}