package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Inspection;
import com.vehiclerental.common.entity.Booking;
import com.vehiclerental.common.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByDeliveryBooking(Booking booking);
    List<Inspection> findByType(String type);
    Optional<Inspection> findByDeliveryAndType(Delivery delivery, String type);
    
    List<Inspection> findAllByOrderByCreatedAtDesc();
    
    long countByStatus(String status);
}