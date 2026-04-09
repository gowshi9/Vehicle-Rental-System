package com.vehiclerental.config;

import com.vehiclerental.strategy.pricing.PricingStrategy;
import com.vehiclerental.strategy.payment.PaymentStrategy;
import com.vehiclerental.strategy.allocation.AllocationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class StrategyRegistry {
    
    @Bean
    public Map<String, PricingStrategy> pricingStrategies(List<PricingStrategy> strategies) {
        return strategies.stream()
            .collect(Collectors.toMap(PricingStrategy::key, Function.identity()));
    }
    
    @Bean
    public Map<String, PaymentStrategy> paymentStrategies(List<PaymentStrategy> strategies) {
        return strategies.stream()
            .collect(Collectors.toMap(PaymentStrategy::key, Function.identity()));
    }
    
    @Bean
    public Map<String, AllocationStrategy> allocationStrategies(List<AllocationStrategy> strategies) {
        return strategies.stream()
            .collect(Collectors.toMap(AllocationStrategy::key, Function.identity()));
    }
}