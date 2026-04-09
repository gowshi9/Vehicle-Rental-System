package com.vehiclerental.web;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import com.vehiclerental.services.RentalService;
import com.vehiclerental.auth.service.AuthService;
import com.vehiclerental.bookings.service.BookingService;
import com.vehiclerental.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/system")
public class SystemController {

    @Autowired private UserRepository userRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private RentalService rentalService;
    @Autowired private AuthService authService;
    @Autowired private BookingService bookingService;
    @Autowired private PaymentService paymentService;

    @GetMapping("/dashboard")
    public String systemDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalRentals", rentalRepository.count());
        model.addAttribute("recentUsers", userRepository.findTop5ByOrderByIdDesc());
        model.addAttribute("availableVehicles", vehicleRepository.findByStatus("AVAILABLE"));
        return "system/dashboard";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "system/users";
    }

    @PostMapping("/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vehicles")
    public String manageVehicles(Model model) {
        model.addAttribute("vehicles", vehicleRepository.findAll());
        return "system/vehicles";
    }

    @PostMapping("/vehicles/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<?> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle saved = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/vehicles/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        vehicle.setId(id);
        Vehicle updated = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/vehicles/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        vehicleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "system/bookings";
    }

    @PostMapping("/bookings/create")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/rentals")
    public String manageRentals(Model model) {
        model.addAttribute("rentals", rentalRepository.findAll());
        return "system/rentals";
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public String managePayments(Model model) {
        model.addAttribute("payments", paymentRepository.findAll());
        return "system/payments";
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE')")
    public String analytics(Model model) {
        model.addAttribute("totalRevenue", paymentRepository.findAll().stream()
            .mapToDouble(p -> p.getAmount().doubleValue()).sum());
        model.addAttribute("activeRentals", rentalRepository.countByStatus("ACTIVE"));
        return "system/analytics";
    }
}