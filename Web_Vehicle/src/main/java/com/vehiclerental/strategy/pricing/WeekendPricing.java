package com.vehiclerental.strategy.pricing;

import com.vehiclerental.dto.RentalQuoteInput;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class WeekendPricing implements PricingStrategy {
    
    @Override
    public String key() {
        return "weekend";
    }
    
    @Override
    public BigDecimal calculate(RentalQuoteInput input) {
        var factor = switch (input.pickupAt().getDayOfWeek()) {
            case FRIDAY, SATURDAY, SUNDAY -> BigDecimal.valueOf(1.15);
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