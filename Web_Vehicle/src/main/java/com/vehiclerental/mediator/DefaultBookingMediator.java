package com.vehiclerental.mediator;

import com.vehiclerental.services.RentalService;
import com.vehiclerental.validation.CheckChain;
import com.vehiclerental.validation.BookingContext;
import com.vehiclerental.notification.bridge.Notification;
import com.vehiclerental.notification.bridge.BasicNotification;
import com.vehiclerental.notification.bridge.EmailSender;
import com.vehiclerental.memento.RentalSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultBookingMediator implements BookingMediator {
    
    private final RentalService rentalService;
    private final CheckChain checkChain;
    private final EmailSender emailSender;
    private final ObjectMapper objectMapper;
    
    @Override
    public BookingResult book(BookingRequest request) {
        try {
            log.info("Starting booking process for customer: {}", request.getCustomerId());
            
            // 1. Validation Pipeline
            var context = new BookingContext(
                null, // Will be set after allocation
                request.getCustomerId(),
                request.getPickupAt(),
                request.getDropoffAt(),
                "DL123456", // Mock license
                true, // Valid license
                true  // Has deposit
            );
            
            // 2. Vehicle Allocation
            var vehicle = rentalService.allocateVehicle(request.getVehicleCategory(), request.getAllocationStrategy())
                .orElseThrow(() -> new RuntimeException("No vehicles available"));
            
            context.setVehicleId(vehicle.getId());
            checkChain.run(context);
            
            // 3. Create Rental with Snapshot
            var rental = rentalService.createRental(
                vehicle.getId(),
                request.getCustomerId(),
                request.getPickupAt(),
                request.getDropoffAt(),
                request.getExpectedKm(),
                request.getPricingStrategy()
            );
            
            // 4. Capture Contract Snapshot
            var snapshot = RentalSnapshot.capture(
                vehicle.getDailyRate(),
                java.math.BigDecimal.valueOf(2.0),
                request.getPricingStrategy()
            );
            try {
                rental.setSnapshotJson(objectMapper.writeValueAsString(snapshot));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize rental snapshot: {}", e.getMessage());
            }
            
            // 5. Process Payment
            var paymentResult = rentalService.processPayment(rental.getId(), request.getPaymentMethod());
            
            // 6. Send Notification
            Notification notification = new BasicNotification(emailSender);
            notification.send(
                "customer@example.com",
                "Booking Confirmation",
                "Your rental #" + rental.getId() + " has been confirmed. Total: $" + rental.getTotalAmount()
            );
            
            log.info("Booking completed successfully: {}", rental.getId());
            return BookingResult.success(rental.getId(), rental.getTotalAmount(), paymentResult.reference());
            
        } catch (Exception e) {
            log.error("Booking failed: {}", e.getMessage(), e);
            return BookingResult.failure("Booking failed: " + e.getMessage());
        }
    }
}