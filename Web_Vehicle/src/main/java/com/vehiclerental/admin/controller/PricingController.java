package com.vehiclerental.admin.controller;

import com.vehiclerental.strategy.pricing.PricingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/admin/pricing")
public class PricingController {

    @Autowired
    private Map<String, PricingStrategy> pricingStrategies;
    
    @Autowired
    private com.vehiclerental.common.repository.PromotionRepository promotionRepository;

    @GetMapping
    public String pricingManagement(Model model) {
        System.out.println("PricingController accessed");
        System.out.println("Strategies available: " + (pricingStrategies != null ? pricingStrategies.size() : "null"));
        
        model.addAttribute("title", "Pricing Strategy Management - Admin");
        model.addAttribute("strategies", pricingStrategies != null ? pricingStrategies : java.util.Collections.emptyMap());
        model.addAttribute("promotions", promotionRepository.findAll());
        
        return "admin/pricing-simple";
    }

    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> testPricing(@RequestParam String strategy,
                                          @RequestParam BigDecimal baseRate,
                                          @RequestParam String date) {
        PricingStrategy pricingStrategy = pricingStrategies.get(strategy);
        if (pricingStrategy != null) {
            // Create test input
            var testInput = new com.vehiclerental.dto.RentalQuoteInput(
                "SEDAN", // vehicleClass
                java.time.LocalDateTime.parse(date + "T12:00:00"),
                java.time.LocalDateTime.parse(date + "T12:00:00").plusDays(1),
                baseRate,
                BigDecimal.valueOf(0.5),
                100L
            );
            
            BigDecimal result = pricingStrategy.calculate(testInput);
            return Map.of(
                "strategy", strategy,
                "baseRate", baseRate,
                "calculatedRate", result,
                "markup", result.subtract(baseRate),
                "percentage", result.divide(baseRate, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            );
        }
        return Map.of("error", "Strategy not found");
    }
}