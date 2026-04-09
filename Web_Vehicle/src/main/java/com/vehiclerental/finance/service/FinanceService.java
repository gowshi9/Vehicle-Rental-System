package com.vehiclerental.finance.service;

import com.vehiclerental.common.entity.Payment;
import com.vehiclerental.common.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {
    
    private final PaymentRepository paymentRepository;
    
    @Cacheable(value = "dailyRevenue", key = "#date")
    public BigDecimal getDailyRevenue(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        
        return paymentRepository.findByCreatedAtBetween(start, end)
            .stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Cacheable(value = "monthlyRevenue", key = "#year + '-' + #month")
    public BigDecimal getMonthlyRevenue(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        
        return paymentRepository.findByCreatedAtBetween(start, end)
            .stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Map<String, Object> getFinancialSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);
        
        BigDecimal totalRevenue = payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal pendingAmount = payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        long completedCount = payments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED ? 1 : 0)
            .sum();
            
        long pendingCount = payments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.PENDING ? 1 : 0)
            .sum();
            
        long refundedCount = payments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.REFUNDED ? 1 : 0)
            .sum();
        
        return Map.of(
            "totalRevenue", totalRevenue,
            "pendingAmount", pendingAmount,
            "completedCount", completedCount,
            "pendingCount", pendingCount,
            "refundedCount", refundedCount,
            "totalTransactions", payments.size(),
            "period", Map.of("start", startDate, "end", endDate)
        );
    }
    
    public Map<String, Object> getDashboardMetrics() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);
        
        // Current month data
        BigDecimal monthlyRevenue = getMonthlyRevenue(now.getYear(), now.getMonthValue());
        List<Payment> allPayments = paymentRepository.findAll();
        long totalPayments = allPayments.size();
        long pendingPayments = allPayments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.PENDING ? 1 : 0)
            .sum();
            
        // Previous month for growth calculation
        BigDecimal lastMonthRevenue = getMonthlyRevenue(lastMonthStart.getYear(), lastMonthStart.getMonthValue());
        double growthRate = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthRate = monthlyRevenue.subtract(lastMonthRevenue)
                .divide(lastMonthRevenue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        
        return Map.of(
            "monthlyRevenue", monthlyRevenue,
            "totalPayments", totalPayments,
            "pendingPayments", pendingPayments,
            "growthRate", growthRate
        );
    }
}