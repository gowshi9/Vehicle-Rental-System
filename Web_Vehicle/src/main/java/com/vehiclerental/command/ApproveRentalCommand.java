package com.vehiclerental.command;

import com.vehiclerental.common.entity.Rental;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApproveRentalCommand implements Command<Boolean> {
    
    private final Rental rental;
    
    @Override
    public Boolean execute() {
        rental.pay();
        return true;
    }
    
    @Override
    public String description() {
        return "Approve rental #" + rental.getId();
    }
}