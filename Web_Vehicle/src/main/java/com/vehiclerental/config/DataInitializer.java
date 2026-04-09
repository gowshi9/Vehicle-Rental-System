package com.vehiclerental.config;

import com.vehiclerental.common.entity.Role;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.entity.Rental;
import com.vehiclerental.common.repository.RoleRepository;
import com.vehiclerental.common.repository.UserRepository;
import com.vehiclerental.common.repository.VehicleRepository;
import com.vehiclerental.common.repository.RentalRepository;
import com.vehiclerental.flyweight.VehicleSpecFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final VehicleSpecFactory vehicleSpecFactory;

    @Override
    public void run(String... args) throws Exception {
        // Create roles
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        Role financeRole = createRoleIfNotExists("ROLE_FINANCE");
        Role marketingRole = createRoleIfNotExists("ROLE_MARKETING");
        Role shopManagerRole = createRoleIfNotExists("ROLE_SHOP_MANAGER");
        Role deliveryRole = createRoleIfNotExists("ROLE_DELIVERY");
        Role customerRole = createRoleIfNotExists("ROLE_CUSTOMER");

        // Create users
        createUserIfNotExists("admin", "admin@driveease.com", "Admin123!", Set.of(adminRole));
        createUserIfNotExists("finance", "finance@driveease.com", "Finance123!", Set.of(financeRole));
        createUserIfNotExists("marketing", "marketing@driveease.com", "Marketing123!", Set.of(marketingRole));
        createUserIfNotExists("shopmanager", "shop@driveease.com", "ShopManager123!", Set.of(shopManagerRole));
        createUserIfNotExists("delivery", "delivery@driveease.com", "Delivery123!", Set.of(deliveryRole));
        createUserIfNotExists("customer", "customer@driveease.com", "Customer123!", Set.of(customerRole));
        
        // Create sample vehicles with flyweight specs
        createVehicleIfNotExists("ABC-1234", "Toyota", "Camry", 2023, "Sedan", new BigDecimal("45.00"));
        createVehicleIfNotExists("XYZ-5678", "Honda", "Civic", 2022, "Sedan", new BigDecimal("40.00"));
        createVehicleIfNotExists("DEF-9012", "Ford", "Explorer", 2023, "SUV", new BigDecimal("65.00"));
        createVehicleIfNotExists("GHI-3456", "Nissan", "Altima", 2021, "Sedan", new BigDecimal("42.00"));
        createVehicleIfNotExists("JKL-7890", "Chevrolet", "Tahoe", 2023, "SUV", new BigDecimal("75.00"));
        
        // Pre-populate flyweight cache
        initializeFlyweightCache();
        
        // Create sample rentals
        createSampleRentals();
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void createUserIfNotExists(String username, String email, String password, Set<Role> roles) {
        if (!userRepository.findByUsername(username).isPresent()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(roles);
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
    
    private void createVehicleIfNotExists(String licensePlate, String make, String model, int year, String category, BigDecimal dailyRate) {
        if (!vehicleRepository.findByLicensePlate(licensePlate).isPresent()) {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate(licensePlate);
            vehicle.setMake(make);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setCategory(category);
            vehicle.setDailyRate(dailyRate);
            vehicle.setStatus("AVAILABLE");
            vehicle.setColor("Black");
            vehicle.setMileage(15000);
            vehicle.setFuelType("Gasoline");
            vehicle.setTransmission("Automatic");
            vehicle.setSeats(5);
            vehicle.setDistanceKm(Math.random() * 50 + 5); // Random distance 5-55km
            vehicleRepository.save(vehicle);
        }
    }
    
    private void createSampleRentals() {
        if (rentalRepository.count() == 0) {
            var customer = userRepository.findByUsername("customer").orElse(null);
            var vehicle = vehicleRepository.findByLicensePlate("ABC-1234").orElse(null);
            
            if (customer != null && vehicle != null) {
                Rental rental = new Rental();
                rental.setVehicle(vehicle);
                rental.setCustomer(customer);
                rental.setPickupAt(LocalDateTime.now().plusDays(1));
                rental.setDropoffAt(LocalDateTime.now().plusDays(3));
                rental.setTotalAmount(new BigDecimal("135.00"));
                rental.setExpectedKm(200L);
                rental.setPricingStrategy("weekday");
                rental.setPaymentMethod("CASH");
                rentalRepository.save(rental);
            }
        }
    }
    
    private void initializeFlyweightCache() {
        // Pre-populate common vehicle specs to demonstrate flyweight pattern
        vehicleSpecFactory.get("Toyota", "Camry", "2.5L", 5, "Gasoline", "Automatic");
        vehicleSpecFactory.get("Honda", "Civic", "1.8L", 5, "Gasoline", "Manual");
        vehicleSpecFactory.get("Ford", "Explorer", "3.5L", 7, "Gasoline", "Automatic");
        vehicleSpecFactory.get("Nissan", "Altima", "2.0L", 5, "Gasoline", "CVT");
        vehicleSpecFactory.get("Chevrolet", "Tahoe", "5.3L", 8, "Gasoline", "Automatic");
    }
}