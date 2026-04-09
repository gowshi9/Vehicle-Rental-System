package com.vehiclerental.bookings.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull
    private Long vehicleId;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private String promoCode;
    
    private String pickupAddress;
    private java.time.LocalTime pickupTime;
    private String dropAddress;
    private java.time.LocalTime dropTime;
}