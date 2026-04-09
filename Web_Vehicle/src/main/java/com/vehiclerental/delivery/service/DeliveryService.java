package com.vehiclerental.delivery.service;

import com.vehiclerental.common.entity.*;
import com.vehiclerental.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public boolean autoAssignDriver(Booking booking) {
        try {
            List<User> availableDrivers = userRepository.findByRoles_NameAndEnabledTrue("ROLE_DELIVERY");
            
            if (!availableDrivers.isEmpty()) {
                User driver = availableDrivers.get(0);
                
                Delivery delivery = new Delivery();
                delivery.setBooking(booking);
                delivery.setDriver(driver);
                delivery.setStatus("SCHEDULED");
                delivery.setScheduledDate(booking.getStartDate());
                delivery.setAssignedByAdmin(false);
                
                try {
                    deliveryRepository.save(delivery);
                    log.info("Auto-assigned driver {} to booking {}", driver.getUsername(), booking.getId());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to save delivery assignment: {}", e.getMessage());
                    return false;
                }
            }
            
            Delivery delivery = new Delivery();
            delivery.setBooking(booking);
            delivery.setStatus("SCHEDULED");
            delivery.setScheduledDate(booking.getStartDate());
            delivery.setAssignedByAdmin(false);
            
            try {
                deliveryRepository.save(delivery);
                log.info("No available drivers - created pending delivery for booking {}", booking.getId());
                return false;
            } catch (Exception e) {
                log.error("Failed to save pending delivery: {}", e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to assign driver for booking {}: {}", booking.getId(), e.getMessage());
            return false;
        }
    }

    @Transactional
    public void adminAssignDriver(Long deliveryId, Long driverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        User driver = userRepository.findById(driverId)
            .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        delivery.setDriver(driver);
        delivery.setStatus("SCHEDULED");
        delivery.setAssignedByAdmin(true);
        
        deliveryRepository.save(delivery);
        log.info("Admin assigned driver {} to delivery {}", driver.getUsername(), deliveryId);
    }

    @Transactional
    public void confirmDelivery(Long deliveryId, String username) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        if (!delivery.getDriver().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to confirm this delivery");
        }
        
        delivery.setStatus("EN_ROUTE");
        delivery.setConfirmedAt(LocalDateTime.now());
        
        deliveryRepository.save(delivery);
        log.info("Driver {} confirmed delivery {}", username, deliveryId);
    }

    @Transactional
    public void cancelDelivery(Long deliveryId, String username, String reason) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        if (!delivery.getDriver().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to cancel this delivery");
        }
        
        delivery.setStatus("CANCELLED");
        delivery.setCancellationReason(reason);
        delivery.setCancelledAt(LocalDateTime.now());
        
        deliveryRepository.save(delivery);
        
        Delivery newDelivery = new Delivery();
        newDelivery.setBooking(delivery.getBooking());
        newDelivery.setStatus("SCHEDULED");
        newDelivery.setScheduledDate(delivery.getScheduledDate());
        newDelivery.setAssignedByAdmin(false);
        newDelivery.setNotes("Previous driver cancelled: " + reason);
        
        deliveryRepository.save(newDelivery);
        log.info("Driver {} cancelled delivery {} - created new pending delivery", username, deliveryId);
    }

    public List<Delivery> getPendingDeliveries() {
        return deliveryRepository.findByStatusOrderByCreatedAtAsc("SCHEDULED");
    }

    public List<Delivery> getDriverDeliveries(String username) {
        return deliveryRepository.findByDriver_UsernameAndStatusInOrderByScheduledDateAsc(
            username, List.of("SCHEDULED", "EN_ROUTE"));
    }

    public List<User> getAvailableDrivers() {
        return userRepository.findByRoles_NameAndEnabledTrue("ROLE_DELIVERY");
    }
    
    public void createPendingDelivery(Booking booking) {
        try {
            // Check if delivery already exists
            if (deliveryRepository.findByBooking(booking).isEmpty()) {
                Delivery delivery = new Delivery();
                delivery.setBooking(booking);
                delivery.setStatus("SCHEDULED");
                delivery.setScheduledDate(booking.getStartDate());
                delivery.setAssignedByAdmin(false);
                delivery.setNotes("Payment pending - awaiting payment completion");
                
                deliveryRepository.save(delivery);
                log.info("Created pending delivery for unpaid booking {}", booking.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create pending delivery for booking {}: {}", booking.getId(), e.getMessage());
        }
    }
}