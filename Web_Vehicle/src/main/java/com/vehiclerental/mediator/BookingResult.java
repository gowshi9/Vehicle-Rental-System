package com.vehiclerental.mediator;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingResult {
    private Long rentalId;
    private BigDecimal totalAmount;
    private String paymentReference;
    private boolean success;
    private String message;
    
    public static BookingResult success(Long rentalId, BigDecimal total, String paymentRef) {
        return new BookingResult(rentalId, total, paymentRef, true, "Booking successful");
    }
    
    public static BookingResult failure(String message) {
        return new BookingResult(null, null, null, false, message);
    }
}