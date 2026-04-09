package com.vehiclerental.marketing.controller;

import com.vehiclerental.common.entity.Promotion;
import com.vehiclerental.common.repository.PromotionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@Slf4j
public class PromotionController {

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePromotion(@RequestParam String code) {
        log.info("Validating promotion code: {}", code);
        
        try {
            Promotion promotion = promotionRepository.findValidPromotion(code, LocalDate.now())
                    .orElse(null);
            
            if (promotion != null) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "discountPercent", promotion.getDiscountPercent(),
                        "title", promotion.getTitle(),
                        "description", promotion.getDescription()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Invalid or expired promotion code"
                ));
            }
        } catch (Exception e) {
            log.error("Error validating promotion", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Error validating promotion code"
            ));
        }
    }
}