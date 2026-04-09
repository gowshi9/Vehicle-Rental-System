package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public class PaidState implements RentalState {
    
    @Override
    public void pay(Rental ctx) {
        // Idempotent - already paid
    }
    
    @Override
    public void start(Rental ctx) {
        ctx.markActive();
        ctx.setState(new ActiveState());
    }
    
    @Override
    public void returnVehicle(Rental ctx) {
        throw new IllegalStateException("Cannot return vehicle - rental not active");
    }
    
    @Override
    public void close(Rental ctx) {
        throw new IllegalStateException("Cannot close rental - not completed");
    }
    
    @Override
    public String name() {
        return "PAID";
    }
}