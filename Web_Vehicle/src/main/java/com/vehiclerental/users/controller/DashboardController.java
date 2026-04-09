package com.vehiclerental.users.controller;

import com.vehiclerental.common.repository.*;
import com.vehiclerental.delivery.service.DeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard - DriveEase");
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("pendingDeliveries", deliveryRepository.countByStatus("SCHEDULED"));
        return "dashboard/admin";
    }

    @GetMapping("/finance")
    @PreAuthorize("hasAuthority('FINANCE')")
    public String financeDashboard(Model model) {
        BigDecimal totalRevenue = bookingRepository.getTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        BigDecimal monthlyRevenue = bookingRepository.getRevenueForPeriod(monthStart, monthEnd);
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
        
        model.addAttribute("title", "Finance Dashboard - DriveEase");
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("pendingPayments", bookingRepository.countByPaymentStatusAndNotCancelled(com.vehiclerental.common.entity.Booking.PaymentStatus.UNPAID));
        return "dashboard/finance";
    }

    @GetMapping("/marketing")
    @PreAuthorize("hasAuthority('MARKETING')")
    public String marketingDashboard(Model model) {
        model.addAttribute("title", "Marketing Dashboard - DriveEase");
        model.addAttribute("activePromotions", promotionRepository.findByActive(true).size());
        model.addAttribute("totalPromotions", promotionRepository.count());
        return "dashboard/marketing";
    }

    @GetMapping("/shopmanager")
    @PreAuthorize("hasAuthority('SHOP_MANAGER')")
    public String shopManagerDashboard(Model model) {
        model.addAttribute("title", "Shop Manager Dashboard - DriveEase");
        model.addAttribute("availableVehicles", vehicleRepository.findByStatus("AVAILABLE").size());
        model.addAttribute("maintenanceVehicles", vehicleRepository.findByStatus("MAINTENANCE").size());
        model.addAttribute("bookedVehicles", vehicleRepository.findByStatus("BOOKED").size());
        return "dashboard/shopmanager";
    }

    @GetMapping("/delivery")
    @PreAuthorize("hasAuthority('DELIVERY')")
    public String deliveryDashboard(Model model, Authentication auth) {
        model.addAttribute("title", "Driver Dashboard - DriveEase");
        
        // Get driver's deliveries
        var myDeliveries = deliveryService.getDriverDeliveries(auth.getName());
        model.addAttribute("deliveries", myDeliveries);
        
        // Statistics
        model.addAttribute("assignedCount", deliveryRepository.countByStatus("SCHEDULED"));
        model.addAttribute("confirmedCount", deliveryRepository.countByStatus("EN_ROUTE"));
        model.addAttribute("completedCount", deliveryRepository.countByStatus("DELIVERED"));
        
        return "dashboard/delivery";
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public String customerDashboard(Model model) {
        model.addAttribute("title", "My Dashboard - DriveEase");
        return "dashboard/customer";
    }
}