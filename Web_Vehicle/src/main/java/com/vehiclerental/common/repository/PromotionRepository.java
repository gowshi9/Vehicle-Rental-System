package com.vehiclerental.common.repository;

import com.vehiclerental.common.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCode(String code);
    List<Promotion> findByActive(boolean active);
    Optional<Promotion> findByCodeAndActiveTrue(String code);
    
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND " +
           "p.validFrom <= :currentDate AND p.validTo >= :currentDate")
    List<Promotion> findActivePromotions(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT p FROM Promotion p WHERE p.code = :code AND p.active = true AND " +
           "p.validFrom <= :currentDate AND p.validTo >= :currentDate AND " +
           "(p.maxUsage IS NULL OR p.usageCount < p.maxUsage)")
    Optional<Promotion> findValidPromotion(@Param("code") String code, @Param("currentDate") LocalDate currentDate);
    
    List<Promotion> findByActiveAndShowOnHomepage(boolean active, boolean showOnHomepage);
    
    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.showOnHomepage = true AND " +
           "p.validFrom <= :currentDate AND p.validTo >= :currentDate ORDER BY p.discountPercent DESC")
    List<Promotion> findHomepagePromotions(@Param("currentDate") LocalDate currentDate);
}