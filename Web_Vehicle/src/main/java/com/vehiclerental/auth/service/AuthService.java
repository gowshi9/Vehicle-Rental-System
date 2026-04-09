package com.vehiclerental.auth.service;

import com.vehiclerental.auth.dto.UserRegistrationDto;
import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Role;
import com.vehiclerental.common.repository.UserRepository;
import com.vehiclerental.common.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerCustomer(UserRegistrationDto userDto) {
        // Validate passwords match
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.setRoles(Set.of(customerRole));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("New customer registered: {}", savedUser.getUsername());
        
        return savedUser;
    }
}