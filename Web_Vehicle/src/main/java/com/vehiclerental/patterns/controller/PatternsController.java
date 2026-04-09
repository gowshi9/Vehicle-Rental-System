package com.vehiclerental.patterns.controller;

import com.vehiclerental.notification.bridge.BasicNotification;
import com.vehiclerental.notification.bridge.EmailSender;
import com.vehiclerental.notification.bridge.SmsSender;
import com.vehiclerental.flyweight.VehicleSpecFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patterns")
@RequiredArgsConstructor
public class PatternsController {
    
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final VehicleSpecFactory vehicleSpecFactory;
    
    @GetMapping
    public String patternsDemo(Model model) {
        model.addAttribute("title", "Design Patterns Demo");
        return "patterns/index";
    }
}

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
class PatternsApiController {
    
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final VehicleSpecFactory vehicleSpecFactory;
    private final com.vehiclerental.common.repository.RentalRepository rentalRepository;
    private final com.vehiclerental.common.repository.VehicleRepository vehicleRepository;
    private final com.vehiclerental.common.repository.UserRepository userRepository;
    
    @PostMapping("/notification")
    public String sendNotification(@RequestParam String type,
                                  @RequestParam String to,
                                  @RequestParam String subject,
                                  @RequestParam String body) {
        
        var sender = "sms".equals(type) ? smsSender : emailSender;
        var notification = new BasicNotification(sender);
        
        notification.send(to, subject, body);
        
        return "Notification sent via " + sender.getType() + " to " + to;
    }
    
    @GetMapping("/flyweight")
    public String testFlyweight(@RequestParam String spec) {
        String[] parts = spec.split("\\|");
        if (parts.length >= 6) {
            var vehicleSpec = vehicleSpecFactory.get(
                parts[0], parts[1], parts[2], 
                Integer.parseInt(parts[3]), parts[4], parts[5]
            );
            return "Retrieved spec: " + vehicleSpec.toString();
        }
        return "Invalid spec format";
    }
    
    @GetMapping("/visitor/revenue")
    public java.util.Map<String, Object> generateRevenueReport() {
        var visitor = new com.vehiclerental.visitor.RevenueAggregationVisitor();
        
        // Visit all entities
        rentalRepository.findAll().forEach(rental -> rental.accept(visitor));
        vehicleRepository.findAll().forEach(vehicle -> vehicle.accept(visitor));
        userRepository.findAll().forEach(user -> user.accept(visitor));
        
        return java.util.Map.of(
            "totalRevenue", visitor.getTotalRevenue(),
            "rentalCount", visitor.getRentalCount(),
            "vehicleCount", visitor.getVehicleCount(),
            "userCount", visitor.getUserCount(),
            "averageRentalValue", visitor.getAverageRentalValue(),
            "timestamp", java.time.LocalDateTime.now()
        );
    }
}