package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Booking;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    java.util.Optional<Booking> findByIdAndCustomer(Long id, User customer);
    List<Booking> findByVehicle(Vehicle vehicle);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);
    
    @Query("SELECT b FROM Booking b WHERE b.customer = :customer ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerOrderByCreatedAtDesc(@Param("customer") User customer);
    
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle = :vehicle AND b.status = 'CONFIRMED' AND " +
           "((b.startDate <= :endDate) AND (b.endDate >= :startDate))")
    boolean existsConflictingBooking(@Param("vehicle") Vehicle vehicle, 
                                   @Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(b.total) FROM Booking b WHERE b.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(b.total) FROM Booking b WHERE b.paymentStatus = 'PAID' AND " +
           "b.createdAt >= :startDate AND b.createdAt <= :endDate")
    BigDecimal getRevenueForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b.vehicle, SUM(b.total) as revenue FROM Booking b WHERE b.paymentStatus = 'PAID' " +
           "GROUP BY b.vehicle ORDER BY revenue DESC")
    List<Object[]> getTopVehiclesByRevenue();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.promoCode = :promoCode")
    Long countByPromoCode(@Param("promoCode") String promoCode);
    
    List<Booking> findTop10ByOrderByIdDesc();
    
    List<Booking> findAllByOrderByCreatedAtDesc();
    
    long countByPaymentStatus(Booking.PaymentStatus paymentStatus);
    
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId AND b.status IN ('CONFIRMED', 'PENDING') AND " +
           "((b.startDate <= :endDate) AND (b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(@Param("vehicleId") Long vehicleId, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :paymentStatus AND NOT EXISTS (SELECT d FROM Delivery d WHERE d.booking = b)")
    List<Booking> findByPaymentStatusAndDeliveryIsNull(@Param("paymentStatus") Booking.PaymentStatus paymentStatus);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.paymentStatus = :paymentStatus AND b.status != 'CANCELLED'")
    long countByPaymentStatusAndNotCancelled(@Param("paymentStatus") Booking.PaymentStatus paymentStatus);
}