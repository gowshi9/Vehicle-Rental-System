package com.vehiclerental.cqrs.query;

import com.vehiclerental.common.repository.RentalRepository;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.common.repository.UserRepository;
import com.vehiclerental.flyweight.VehicleSpecFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardReadService {
    
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleSpecFactory vehicleSpecFactory;
    
    public Map<String, Object> getKpis() {
        try {
            BigDecimal revenue = rentalRepository.getTotalRevenue();
            return Map.of(
                "totalVehicles", vehicleRepository.count(),
                "availableVehicles", vehicleRepository.countAvailableVehicles(),
                "activeRentals", rentalRepository.countActiveRentals(),
                "totalUsers", userRepository.count(),
                "totalRevenue", revenue != null ? revenue : BigDecimal.ZERO,
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            return Map.of(
                "error", "Unable to fetch KPIs",
                "timestamp", LocalDateTime.now()
            );
        }
    }
    
    public Map<String, Object> getRevenueTrend(int days) {
        try {
            if (days <= 0 || days > 365) {
                days = 7; // Default to 7 days
            }
            
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            // Get actual revenue data from database
            BigDecimal totalRevenue = rentalRepository.getRevenueForPeriod(startDate, endDate);
            
            return Map.of(
                "period", days + " days",
                "startDate", startDate.toLocalDate(),
                "endDate", endDate.toLocalDate(),
                "totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            return Map.of(
                "error", "Unable to fetch revenue trend",
                "period", days + " days",
                "timestamp", LocalDateTime.now()
            );
        }
    }
    
    public Map<String, Object> getVehicleUtilization() {
        try {
            var categories = vehicleRepository.countByCategory();
            long totalVehicles = vehicleRepository.count();
            long availableVehicles = vehicleRepository.countAvailableVehicles();
            
            double utilizationRate = totalVehicles > 0 ? 
                (double) (totalVehicles - availableVehicles) / totalVehicles : 0.0;
            
            return Map.of(
                "byCategory", categories != null ? categories : Map.of(),
                "utilizationRate", Math.round(utilizationRate * 100.0) / 100.0,
                "totalVehicles", totalVehicles,
                "availableVehicles", availableVehicles,
                "timestamp", LocalDateTime.now()
            );
        } catch (Exception e) {
            return Map.of(
                "error", "Unable to fetch vehicle utilization",
                "timestamp", LocalDateTime.now()
            );
        }
    }
    
    public Map<String, Object> getCacheStats() {
        return vehicleSpecFactory.getCacheStats();
    }
}