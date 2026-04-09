package com.vehiclerental.customer;

import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String profile(Model model, Authentication auth) {
        model.addAttribute("title", "My Profile - DriveEase");
        
        if (auth != null) {
            User customer = userRepository.findByUsername(auth.getName()).orElse(null);
            if (customer != null) {
                model.addAttribute("customer", customer);
            }
        }
        
        return "customer/profile";
    }
    
    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateProfile(@RequestParam String email,
                               @RequestParam String phone,
                               Authentication auth,
                               Model model) {
        try {
            User customer = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            customer.setEmail(email);
            customer.setPhone(phone);
            userRepository.save(customer);
            
            model.addAttribute("success", "Profile updated successfully!");
            model.addAttribute("customer", customer);
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        model.addAttribute("title", "My Profile - DriveEase");
        return "customer/profile";
    }
}