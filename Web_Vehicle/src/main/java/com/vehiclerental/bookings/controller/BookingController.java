package com.vehiclerental.bookings.controller;

import com.vehiclerental.bookings.dto.BookingRequest;
import com.vehiclerental.bookings.service.BookingService;
import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Controller
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/bookings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingRequest request, Authentication auth) {
        log.info("Creating booking for vehicle: {} by user: {}", request.getVehicleId(), auth.getName());
        
        try {
            User customer = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Booking booking = bookingService.createBooking(request, customer);
            
            Map<String, Object> response = Map.of(
                "success", true, 
                "message", "Booking created successfully",
                "bookingId", booking.getId(),
                "redirectUrl", "/payment/checkout/" + booking.getId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Booking creation failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Booking failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/customer/bookings")
    public String customerBookings(Model model, Authentication auth) {
        model.addAttribute("title", "My Bookings - DriveEase");
        
        if (auth != null) {
            User customer = userRepository.findByUsername(auth.getName()).orElse(null);
            if (customer != null) {
                var bookings = bookingService.getCustomerBookings(customer);
                log.info("Returning {} bookings for customer {}", bookings == null ? 0 : bookings.size(), customer.getUsername());
                model.addAttribute("bookings", bookings);
            }
        }
        
        // Use the updated dynamic template that iterates over the bookings model
        return "bookings/customer-bookings-new";
    }

    @GetMapping("/customer/bookings/{id}")
    public String customerBookingDetail(@PathVariable Long id, Model model, Authentication auth) {
        model.addAttribute("title", "Booking Details - DriveEase");
        
        if (auth != null) {
            User customer = userRepository.findByUsername(auth.getName()).orElse(null);
            if (customer != null) {
                Booking booking = bookingService.getCustomerBooking(id, customer);
                if (booking != null) {
                    model.addAttribute("booking", booking);
                } else {
                    return "redirect:/customer/bookings";
                }
            }
        }
        
        return "bookings/customer-booking-detail";
    }

    @GetMapping("/booking/create")
    public String createBookingPage(@RequestParam(required = false) Long vehicleId, Model model) {
        model.addAttribute("title", "Book Vehicle - DriveEase");
        
        if (vehicleId != null) {
            Optional<Vehicle> vehicle = vehicleRepository.findById(vehicleId);
            if (vehicle.isPresent()) {
                model.addAttribute("vehicle", vehicle.get());
            }
        }
        
        return "bookings/create";
    }

    @GetMapping("/api/me/bookings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> myBookings(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        }
        var user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "user_not_found"));
        }
        List<com.vehiclerental.common.entity.Booking> bookings = bookingService.getCustomerBookings(user);
        var data = bookings.stream().map(b -> Map.of(
                "id", b.getId(),
                "customerId", b.getCustomer() == null ? null : b.getCustomer().getId(),
                "vehicleId", b.getVehicle() == null ? null : b.getVehicle().getId(),
                "total", b.getTotal(),
                "status", b.getStatus(),
                "paymentStatus", b.getPaymentStatus(),
                "startDate", b.getStartDate(),
                "endDate", b.getEndDate(),
                "createdAt", b.getCreatedAt()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("count", bookings.size(), "bookings", data));
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long id, Authentication auth) {
        try {
            User customer = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            bookingService.cancelBooking(id, customer);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking cancelled successfully"
            ));
        } catch (Exception e) {
            log.error("Booking cancellation failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Cancellation failed: " + e.getMessage()
            ));
        }
    }
}