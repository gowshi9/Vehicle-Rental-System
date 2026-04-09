package com.vehiclerental.api;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemApiController {

    @Autowired private UserRepository userRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private PaymentRepository paymentRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("totalBookings", bookingRepository.count());
        stats.put("totalRentals", rentalRepository.count());
        stats.put("availableVehicles", vehicleRepository.findByStatus("AVAILABLE").size());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/vehicles/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<?> toggleVehicleAvailability(@PathVariable Long id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle != null) {
            boolean isAvailable = "AVAILABLE".equals(vehicle.getStatus());
            vehicle.setStatus(isAvailable ? "UNAVAILABLE" : "AVAILABLE");
            vehicleRepository.save(vehicle);
            return ResponseEntity.ok(Map.of("available", "AVAILABLE".equals(vehicle.getStatus())));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/bookings/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            return ResponseEntity.ok(booking);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, String>> testDatabaseConnection() {
        try {
            long userCount = userRepository.count();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Database connected successfully",
                "userCount", String.valueOf(userCount)
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Database connection failed: " + e.getMessage()
            ));
        }
    }
}