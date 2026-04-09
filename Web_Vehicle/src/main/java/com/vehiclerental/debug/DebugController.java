package com.vehiclerental.debug;

import com.vehiclerental.common.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
@Slf4j
public class DebugController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private InspectionRepository inspectionRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> status = Map.of(
                "database", "connected",
                "users", userRepository.count(),
                "vehicles", vehicleRepository.count(),
                "bookings", bookingRepository.count(),
                "payments", paymentRepository.count(),
                "deliveries", deliveryRepository.count(),
                "inspections", inspectionRepository.count(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            log.info("System status check: {}", status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("System status check failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "status", "failed"
            ));
        }
    }
}