package com.vehiclerental.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Promotion code is required")
    @Column(unique = true)
    private String code;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Min(value = 0, message = "Discount percent must be at least 0")
    @Max(value = 90, message = "Discount percent cannot exceed 90")
    @Column(name = "discount_percent")
    private Integer discountPercent;

    @NotNull(message = "Valid from date is required")
    @Column(name = "valid_from")
    private LocalDate validFrom;

    @NotNull(message = "Valid to date is required")
    @Column(name = "valid_to")
    private LocalDate validTo;

    private boolean active = true;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "min_rental_days")
    private Integer minRentalDays = 1;
    
    @Column(name = "max_uses")
    private Integer maxUses;
    
    @Column(name = "current_uses")
    private Integer currentUses = 0;
    
    @Column(name = "target_segment")
    private String targetSegment; // NEW, RETURNING, VIP, ALL
    
    @Column(name = "marketing_title")
    private String marketingTitle;
    
    @Column(name = "marketing_description")
    private String marketingDescription;
    
    @Column(name = "banner_color")
    private String bannerColor = "#dc3545";
    
    @Column(name = "show_on_homepage")
    private Boolean showOnHomepage = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return active && 
               !now.isBefore(validFrom) && 
               !now.isAfter(validTo) &&
               (maxUsage == null || usageCount < maxUsage);
    }
}