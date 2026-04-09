package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findActiveUsers();
    
    List<User> findTop5ByOrderByIdDesc();
    
    List<User> findByUsernameContainingOrEmailContaining(
        String username, String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_DELIVERY' AND u.enabled = true")
    List<User> findAvailableDrivers();
    
    List<User> findByRoles_NameAndEnabledTrue(String roleName);
}