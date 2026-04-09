package com.vehiclerental.services;

import com.vehiclerental.common.entity.Rental;
import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.repository.RentalRepository;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.common.repository.UserRepository;
import com.vehiclerental.dto.RentalQuoteInput;
import com.vehiclerental.strategy.pricing.PricingStrategy;
import com.vehiclerental.strategy.payment.PaymentStrategy;
import com.vehiclerental.strategy.payment.PaymentResult;
import com.vehiclerental.strategy.allocation.AllocationStrategy;
import com.vehiclerental.validation.CheckChain;
import com.vehiclerental.validation.BookingContext;
import com.vehiclerental.decorator.CostCalculator;
import com.vehiclerental.decorator.CouponDecorator;
import com.vehiclerental.decorator.LoyaltyDecorator;
import com.vehiclerental.repositories.spec.VehicleSpecs;
import com.vehiclerental.flyweight.VehicleSpecFactory;
import static org.springframework.data.jpa.domain.Specification.where;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final Map<String, PricingStrategy> pricingStrategies;
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final Map<String, AllocationStrategy> allocationStrategies;
    private final CheckChain checkChain;
    private final CostCalculator baseCostCalculator;
    private final VehicleSpecFactory vehicleSpecFactory;
    
    public BigDecimal quote(String vehicleClass, LocalDateTime from, LocalDateTime to, long km, String pricingKey) {
        var vehicle = vehicleRepository.findByCategory(vehicleClass).stream().findFirst()
            .orElseThrow(() -> new RuntimeException("No vehicles available for class: " + vehicleClass));
            
        // Use flyweight for vehicle specs
        var spec = vehicleSpecFactory.get(
            vehicle.getMake(), vehicle.getModel(), "2.5L", 
            vehicle.getSeats(), vehicle.getFuelType(), vehicle.getTransmission()
        );
            
        var input = new RentalQuoteInput(
            vehicleClass, from, to, 
            vehicle.getDailyRate(), 
            BigDecimal.valueOf(2.0), // per km charge
            km
        );
        
        return pricingStrategies.getOrDefault(pricingKey, pricingStrategies.get("weekday"))
            .calculate(input);
    }
    
    public Rental createRental(Long vehicleId, Long customerId, LocalDateTime from, LocalDateTime to, 
                              long expectedKm, String pricingKey) {
        var vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        var customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        // Validation chain
        var context = new BookingContext(vehicleId, customerId, from, to, "DL123456", true, true);
        checkChain.run(context);
        
        var totalAmount = quote(vehicle.getCategory(), from, to, expectedKm, pricingKey);
        
        var rental = new Rental();
        rental.setVehicle(vehicle);
        rental.setCustomer(customer);
        rental.setPickupAt(from);
        rental.setDropoffAt(to);
        rental.setExpectedKm(expectedKm);
        rental.setTotalAmount(totalAmount);
        rental.setPricingStrategy(pricingKey);
        
        return rentalRepository.save(rental);
    }
    
    public Optional<Vehicle> allocateVehicle(String category, String allocationKey) {
        var spec = where(VehicleSpecs.categoryIs(category))
            .and(VehicleSpecs.statusIs("AVAILABLE"));
        var candidates = vehicleRepository.findAll(spec);
        
        return allocationStrategies.getOrDefault(allocationKey, allocationStrategies.get("nearest"))
            .choose(candidates);
    }
    
    public BigDecimal calculateWithPromotions(RentalQuoteInput input, boolean hasCoupon, boolean isLoyalMember) {
        CostCalculator calculator = baseCostCalculator;
        
        if (hasCoupon) {
            calculator = new CouponDecorator(calculator, new BigDecimal("0.10")); // 10% off
        }
        
        if (isLoyalMember) {
            calculator = new LoyaltyDecorator(calculator, new BigDecimal("25.00")); // $25 off
        }
        
        return calculator.cost(input);
    }
    
    public PaymentResult processPayment(Long rentalId, String paymentMethod) {
        var rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RuntimeException("Rental not found"));
            
        var result = paymentStrategies.getOrDefault(paymentMethod, paymentStrategies.get("cash"))
            .pay(rentalId.toString(), rental.getTotalAmount());
            
        if (result.success()) {
            rental.setPaymentReference(result.reference());
            rental.setPaymentMethod(paymentMethod);
            rental.pay(); // State transition
            rentalRepository.save(rental);
        }
        
        return result;
    }
    
    public void startRental(Long rentalId) {
        var rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RuntimeException("Rental not found"));
        rental.start();
        rentalRepository.save(rental);
    }
    
    public void returnVehicle(Long rentalId, long actualKm) {
        var rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RuntimeException("Rental not found"));
        rental.setActualKm(actualKm);
        rental.returnVehicle();
        rentalRepository.save(rental);
    }
    
    public void closeRental(Long rentalId) {
        var rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RuntimeException("Rental not found"));
        rental.close();
        rentalRepository.save(rental);
    }
}