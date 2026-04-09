package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public class ReturnedState implements RentalState {
    
    @Override
    public void pay(Rental ctx) {
        // Already paid
    }
    
    @Override
    public void start(Rental ctx) {
        throw new IllegalStateException("Cannot start - rental already completed");
    }
    
    @Override
    public void returnVehicle(Rental ctx) {
        // Already returned
    }
    
    @Override
    public void close(Rental ctx) {
        ctx.markClosed();
        ctx.setState(new ClosedState());
    }
    
    @Override
    public String name() {
        return "RETURNED";
    }
}