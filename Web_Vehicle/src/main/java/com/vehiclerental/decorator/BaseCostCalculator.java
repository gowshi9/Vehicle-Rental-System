package com.vehiclerental.decorator;

import com.vehiclerental.dto.RentalQuoteInput;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class BaseCostCalculator implements CostCalculator {
    
    @Override
    public BigDecimal cost(RentalQuoteInput input) {
        return input.baseRatePerDay()
            .multiply(BigDecimal.valueOf(input.rentalDaysRoundedUp()));
    }
}