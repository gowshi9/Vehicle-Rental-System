package com.vehiclerental.cqrs.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/q")
@RequiredArgsConstructor
@Slf4j
public class DashboardQueryController {
    
    private final DashboardReadService dashboardReadService;
    
    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'SHOP_MANAGER')")
    public ResponseEntity<Map<String, Object>> getKpis() {
        try {
            Map<String, Object> kpis = dashboardReadService.getKpis();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            log.error("Failed to fetch KPIs", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to fetch KPIs"));
        }
    }
    
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Map<String, Object>> getRevenueTrend(@RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> trend = dashboardReadService.getRevenueTrend(days);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            log.error("Failed to fetch revenue trend for {} days", days, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to fetch revenue trend"));
        }
    }
    
    @GetMapping("/vehicle-utilization")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_MANAGER')")
    public ResponseEntity<Map<String, Object>> getVehicleUtilization() {
        try {
            Map<String, Object> utilization = dashboardReadService.getVehicleUtilization();
            return ResponseEntity.ok(utilization);
        } catch (Exception e) {
            log.error("Failed to fetch vehicle utilization", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to fetch vehicle utilization"));
        }
    }
    
    @GetMapping("/cache-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = dashboardReadService.getCacheStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to fetch cache stats", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to fetch cache stats"));
        }
    }
}