package com.vehiclerental.validation;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingContext {
    private Long vehicleId;
    private Long customerId;
    private LocalDateTime pickupAt;
    private LocalDateTime dropoffAt;
    private String licenseNumber;
    private boolean licenseValid;
    private boolean hasDeposit;
}