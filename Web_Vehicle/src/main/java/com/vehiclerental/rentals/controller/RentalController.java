package com.vehiclerental.rentals.controller;

import com.vehiclerental.services.RentalService;
import com.vehiclerental.common.repository.RentalRepository;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.repositories.spec.VehicleSpecs;
import static org.springframework.data.jpa.domain.Specification.where;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    
    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    
    @GetMapping
    public String rentalsPage(Model model) {
        model.addAttribute("title", "Rental Management");
        model.addAttribute("rentals", rentalRepository.findAll());
        return "rentals/index";
    }
    
    @GetMapping("/quote")
    @ResponseBody
    public BigDecimal getQuote(@RequestParam String vehicleClass,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupAt,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoffAt,
                              @RequestParam(defaultValue = "0") long expectedKm,
                              @RequestParam(defaultValue = "weekday") String pricing) {
        return rentalService.quote(vehicleClass, pickupAt, dropoffAt, expectedKm, pricing);
    }
    
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createRental(@RequestParam Long vehicleId,
                                         @RequestParam Long customerId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupAt,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoffAt,
                                         @RequestParam(defaultValue = "0") long expectedKm,
                                         @RequestParam(defaultValue = "weekday") String pricing) {
        try {
            var rental = rentalService.createRental(vehicleId, customerId, pickupAt, dropoffAt, expectedKm, pricing);
            return ResponseEntity.ok(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/pay")
    @ResponseBody
    public ResponseEntity<?> processPayment(@PathVariable Long id,
                                           @RequestParam(defaultValue = "cash") String method) {
        try {
            var result = rentalService.processPayment(id, method);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/vehicles/search")
    @ResponseBody
    public ResponseEntity<?> searchVehicles(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) Integer minSeats,
                                           @RequestParam(required = false) java.math.BigDecimal minRate,
                                           @RequestParam(required = false) java.math.BigDecimal maxRate,
                                           @RequestParam(required = false) String fuelType) {
        try {
            var spec = where(VehicleSpecs.categoryIs(category))
                .and(VehicleSpecs.seatsAtLeast(minSeats))
                .and(VehicleSpecs.rateBetween(minRate, maxRate))
                .and(VehicleSpecs.fuelTypeIs(fuelType))
                .and(VehicleSpecs.statusIs("AVAILABLE"));
                
            var vehicles = vehicleRepository.findAll(spec);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/allocate")
    @ResponseBody
    public ResponseEntity<?> allocateVehicle(@RequestParam String category,
                                            @RequestParam(defaultValue = "nearest") String strategy) {
        try {
            var vehicle = rentalService.allocateVehicle(category, strategy);
            return vehicle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/quote/promotions")
    @ResponseBody
    public java.math.BigDecimal getPromotionalQuote(@RequestParam String vehicleClass,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupAt,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dropoffAt,
                                                   @RequestParam(defaultValue = "0") long expectedKm,
                                                   @RequestParam(defaultValue = "false") boolean hasCoupon,
                                                   @RequestParam(defaultValue = "false") boolean isLoyalMember) {
        var vehicle = vehicleRepository.findByCategory(vehicleClass).stream().findFirst()
            .orElseThrow(() -> new RuntimeException("No vehicles available"));
            
        var input = new com.vehiclerental.dto.RentalQuoteInput(
            vehicleClass, pickupAt, dropoffAt, 
            vehicle.getDailyRate(), 
            java.math.BigDecimal.valueOf(2.0), 
            expectedKm
        );
        
        return rentalService.calculateWithPromotions(input, hasCoupon, isLoyalMember);
    }
}