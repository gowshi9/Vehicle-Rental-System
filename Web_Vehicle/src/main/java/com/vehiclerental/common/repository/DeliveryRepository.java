package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Delivery;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByDriver(User driver);
    List<Delivery> findByStatus(String status);
    List<Delivery> findByStatusOrderByCreatedAtAsc(String status);
    List<Delivery> findByDriver_UsernameAndStatusInOrderByScheduledDateAsc(String username, List<String> statuses);
    List<Delivery> findByBooking(Booking booking);
    
    @Query("SELECT d FROM Delivery d WHERE d.driver = :driver ORDER BY d.scheduledDate ASC")
    List<Delivery> findByDriverOrderByScheduledAt(@Param("driver") User driver);
    
    long countByStatus(String status);
}