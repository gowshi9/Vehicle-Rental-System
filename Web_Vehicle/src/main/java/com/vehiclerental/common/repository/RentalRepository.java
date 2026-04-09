package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long>, JpaSpecificationExecutor<Rental> {
    List<Rental> findByStatus(String status);
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByVehicleId(Long vehicleId);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.status = 'ACTIVE'")
    long countActiveRentals();
    
    @Query("SELECT SUM(r.totalAmount) FROM Rental r WHERE r.status = 'CLOSED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT SUM(r.totalAmount) FROM Rental r WHERE r.status = 'CLOSED' AND r.createdAt BETWEEN ?1 AND ?2")
    BigDecimal getRevenueForPeriod(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT r FROM Rental r WHERE r.vehicle.id = ?1 AND r.status IN ('PAID', 'ACTIVE') AND ((r.pickupAt BETWEEN ?2 AND ?3) OR (r.dropoffAt BETWEEN ?2 AND ?3))")
    List<Rental> findConflictingRentals(Long vehicleId, LocalDateTime from, LocalDateTime to);
    
    long countByStatus(String status);
}