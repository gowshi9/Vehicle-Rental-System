package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByStatus(String status);
    List<Vehicle> findByCategory(String category);
    
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'AVAILABLE'")
    long countAvailableVehicles();
    
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'RENTED'")
    long countRentedVehicles();
    
    @Query("SELECT v.category, COUNT(v) FROM Vehicle v GROUP BY v.category")
    List<Object[]> countByCategory();
    

}