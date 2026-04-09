package com.vehiclerental.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;



    @PostMapping("/vehicle-photo")
    public ResponseEntity<Map<String, String>> uploadVehiclePhoto(@RequestParam("file") MultipartFile file) {
        System.out.println("=== UPLOAD ENDPOINT HIT ===");
        System.out.println("Upload request received. File: " + (file != null ? file.getOriginalFilename() : "null"));
        
        try {
            if (file == null || file.isEmpty()) {
                System.out.println("File is empty or null");
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file"));
            }

            // Use simple filename
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.]", "_");
            
            // Create uploads directory in project root
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                System.out.println("Created uploads directory: " + uploadDir.getAbsolutePath());
            }
            
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);
            
            String fileUrl = "/uploads/" + fileName;
            System.out.println("File saved to: " + destFile.getAbsolutePath());
            System.out.println("File URL: " + fileUrl);
            
            return ResponseEntity.ok(Map.of("url", fileUrl, "filename", fileName));
            
        } catch (Exception e) {
            System.out.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Upload endpoint is working!");
    }
}