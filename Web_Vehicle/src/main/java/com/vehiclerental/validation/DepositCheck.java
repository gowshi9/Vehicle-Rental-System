package com.vehiclerental.validation;

import org.springframework.stereotype.Component;

@Component
public class DepositCheck implements BookingCheck {
    
    @Override
    public void check(BookingContext ctx, Runnable next) {
        if (!ctx.isHasDeposit()) {
            throw new IllegalStateException("Security deposit required");
        }
        next.run();
    }
}