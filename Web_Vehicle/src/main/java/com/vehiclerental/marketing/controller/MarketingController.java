package com.vehiclerental.marketing.controller;

import com.vehiclerental.common.entity.Promotion;
import com.vehiclerental.common.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/marketing")
public class MarketingController {

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping("/promotions")
    public String managePromotions(Model model) {
        model.addAttribute("title", "Manage Promotions - Marketing");
        model.addAttribute("promotions", promotionRepository.findAll());
        return "marketing/promotions";
    }

    @GetMapping("/promotions/new")
    public String createPromotionForm(Model model) {
        model.addAttribute("title", "Create Promotion - Marketing");
        model.addAttribute("promotion", new Promotion());
        return "marketing/create-promotion";
    }

    @PostMapping("/promotions/create")
    public String createPromotion(@ModelAttribute Promotion promotion) {
        promotionRepository.save(promotion);
        return "redirect:/marketing/promotions";
    }

    @GetMapping("/deals")
    public String dealsPage(Model model) {
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(LocalDate.now());
        model.addAttribute("title", "Special Deals & Offers - DriveEase");
        model.addAttribute("promotions", activePromotions);
        return "marketing/deals";
    }

    @PostMapping("/promotions/{id}/toggle-homepage")
    @ResponseBody
    public String toggleHomepage(@PathVariable Long id) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion != null) {
            promotion.setShowOnHomepage(!promotion.getShowOnHomepage());
            promotionRepository.save(promotion);
            return "success";
        }
        return "error";
    }
    
    @GetMapping("/promotions/{id}/edit")
    public String editPromotionForm(@PathVariable Long id, Model model) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion == null) {
            return "redirect:/marketing/promotions";
        }
        model.addAttribute("title", "Edit Promotion - Marketing");
        model.addAttribute("promotion", promotion);
        return "marketing/edit-promotion";
    }
    
    @PostMapping("/promotions/{id}/update")
    public String updatePromotion(@PathVariable Long id, @ModelAttribute Promotion promotion) {
        promotion.setId(id);
        promotionRepository.save(promotion);
        return "redirect:/marketing/promotions";
    }
    
    @PostMapping("/promotions/{id}/delete")
    @ResponseBody
    public String deletePromotion(@PathVariable Long id) {
        try {
            promotionRepository.deleteById(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    @GetMapping("/api/promotions/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePromotion(@RequestParam String code) {
        Optional<Promotion> promotion = promotionRepository.findValidPromotion(code, LocalDate.now());
        
        if (promotion.isPresent()) {
            Promotion promo = promotion.get();
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "discount", promo.getDiscountPercent(),
                "message", "Promo code applied! You save " + promo.getDiscountPercent() + "%"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "Invalid or expired promo code"
            ));
        }
    }
}