package com.vehiclerental.memento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalSnapshot(
    BigDecimal dailyRate,
    BigDecimal kmCharge,
    BigDecimal taxPercent,
    String termsVersion,
    String pricingStrategy,
    LocalDateTime capturedAt
) {
    
    public static RentalSnapshot capture(BigDecimal dailyRate, BigDecimal kmCharge, String pricingStrategy) {
        return new RentalSnapshot(
            dailyRate,
            kmCharge,
            new BigDecimal("0.08"), // 8% tax
            "v2024.1",
            pricingStrategy,
            LocalDateTime.now()
        );
    }
}