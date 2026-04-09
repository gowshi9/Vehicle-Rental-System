package com.vehiclerental.strategy.allocation;

import com.vehiclerental.common.entity.Vehicle;
import java.util.List;
import java.util.Optional;

public interface AllocationStrategy {
    String key();
    Optional<Vehicle> choose(List<Vehicle> candidates);
}