package com.vehiclerental.validation;

import org.springframework.stereotype.Component;

@Component
public class LicenseValidCheck implements BookingCheck {
    
    @Override
    public void check(BookingContext ctx, Runnable next) {
        if (!ctx.isLicenseValid()) {
            throw new IllegalStateException("Invalid or expired license");
        }
        next.run();
    }
}