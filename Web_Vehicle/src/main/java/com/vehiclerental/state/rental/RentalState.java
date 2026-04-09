package com.vehiclerental.state.rental;

import com.vehiclerental.common.entity.Rental;

public interface RentalState {
    void pay(Rental ctx);
    void start(Rental ctx);
    void returnVehicle(Rental ctx);
    void close(Rental ctx);
    String name();
}