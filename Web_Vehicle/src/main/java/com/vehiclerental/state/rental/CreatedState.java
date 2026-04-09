package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public class CreatedState implements RentalState {
    
    @Override
    public void pay(Rental ctx) {
        ctx.markPaid();
        ctx.setState(new PaidState());
    }
    
    @Override
    public void start(Rental ctx) {
        throw new IllegalStateException("Cannot start rental - payment required first");
    }
    
    @Override
    public void returnVehicle(Rental ctx) {
        throw new IllegalStateException("Cannot return vehicle - rental not started");
    }
    
    @Override
    public void close(Rental ctx) {
        throw new IllegalStateException("Cannot close rental - not completed");
    }
    
    @Override
    public String name() {
        return "CREATED";
    }
}