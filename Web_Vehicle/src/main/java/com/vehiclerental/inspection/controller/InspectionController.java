package com.vehiclerental.inspection.controller;

import com.vehiclerental.common.entity.Inspection;
import com.vehiclerental.common.repository.InspectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/inspections")
@Slf4j
public class InspectionController {

    @Autowired
    private InspectionRepository inspectionRepository;

    @GetMapping
    public String manageInspections(Model model) {
        model.addAttribute("title", "Inspection Management - Admin");
        
        List<Inspection> inspections = inspectionRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("inspections", inspections);
        
        long pendingCount = inspections.stream().mapToLong(i -> "PENDING_APPROVAL".equals(i.getStatus()) ? 1 : 0).sum();
        long approvedCount = inspections.stream().mapToLong(i -> "APPROVED".equals(i.getStatus()) ? 1 : 0).sum();
        long rejectedCount = inspections.stream().mapToLong(i -> "REJECTED".equals(i.getStatus()) ? 1 : 0).sum();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalInspections", inspections.size());
        
        return "admin/inspections";
    }
    
    @PostMapping("/approve/{inspectionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveInspection(@PathVariable Long inspectionId) {
        try {
            Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));
                
            inspection.setStatus("APPROVED");
            inspectionRepository.save(inspection);
            
            log.info("Inspection {} approved", inspectionId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Inspection approved"));
        } catch (Exception e) {
            log.error("Failed to approve inspection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/reject/{inspectionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectInspection(@PathVariable Long inspectionId,
                                                              @RequestParam String reason) {
        try {
            Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));
                
            inspection.setStatus("REJECTED");
            inspection.setNotes(inspection.getNotes() + "\n\nRejection Reason: " + reason);
            inspectionRepository.save(inspection);
            
            log.info("Inspection {} rejected: {}", inspectionId, reason);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Inspection rejected"));
        } catch (Exception e) {
            log.error("Failed to reject inspection", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}