package com.vehiclerental.delivery.controller;

import com.vehiclerental.common.entity.Delivery;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('DELIVERY')")
    public String dashboard(Model model, Authentication auth) {
        List<Delivery> myDeliveries = deliveryService.getDriverDeliveries(auth.getName());
        
        model.addAttribute("title", "Delivery Dashboard - DriveEase");
        model.addAttribute("deliveries", myDeliveries);
        
        return "delivery/dashboard";
    }

    @PostMapping("/confirm/{deliveryId}")
    @PreAuthorize("hasRole('DELIVERY')")
    public String confirmDelivery(@PathVariable Long deliveryId, 
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            deliveryService.confirmDelivery(deliveryId, auth.getName());
            redirectAttributes.addFlashAttribute("success", "Delivery confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to confirm delivery: " + e.getMessage());
        }
        
        return "redirect:/delivery/dashboard";
    }

    @PostMapping("/cancel/{deliveryId}")
    @PreAuthorize("hasRole('DELIVERY')")
    public String cancelDelivery(@PathVariable Long deliveryId,
                               @RequestParam String reason,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            deliveryService.cancelDelivery(deliveryId, auth.getName(), reason);
            redirectAttributes.addFlashAttribute("success", "Delivery cancelled. Admin will reassign.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to cancel delivery: " + e.getMessage());
        }
        
        return "redirect:/delivery/dashboard";
    }
}