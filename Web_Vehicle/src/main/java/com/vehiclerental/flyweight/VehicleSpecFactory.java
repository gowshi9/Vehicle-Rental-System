package com.vehiclerental.flyweight;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class VehicleSpecFactory {
    
    private final Map<String, VehicleSpec> cache = new ConcurrentHashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;
    
    public VehicleSpec get(String make, String model, String engine, int seats, String fuelType, String transmission) {
        String key = "%s|%s|%s|%d|%s|%s".formatted(make, model, engine, seats, fuelType, transmission);
        
        VehicleSpec spec = cache.get(key);
        if (spec != null) {
            cacheHits++;
            log.debug("Cache HIT for key: {} (hits: {}, misses: {})", key, cacheHits, cacheMisses);
            return spec;
        }
        
        cacheMisses++;
        spec = new VehicleSpec(make, model, engine, seats, fuelType, transmission);
        cache.put(key, spec);
        log.info("Cache MISS for key: {} - Created new spec (hits: {}, misses: {})", key, cacheHits, cacheMisses);
        
        return spec;
    }
    
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "cacheSize", cache.size(),
            "cacheHits", cacheHits,
            "cacheMisses", cacheMisses,
            "hitRatio", cacheHits + cacheMisses > 0 ? (double) cacheHits / (cacheHits + cacheMisses) : 0.0
        );
    }
}