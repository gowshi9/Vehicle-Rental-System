package com.vehiclerental.finance.service;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import com.vehiclerental.notification.bridge.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Colombo")
    public void sendDailyRevenueReport() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime start = yesterday.atStartOfDay();
            LocalDateTime end = yesterday.atTime(23, 59, 59);
            
            List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);
            
            BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            long completedPayments = payments.stream()
                .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED ? 1 : 0)
                .sum();
                
            long pendingPayments = payments.stream()
                .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.PENDING ? 1 : 0)
                .sum();
            
            String report = generateDailyReport(yesterday, totalRevenue, completedPayments, pendingPayments, payments.size());
            
            List<User> financeUsers = userRepository.findByRoles_NameAndEnabledTrue("ROLE_FINANCE");
            for (User user : financeUsers) {
                emailSender.deliver(
                    user.getEmail(),
                    "Daily Revenue Report - " + yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    report
                );
            }
            
            log.info("Daily revenue report sent to {} finance users", financeUsers.size());
            
        } catch (Exception e) {
            log.error("Failed to send daily revenue report", e);
        }
    }
    
    private String generateDailyReport(LocalDate date, BigDecimal revenue, long completed, long pending, int total) {
        return String.format("""
            Daily Revenue Report - %s
            ================================
            
            Revenue Summary:
            - Total Revenue: $%.2f
            - Completed Payments: %d
            - Pending Payments: %d
            - Total Transactions: %d
            
            Report generated at: %s
            
            Best regards,
            DriveEase Finance System
            """, 
            date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            revenue,
            completed,
            pending,
            total,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}