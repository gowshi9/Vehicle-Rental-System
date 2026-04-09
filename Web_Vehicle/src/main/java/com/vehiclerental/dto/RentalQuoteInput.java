package com.vehiclerental.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record RentalQuoteInput(
    String vehicleClass,
    LocalDateTime pickupAt,
    LocalDateTime dropoffAt,
    BigDecimal baseRatePerDay,
    BigDecimal perKmCharge,
    long expectedKm
) {
    public long rentalDaysRoundedUp() {
        long hours = ChronoUnit.HOURS.between(pickupAt, dropoffAt);
        return Math.max(1, (hours + 23) / 24);
    }
}