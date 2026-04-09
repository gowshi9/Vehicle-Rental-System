package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public class ClosedState implements RentalState {
    
    @Override
    public void pay(Rental ctx) {
        throw new IllegalStateException("Rental is closed");
    }
    
    @Override
    public void start(Rental ctx) {
        throw new IllegalStateException("Rental is closed");
    }
    
    @Override
    public void returnVehicle(Rental ctx) {
        throw new IllegalStateException("Rental is closed");
    }
    
    @Override
    public void close(Rental ctx) {
        // Already closed
    }
    
    @Override
    public String name() {
        return "CLOSED";
    }
}