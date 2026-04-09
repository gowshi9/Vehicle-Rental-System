package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public class ActiveState implements RentalState {
    
    @Override
    public void pay(Rental ctx) {
        // Already paid
    }
    
    @Override
    public void start(Rental ctx) {
        // Already started
    }
    
    @Override
    public void returnVehicle(Rental ctx) {
        ctx.markReturned();
        ctx.setState(new ReturnedState());
    }
    
    @Override
    public void close(Rental ctx) {
        throw new IllegalStateException("Cannot close rental - vehicle not returned");
    }
    
    @Override
    public String name() {
        return "ACTIVE";
    }
}