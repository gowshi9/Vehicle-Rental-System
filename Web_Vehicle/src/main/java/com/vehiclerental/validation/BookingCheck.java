package com.vehiclerental.validation;

public interface BookingCheck {
    void check(BookingContext ctx, Runnable next);
}