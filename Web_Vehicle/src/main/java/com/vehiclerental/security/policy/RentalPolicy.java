package com.vehiclerental.security.policy;

import com.vehiclerental.common.entity.User;
import com.vehiclerental.common.entity.Rental;
import org.springframework.stereotype.Component;

@Component
public class RentalPolicy {
    
    public boolean canCancel(User user, Rental rental) {
        return isAdmin(user) || 
               (rental.getCustomer().getId().equals(user.getId()) && isCancelable(rental));
    }
    
    public boolean canModify(User user, Rental rental) {
        return isAdmin(user) || isShopManager(user);
    }
    
    public boolean canViewDetails(User user, Rental rental) {
        return isAdmin(user) || 
               isShopManager(user) || 
               rental.getCustomer().getId().equals(user.getId());
    }
    
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
    }
    
    private boolean isShopManager(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> "ROLE_SHOP_MANAGER".equals(role.getName()));
    }
    
    private boolean isCancelable(Rental rental) {
        return "CREATED".equals(rental.getStatus()) || "PAID".equals(rental.getStatus());
    }
}