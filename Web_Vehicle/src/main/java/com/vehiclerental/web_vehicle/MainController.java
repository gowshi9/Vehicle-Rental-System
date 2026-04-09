package com.vehiclerental.web_vehicle;

import com.vehiclerental.common.entity.ContactMessage;
import com.vehiclerental.common.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class MainController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("title", "DriveEase - Premium Vehicle Rental");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Us - DriveEase");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contact Us - DriveEase");
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute ContactMessage contactMessage,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Contact Us - DriveEase");
            return "contact";
        }

        try {
            contactMessageRepository.save(contactMessage);
            log.info("Contact message received from: {}", contactMessage.getEmail());
            redirectAttributes.addFlashAttribute("success", "Thank you for your message! We'll get back to you soon.");
            return "redirect:/contact";
        } catch (Exception e) {
            log.error("Error saving contact message", e);
            result.reject("error.contact", "There was an error sending your message. Please try again.");
            model.addAttribute("title", "Contact Us - DriveEase");
            return "contact";
        }
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        model.addAttribute("title", "Login - DriveEase");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return "redirect:/dashboard/admin";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("FINANCE"))) {
            return "redirect:/dashboard/finance";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("MARKETING"))) {
            return "redirect:/dashboard/marketing";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("SHOP_MANAGER"))) {
            return "redirect:/dashboard/shopmanager";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("DELIVERY"))) {
            return "redirect:/dashboard/delivery";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("CUSTOMER"))) {
            return "redirect:/dashboard/customer";
        }
        
        return "redirect:/";
    }
}