package com.vehiclerental.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.vehiclerental.visitor.Visitable;
import com.vehiclerental.visitor.ReportVisitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle implements Visitable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    private String color;
    private Integer mileage;
    private String fuelType;
    private String transmission;
    private Integer seats;
    private Double distanceKm = 0.0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}