package com.vehiclerental.vehicles.controller;

import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.vehicles.service.VehicleUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private VehicleUpdateService vehicleUpdateService;

    @GetMapping("/vehicles")
    public String showVehicles(Model model) {
        List<Vehicle> vehicles = vehicleRepository.findByStatus("AVAILABLE");
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("title", "Our Fleet - DriveEase");
        return "vehicles/catalog";
    }

    @GetMapping("/api/vehicles")
    @ResponseBody
    public ResponseEntity<List<Vehicle>> getVehiclesApi(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<Vehicle> vehicles;
        
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            vehicles = vehicleRepository.findByStatus("AVAILABLE");
        } else {
            vehicles = vehicleRepository.findByStatus("AVAILABLE");
        }
        
        // Apply filters
        if (make != null && !make.isEmpty()) {
            vehicles = vehicles.stream()
                    .filter(v -> v.getMake().toLowerCase().contains(make.toLowerCase()))
                    .toList();
        }
        
        if (model != null && !model.isEmpty()) {
            vehicles = vehicles.stream()
                    .filter(v -> v.getModel().toLowerCase().contains(model.toLowerCase()))
                    .toList();
        }
        
        if (minPrice != null && maxPrice != null) {
            vehicles = vehicles.stream()
                    .filter(v -> v.getDailyRate().compareTo(minPrice) >= 0 && 
                               v.getDailyRate().compareTo(maxPrice) <= 0)
                    .toList();
        }
        
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/{id}")
    public String showVehicleDetails(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("title", vehicle.getMake() + " " + vehicle.getModel() + " - DriveEase");
        return "vehicles/details";
    }
}