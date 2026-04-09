package com.vehiclerental.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalDTO(
    Long id,
    String vehicleInfo,
    String customerName,
    LocalDateTime pickupAt,
    LocalDateTime dropoffAt,
    BigDecimal totalAmount,
    String status,
    String pricingStrategy,
    String paymentMethod
) {}