package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Payment;
import com.vehiclerental.common.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    List<Payment> findByBooking(Booking booking);
    List<Payment> findByStatus(Payment.PaymentStatus status);
    List<Payment> findByGatewayType(Payment.GatewayType gatewayType);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :date")
    Long countSuccessfulPaymentsSince(@Param("date") LocalDateTime date);
    
    long countByStatus(String status);
    
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Payment> findByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, Payment.PaymentStatus status);
    
    long countByStatus(Payment.PaymentStatus status);
}