package com.vehiclerental.cqrs.command;

import com.vehiclerental.mediator.BookingMediator;
import com.vehiclerental.mediator.BookingRequest;
import com.vehiclerental.mediator.BookingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/c")
@RequiredArgsConstructor
public class BookingCommandController {
    
    private final BookingMediator bookingMediator;
    
    @PostMapping("/book")
    public ResponseEntity<BookingResult> createBooking(
            @RequestParam Long customerId,
            @RequestParam String vehicleCategory,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoffAt,
            @RequestParam(defaultValue = "0") long expectedKm,
            @RequestParam(defaultValue = "weekday") String pricingStrategy,
            @RequestParam(defaultValue = "card") String paymentMethod,
            @RequestParam(defaultValue = "nearest") String allocationStrategy) {
        
        var request = new BookingRequest(
            customerId, vehicleCategory, pickupAt, dropoffAt,
            expectedKm, pricingStrategy, paymentMethod, allocationStrategy
        );
        
        var result = bookingMediator.book(request);
        
        return result.isSuccess() 
            ? ResponseEntity.ok(result)
            : ResponseEntity.badRequest().body(result);
    }
}