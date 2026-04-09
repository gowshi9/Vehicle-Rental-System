package com.vehiclerental.validation;

import com.vehiclerental.common.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvailabilityCheck implements BookingCheck {
    
    private final RentalRepository rentalRepository;
    
    @Override
    public void check(BookingContext ctx, Runnable next) {
        var conflicts = rentalRepository.findConflictingRentals(
            ctx.getVehicleId(), ctx.getPickupAt(), ctx.getDropoffAt());
            
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Vehicle not available for selected dates");
        }
        next.run();
    }
}