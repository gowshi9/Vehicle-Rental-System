package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.PaymentCard;
import com.vehiclerental.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByUser(User user);
    List<PaymentCard> findByUserOrderByCreatedAtDesc(User user);
    Optional<PaymentCard> findByUserAndIsDefaultTrue(User user);
}