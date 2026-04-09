package com.vehiclerental.vehicles.controller;

import com.vehiclerental.vehicles.service.VehicleUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class VehicleUpdateController {
    
    @Autowired
    private VehicleUpdateService vehicleUpdateService;
    
    @GetMapping("/api/vehicles/updates")
    public SseEmitter subscribeToVehicleUpdates() {
        return vehicleUpdateService.subscribe();
    }
}