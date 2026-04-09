package com.vehiclerental.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.vehiclerental.state.rental.RentalState;
import com.vehiclerental.state.rental.CreatedState;
import com.vehiclerental.memento.RentalSnapshot;
import com.vehiclerental.visitor.Visitable;
import com.vehiclerental.visitor.ReportVisitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rental implements Visitable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private LocalDateTime pickupAt;

    @Column(nullable = false)
    private LocalDateTime dropoffAt;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status = "CREATED";

    @Column(nullable = false)
    private String paymentMethod = "CASH";

    @Column(nullable = false)
    private String pricingStrategy = "WEEKDAY";

    private String paymentReference;
    private LocalDateTime actualPickupAt;
    private LocalDateTime actualReturnAt;
    private Long expectedKm = 0L;
    private Long actualKm = 0L;
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String snapshotJson; // Serialized RentalSnapshot
    
    @Transient
    private RentalSnapshot snapshot;

    @Transient
    private RentalState state = new CreatedState();

    public void setState(RentalState state) {
        this.state = state;
        this.status = state.name();
        this.updatedAt = LocalDateTime.now();
    }

    public void pay() { state.pay(this); }
    public void start() { state.start(this); }
    public void returnVehicle() { state.returnVehicle(this); }
    public void close() { state.close(this); }

    public void markPaid() {
        this.status = "PAID";
        this.updatedAt = LocalDateTime.now();
    }

    public void markActive() {
        this.status = "ACTIVE";
        this.actualPickupAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markReturned() {
        this.status = "RETURNED";
        this.actualReturnAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markClosed() {
        this.status = "CLOSED";
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this);
    }
}