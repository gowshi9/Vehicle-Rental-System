package com.vehiclerental.mediator;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequest {
    private Long customerId;
    private String vehicleCategory;
    private LocalDateTime pickupAt;
    private LocalDateTime dropoffAt;
    private long expectedKm;
    private String pricingStrategy;
    private String paymentMethod;
    private String allocationStrategy;
}