package com.vehiclerental.delivery.service;

import com.vehiclerental.common.entity.Booking;
import com.vehiclerental.common.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryScheduler {

    private final BookingRepository bookingRepository;
    private final DeliveryService deliveryService;

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void processBookingsForDelivery() {
        try {
            // Find paid bookings without deliveries - create delivery records for admin assignment
            List<Booking> paidBookings = bookingRepository.findByPaymentStatusAndDeliveryIsNull(
                Booking.PaymentStatus.PAID);
            
            for (Booking booking : paidBookings) {
                deliveryService.createPendingDelivery(booking);
                log.info("Created delivery record for paid booking {} - awaiting admin assignment", booking.getId());
            }
            
        } catch (Exception e) {
            log.error("Error processing bookings for delivery: {}", e.getMessage());
        }
    }
}