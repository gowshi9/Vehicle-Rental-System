package com.vehiclerental.repositories.spec;

import com.vehiclerental.common.entity.Vehicle;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public final class VehicleSpecs {
    
    private VehicleSpecs() {}
    
    public static Specification<Vehicle> categoryIs(String category) {
        return (root, query, cb) -> 
            category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }
    
    public static Specification<Vehicle> seatsAtLeast(Integer seats) {
        return (root, query, cb) -> 
            seats == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("seats"), seats);
    }
    
    public static Specification<Vehicle> rateBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min == null) return cb.lessThanOrEqualTo(root.get("dailyRate"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("dailyRate"), min);
            return cb.between(root.get("dailyRate"), min, max);
        };
    }
    
    public static Specification<Vehicle> statusIs(String status) {
        return (root, query, cb) -> 
            status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
    
    public static Specification<Vehicle> fuelTypeIs(String fuelType) {
        return (root, query, cb) -> 
            fuelType == null ? cb.conjunction() : cb.equal(root.get("fuelType"), fuelType);
    }
}