package com.vehiclerental.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/dashboard/admin";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"))) {
            return "redirect:/dashboard/finance";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MARKETING"))) {
            return "redirect:/dashboard/marketing";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SHOP_MANAGER"))) {
            return "redirect:/dashboard/shopmanager";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DELIVERY"))) {
            return "redirect:/dashboard/delivery";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/dashboard/customer";
        }
        return "redirect:/";
    }
}