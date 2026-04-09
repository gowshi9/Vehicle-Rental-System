package com.vehiclerental.admin.controller;

import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Role;
import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.repository.UserRepository;
import com.vehiclerental.common.repository.RoleRepository;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.common.repository.RentalRepository;
import com.vehiclerental.common.repository.BookingRepository;
import com.vehiclerental.common.repository.DeliveryRepository;
import com.vehiclerental.vehicles.service.VehicleUpdateService;
import com.vehiclerental.delivery.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private VehicleUpdateService vehicleUpdateService;
    
    @Autowired
    private com.vehiclerental.common.repository.ContactMessageRepository contactMessageRepository;
    
    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("title", "Manage Users - Admin");
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/vehicles")
    public String manageVehicles(Model model) {
        model.addAttribute("title", "Manage Vehicles - Admin");
        model.addAttribute("vehicles", vehicleRepository.findAll());
        return "admin/vehicles";
    }

    @GetMapping("/vehicles/new")
    public String addVehicleForm(Model model) {
        model.addAttribute("title", "Add Vehicle - Admin");
        return "admin/add-vehicle";
    }

    @PostMapping("/vehicles/create")
    public String createVehicle(@RequestParam String licensePlate,
                               @RequestParam String make,
                               @RequestParam String model,
                               @RequestParam Integer year,
                               @RequestParam String category,
                               @RequestParam java.math.BigDecimal dailyRate,
                               @RequestParam String color,
                               @RequestParam Integer mileage,
                               @RequestParam String fuelType,
                               @RequestParam String transmission,
                               @RequestParam Integer seats,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) String photoUrl,
                               RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate(licensePlate);
            vehicle.setMake(make);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setCategory(category);
            vehicle.setDailyRate(dailyRate);
            vehicle.setColor(color);
            vehicle.setMileage(mileage);
            vehicle.setFuelType(fuelType);
            vehicle.setTransmission(transmission);
            vehicle.setSeats(seats);
            vehicle.setDescription(description);
            vehicle.setPhotoUrl(photoUrl);
            vehicle.setStatus("AVAILABLE");
            
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            vehicleUpdateService.notifyVehicleUpdate("created", savedVehicle.getId());
            redirectAttributes.addFlashAttribute("success", "Vehicle created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating vehicle: " + e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/vehicles/edit/{id}")
    public String editVehicleForm(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle == null) {
            return "redirect:/admin/vehicles";
        }
        model.addAttribute("vehicle", vehicle);
        return "admin/edit-vehicle";
    }

    @PostMapping("/vehicles/edit/{id}")
    public String editVehicle(@PathVariable Long id,
                             @RequestParam String licensePlate,
                             @RequestParam String make,
                             @RequestParam String model,
                             @RequestParam Integer year,
                             @RequestParam String category,
                             @RequestParam java.math.BigDecimal dailyRate,
                             @RequestParam String status,
                             @RequestParam String color,
                             @RequestParam Integer mileage,
                             @RequestParam String fuelType,
                             @RequestParam String transmission,
                             @RequestParam Integer seats,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String photoUrl,
                             RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
            if (vehicle == null) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
                return "redirect:/admin/vehicles";
            }
            
            vehicle.setLicensePlate(licensePlate);
            vehicle.setMake(make);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setCategory(category);
            vehicle.setDailyRate(dailyRate);
            vehicle.setStatus(status);
            vehicle.setColor(color);
            vehicle.setMileage(mileage);
            vehicle.setFuelType(fuelType);
            vehicle.setTransmission(transmission);
            vehicle.setSeats(seats);
            vehicle.setDescription(description);
            if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                vehicle.setPhotoUrl(photoUrl);
            }
            
            vehicleRepository.save(vehicle);
            vehicleUpdateService.notifyVehicleUpdate("updated", vehicle.getId());
            redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating vehicle: " + e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
            if (vehicle == null) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
                return "redirect:/admin/vehicles";
            }
            
            vehicleRepository.delete(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting vehicle: " + e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("title", "Reports - Admin");
        
        // Add analytics data for reports
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("availableVehicles", vehicleRepository.countAvailableVehicles());
        model.addAttribute("rentedVehicles", vehicleRepository.countRentedVehicles());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalRentals", rentalRepository.count());
        model.addAttribute("totalRevenue", rentalRepository.getTotalRevenue() != null ? rentalRepository.getTotalRevenue() : java.math.BigDecimal.ZERO);
        
        return "admin/reports";
    }

    @GetMapping("/logs")
    public String viewLogs(Model model) {
        model.addAttribute("title", "System Logs - Admin");
        return "admin/logs";
    }

    @GetMapping("/users/new")
    public String addUser(Model model) {
        model.addAttribute("title", "Add User - Admin");
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/add-user";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String role,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            
            Role userRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role));
            user.getRoles().add(userRole);
            
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                          @RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String phone,
                          @RequestParam String role,
                          @RequestParam(required = false) String password,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found!");
                return "redirect:/admin/users";
            }
            
            // Log original values
            System.out.println("Original user: " + user.getUsername() + ", " + user.getEmail() + ", " + user.getPhone());
            System.out.println("New values: " + username + ", " + email + ", " + phone + ", role: " + role);
            
            user.setUsername(username);
            user.setEmail(email);
            user.setPhone(phone);
            
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
                System.out.println("Password updated");
            }
            
            Role userRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role));
            user.getRoles().clear();
            user.getRoles().add(userRole);
            
            User savedUser = userRepository.save(user);
            System.out.println("Saved user: " + savedUser.getUsername() + ", " + savedUser.getEmail() + ", " + savedUser.getPhone());
            
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found!");
                return "redirect:/admin/users";
            }
            
            // Prevent deletion of admin user
            if ("admin".equals(user.getUsername()) || 
                user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()))) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete admin user!");
                return "redirect:/admin/users";
            }
            
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }



    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        model.addAttribute("title", "Manage Bookings - Admin");
        
        // Get all bookings with payment information
        List<com.vehiclerental.common.entity.Booking> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("bookings", bookings);
        
        // Get payment statistics
        long totalBookings = bookingRepository.count();
        long paidBookings = bookingRepository.countByPaymentStatus(com.vehiclerental.common.entity.Booking.PaymentStatus.PAID);
        long pendingBookings = bookingRepository.countByPaymentStatus(com.vehiclerental.common.entity.Booking.PaymentStatus.UNPAID);
        
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("paidBookings", paidBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        
        return "admin/bookings";
    }

    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("title", "System Settings - Admin");
        return "admin/settings";
    }

    @GetMapping("/analytics")
    public String viewAnalytics(Model model) {
        model.addAttribute("title", "Analytics - Admin");
        
        // Vehicle analytics
        model.addAttribute("totalVehicles", vehicleRepository.count());
        model.addAttribute("availableVehicles", vehicleRepository.countAvailableVehicles());
        model.addAttribute("rentedVehicles", vehicleRepository.countRentedVehicles());
        model.addAttribute("vehiclesByCategory", vehicleRepository.countByCategory());
        
        // User analytics
        model.addAttribute("totalUsers", userRepository.count());
        
        // Rental analytics
        model.addAttribute("totalRentals", rentalRepository.count());
        model.addAttribute("activeRentals", rentalRepository.countActiveRentals());
        model.addAttribute("totalRevenue", rentalRepository.getTotalRevenue() != null ? rentalRepository.getTotalRevenue() : java.math.BigDecimal.ZERO);
        
        return "admin/analytics";
    }

    @GetMapping("/reports/download/{type}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String type) {
        String content = "Sample " + type + " Report\nGenerated on: " + java.time.LocalDateTime.now();
        byte[] pdfBytes = content.getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", type + "-report.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<byte[]> generateCustomReport(
            @RequestParam String reportType,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        String content = "Custom " + reportType + " Report\nFrom: " + fromDate + " To: " + toDate;
        byte[] pdfBytes = content.getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "custom-report.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs() {
        String csvContent = "Timestamp,Level,Message,User,IP\n" +
                "2024-10-13 20:15:32,INFO,User login successful,admin,192.168.1.100\n" +
                "2024-10-13 20:10:15,WARN,Failed login attempt,unknown,192.168.1.105";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "system-logs.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvContent.getBytes());
    }

    @GetMapping("/logs/filter")
    public String filterLogs(@RequestParam(required = false) String level,
                           @RequestParam(required = false) String date,
                           Model model) {
        model.addAttribute("title", "System Logs - Admin");
        model.addAttribute("filterLevel", level);
        model.addAttribute("filterDate", date);
        return "admin/logs";
    }
    
    @GetMapping("/deliveries")
    public String manageDeliveries(Model model) {
        model.addAttribute("title", "Delivery Management - Admin");
        
        List<com.vehiclerental.common.entity.Delivery> pendingDeliveries = deliveryService.getPendingDeliveries();
        List<com.vehiclerental.common.entity.Delivery> allDeliveries = deliveryRepository.findAll();
        List<com.vehiclerental.common.entity.User> availableDrivers = deliveryService.getAvailableDrivers();
        
        model.addAttribute("pendingDeliveries", pendingDeliveries);
        model.addAttribute("allDeliveries", allDeliveries);
        model.addAttribute("availableDrivers", availableDrivers);
        
        model.addAttribute("totalDeliveries", deliveryRepository.count());
        model.addAttribute("completedDeliveries", deliveryRepository.countByStatus("DELIVERED"));
        model.addAttribute("assignedDeliveries", deliveryRepository.countByStatus("SCHEDULED"));
        model.addAttribute("pendingDeliveriesCount", deliveryRepository.countByStatus("SCHEDULED"));
        
        return "admin/deliveries";
    }
    
    @PostMapping("/deliveries/assign")
    public String assignDriver(@RequestParam Long deliveryId,
                              @RequestParam Long driverId,
                              RedirectAttributes redirectAttributes) {
        try {
            deliveryService.adminAssignDriver(deliveryId, driverId);
            redirectAttributes.addFlashAttribute("success", "Driver assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign driver: " + e.getMessage());
        }
        return "redirect:/admin/deliveries";
    }
    
    @GetMapping("/contact-messages")
    public String contactMessages(Model model) {
        model.addAttribute("title", "Contact Messages - Admin Dashboard");
        model.addAttribute("messages", contactMessageRepository.findAllOrderByCreatedAtDesc());
        return "admin/contact-messages";
    }
    
    @PostMapping("/contact-messages/sms")
    public String sendSmsReply(@RequestParam Long messageId,
                              @RequestParam String smsMessage,
                              RedirectAttributes redirectAttributes) {
        try {
            var contactMessage = contactMessageRepository.findById(messageId).orElse(null);
            if (contactMessage != null) {
                var smsService = new com.vehiclerental.sms.SmsService();
                boolean sent = smsService.sendSms(contactMessage.getPhone(), smsMessage);
                if (sent) {
                    redirectAttributes.addFlashAttribute("success", "SMS sent successfully to " + contactMessage.getName());
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to send SMS");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending SMS: " + e.getMessage());
        }
        return "redirect:/admin/contact-messages";
    }
}