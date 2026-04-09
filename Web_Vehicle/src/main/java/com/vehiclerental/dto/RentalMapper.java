package com.vehiclerental.dto;

import com.vehiclerental.common.entity.Rental;

public final class RentalMapper {
    
    private RentalMapper() {}
    
    public static RentalDTO toDTO(Rental rental) {
        return new RentalDTO(
            rental.getId(),
            rental.getVehicle().getMake() + " " + rental.getVehicle().getModel() + " (" + rental.getVehicle().getLicensePlate() + ")",
            rental.getCustomer().getUsername(),
            rental.getPickupAt(),
            rental.getDropoffAt(),
            rental.getTotalAmount(),
            rental.getStatus(),
            rental.getPricingStrategy(),
            rental.getPaymentMethod()
        );
    }
}