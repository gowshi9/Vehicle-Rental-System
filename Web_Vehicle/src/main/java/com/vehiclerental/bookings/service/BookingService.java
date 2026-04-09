package com.vehiclerental.bookings.service;

import com.vehiclerental.bookings.dto.BookingRequest;
import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import com.vehiclerental.strategy.pricing.PricingStrategy;
import com.vehiclerental.dto.RentalQuoteInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private Map<String, PricingStrategy> pricingStrategies;

    @Autowired
    private com.vehiclerental.payment.service.PaymentService paymentService;

    @Autowired
    private com.vehiclerental.common.repository.PaymentRepository paymentRepository;

    @Transactional
    public Booking createBooking(BookingRequest request, User customer) {
        log.info("Creating booking for vehicle {} by customer {}", request.getVehicleId(), customer.getUsername());
        
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            
        if (!"AVAILABLE".equals(vehicle.getStatus())) {
            throw new RuntimeException("Vehicle is not available");
        }
        
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days <= 0) {
            throw new RuntimeException("Invalid date range");
        }
        
        // Apply pricing strategy
        BigDecimal total = applyPricingStrategy(vehicle, request);
        
        // Apply promotion if provided
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            total = applyPromotion(total, request.getPromoCode());
        }
        
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotal(total);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.UNPAID);
        booking.setPickupAddress(request.getPickupAddress());
        booking.setPickupTime(request.getPickupTime());
        booking.setDropAddress(request.getDropAddress());
        booking.setDropTime(request.getDropTime());
        
        booking = bookingRepository.save(booking);
        log.info("Booking saved with id={} for customerId={}", booking.getId(), customer.getId());
        
        return booking;
    }
    
    private BigDecimal applyPricingStrategy(Vehicle vehicle, BookingRequest request) {
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        return vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
    }
    
    private BigDecimal applyPromotion(BigDecimal total, String promoCode) {
        Optional<Promotion> promoOpt = promotionRepository.findByCodeAndActiveTrue(promoCode);
        if (promoOpt.isEmpty()) {
            return total;
        }
        
        Promotion promo = promoOpt.get();
        BigDecimal discount = total.multiply(BigDecimal.valueOf(promo.getDiscountPercent()).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return total.subtract(discount);
    }

    // --- booking modification helpers ---
    @Transactional
    public Booking updateBookingDates(Long bookingId, User customer, java.time.LocalDate newStart, java.time.LocalDate newEnd) {
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, customer)
                .orElseThrow(() -> new RuntimeException("Booking not found or not owned by user"));
        long days = ChronoUnit.DAYS.between(newStart, newEnd);
        if (days <= 0) throw new RuntimeException("Invalid date range");
        // Check vehicle availability/conflicts (exclude current booking)
        if (booking.getVehicle() != null) {
            var conflicts = bookingRepository.findConflictingBookings(booking.getVehicle().getId(), newStart, newEnd);
            boolean hasOther = conflicts.stream().anyMatch(b -> !b.getId().equals(bookingId));
            if (hasOther) {
                throw new RuntimeException("Vehicle is not available for the selected dates");
            }
        }
        booking.setStartDate(newStart);
        booking.setEndDate(newEnd);
        BookingRequest dummy = new BookingRequest();
        dummy.setStartDate(newStart);
        dummy.setEndDate(newEnd);
        BigDecimal newTotal = applyPricingStrategy(booking.getVehicle(), dummy);
        booking.setTotal(newTotal);
        booking = bookingRepository.save(booking);
        log.info("Booking {} dates updated to {} - {} (total={})", bookingId, newStart, newEnd, newTotal);
        return booking;
    }

    @Transactional
    public Booking adjustBookingDays(Long bookingId, User customer, long deltaDays) {
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, customer)
                .orElseThrow(() -> new RuntimeException("Booking not found or not owned by user"));
        java.time.LocalDate newEnd = booking.getEndDate().plusDays(deltaDays);
        if (!newEnd.isAfter(booking.getStartDate())) throw new RuntimeException("Resulting end date must be after start date");
        // Check availability for the new date range
        if (booking.getVehicle() != null) {
            var conflicts = bookingRepository.findConflictingBookings(booking.getVehicle().getId(), booking.getStartDate(), newEnd);
            boolean hasOther = conflicts.stream().anyMatch(b -> !b.getId().equals(bookingId));
            if (hasOther) {
                throw new RuntimeException("Vehicle is not available for the extended/shortened dates");
            }
        }
        booking.setEndDate(newEnd);
        BookingRequest dummy = new BookingRequest();
        dummy.setStartDate(booking.getStartDate());
        dummy.setEndDate(newEnd);
        BigDecimal newTotal = applyPricingStrategy(booking.getVehicle(), dummy);
        booking.setTotal(newTotal);
        booking = bookingRepository.save(booking);
        log.info("Booking {} adjusted by {} days; new endDate={} total={}", bookingId, deltaDays, newEnd, newTotal);
        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, User customer) {
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, customer)
                .orElseThrow(() -> new RuntimeException("Booking not found or not owned by user"));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
        
        // Cancel associated payments
        List<Payment> payments = paymentRepository.findByBooking(booking);
        for (Payment payment : payments) {
            if (payment.getStatus() == Payment.PaymentStatus.PENDING) {
                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                paymentRepository.save(payment);
            }
        }
        
        booking = bookingRepository.save(booking);
        if (booking.getVehicle() != null) {
            booking.getVehicle().setStatus("AVAILABLE");
            vehicleRepository.save(booking.getVehicle());
        }
        log.info("Booking {} cancelled with payment status updated", bookingId);
        return booking;
    }

    @Transactional
    public boolean refundBooking(Long bookingId, User customer) {
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, customer)
                .orElseThrow(() -> new RuntimeException("Booking not found or not owned by user"));

        if (booking.getPaymentStatus() != Booking.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot refund a booking that is not PAID");
        }

        // Find the last successful payment for this booking
        java.util.List<com.vehiclerental.common.entity.Payment> payments = paymentRepository.findByBooking(booking);
        com.vehiclerental.common.entity.Payment successful = payments.stream()
                .filter(p -> p.getStatus() == com.vehiclerental.common.entity.Payment.PaymentStatus.COMPLETED)
                .findFirst().orElse(null);

        if (successful == null) {
            throw new RuntimeException("No successful payment found to refund");
        }

        // Call the payment service to refund the payment (uses configured gateway)
        var response = paymentService.refundPayment(successful.getId(), successful.getAmount());
        if (response == null || !response.isSuccess()) {
            String msg = response == null ? "Unknown gateway failure" : response.getMessage();
            throw new RuntimeException("Refund failed: " + msg);
        }

        // PaymentService.refundPayment updates payment and booking statuses; log and return
        log.info("Booking {} refunded via gateway, refund txn={}", bookingId, response.getGatewayTransactionId());
        return true;
    }

    @Transactional
    public void deleteBooking(Long bookingId, User customer) {
        Booking booking = bookingRepository.findByIdAndCustomer(bookingId, customer)
                .orElseThrow(() -> new RuntimeException("Booking not found or not owned by user"));
        bookingRepository.delete(booking);
        log.info("Booking {} deleted by user {}", bookingId, customer.getUsername());
    }
    
    public List<Booking> getCustomerBookings(User customer) {
        return bookingRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }
    
    public Booking getCustomerBooking(Long bookingId, User customer) {
        return bookingRepository.findByIdAndCustomer(bookingId, customer).orElse(null);
    }
}