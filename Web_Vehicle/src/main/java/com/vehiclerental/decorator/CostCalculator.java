package com.vehiclerental.decorator;

import com.vehiclerental.dto.RentalQuoteInput;
import java.math.BigDecimal;

public interface CostCalculator {
    BigDecimal cost(RentalQuoteInput input);
}