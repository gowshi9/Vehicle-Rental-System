package com.vehiclerental.strategy.allocation;

import com.vehiclerental.common.entity.Vehicle;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class CheapestAllocation implements AllocationStrategy {
    
    @Override
    public String key() {
        return "cheapest";
    }
    
    @Override
    public Optional<Vehicle> choose(List<Vehicle> candidates) {
        return candidates.stream()
            .min(Comparator.comparing(Vehicle::getDailyRate));
    }
}