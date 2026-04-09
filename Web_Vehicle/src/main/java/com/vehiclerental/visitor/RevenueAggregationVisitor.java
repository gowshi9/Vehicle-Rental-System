package com.vehiclerental.visitor;

import com.vehiclerental.common.entity.Rental;
import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.entity.User;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class RevenueAggregationVisitor implements ReportVisitor {
    
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private int rentalCount = 0;
    private int vehicleCount = 0;
    private int userCount = 0;
    
    @Override
    public void visit(Rental rental) {
        if ("CLOSED".equals(rental.getStatus())) {
            totalRevenue = totalRevenue.add(rental.getTotalAmount());
        }
        rentalCount++;
    }
    
    @Override
    public void visit(Vehicle vehicle) {
        vehicleCount++;
    }
    
    @Override
    public void visit(User user) {
        userCount++;
    }
    
    public BigDecimal getAverageRentalValue() {
        return rentalCount > 0 ? totalRevenue.divide(BigDecimal.valueOf(rentalCount), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}