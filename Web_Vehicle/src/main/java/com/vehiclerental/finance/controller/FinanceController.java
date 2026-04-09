package com.vehiclerental.finance.controller;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import com.vehiclerental.finance.service.FinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance")
@Slf4j
public class FinanceController {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private FinanceService financeService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public String financeDashboard(Model model,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                 @RequestParam(required = false) String status) {
        
        model.addAttribute("title", "Finance Dashboard - DriveEase");
        
        // Set default date range if not provided
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedStatus", status != null ? HtmlUtils.htmlEscape(status) : null);
        
        // Get filtered payments
        List<Payment> payments = getFilteredPayments(startDate, endDate, status);
        model.addAttribute("payments", payments);
        
        // Calculate statistics using FinanceService
        Map<String, Object> financialSummary = financeService.getFinancialSummary(startDate, endDate);
        model.addAllAttributes(financialSummary);
        
        // Add dashboard metrics
        Map<String, Object> dashboardMetrics = financeService.getDashboardMetrics();
        model.addAllAttributes(dashboardMetrics);
        
        // Get delivery status counts
        long finishedDeliveries = deliveryRepository.countByStatus("DELIVERED");
        long activeDeliveries = deliveryRepository.countByStatus("SCHEDULED");
        
        model.addAttribute("finishedDeliveries", finishedDeliveries);
        model.addAttribute("activeDeliveries", activeDeliveries);
        
        return "finance/dashboard";
    }
    
    @PostMapping("/payment/{paymentId}/refund")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Map<String, Object>> processRefund(@PathVariable @NotNull Long paymentId,
                                                           @RequestParam @NotNull BigDecimal amount) {
        try {
            if (paymentId <= 0 || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid parameters"));
            }
            
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
                
            if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Payment not eligible for refund"));
            }
            
            if (amount.compareTo(payment.getAmount()) > 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refund amount exceeds payment amount"));
            }
            
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.getBooking().setPaymentStatus(Booking.PaymentStatus.REFUNDED);
            
            paymentRepository.save(payment);
            bookingRepository.save(payment.getBooking());
            
            log.info("Refund processed for payment ID: {}", paymentId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Refund processed successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid refund request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid request"));
        } catch (Exception e) {
            log.error("Refund processing failed for payment ID: {}", paymentId, e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Processing failed"));
        }
    }
    
    @GetMapping("/reports/export")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<byte[]> exportReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate endDate,
                                             @RequestParam @NotNull String format) {
        
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Payment> payments = getFilteredPayments(startDate, endDate, null);
            
            StringBuilder csv = new StringBuilder();
            csv.append("Payment ID,Booking ID,Customer,Amount,Status,Date,Vehicle\n");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Payment payment : payments) {
                csv.append(escapeCSV(payment.getId().toString())).append(",")
                   .append(escapeCSV(payment.getBooking().getId().toString())).append(",")
                   .append(escapeCSV(payment.getBooking().getCustomer().getUsername())).append(",")
                   .append(escapeCSV(payment.getAmount().toString())).append(",")
                   .append(escapeCSV(payment.getStatus().toString())).append(",")
                   .append(escapeCSV(payment.getCreatedAt().format(formatter))).append(",")
                   .append(escapeCSV(payment.getBooking().getVehicle().getMake() + " " + payment.getBooking().getVehicle().getModel()))
                   .append("\n");
            }
            
            String filename = String.format("finance-report-%s-to-%s.csv", startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString().getBytes());
                
        } catch (Exception e) {
            log.error("Export failed for date range {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public String analytics(Model model) {
        model.addAttribute("title", "Finance Analytics - DriveEase");
        
        // Get dynamic analytics data
        Map<String, Object> analyticsData = financeService.getFinancialSummary(
            LocalDate.now().minusMonths(1), LocalDate.now());
        model.addAllAttributes(analyticsData);
        
        return "finance/analytics";
    }
    
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public String transactions(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        model.addAttribute("title", "Transactions - DriveEase");
        
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        
        return "finance/transactions";
    }
    
    private List<Payment> getFilteredPayments(LocalDate startDate, LocalDate endDate, String status) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        if (status != null && !status.isEmpty()) {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status);
            return paymentRepository.findByCreatedAtBetweenAndStatus(start, end, paymentStatus);
        } else {
            return paymentRepository.findByCreatedAtBetween(start, end);
        }
    }
}