package com.vehiclerental.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public pages
                .requestMatchers("/", "/home", "/about", "/contact", "/vehicles", "/learn-more").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/auth/register", "/api/vehicles").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/payment/paypal/approve", "/payment/paypal/cancel").permitAll()
                
                // Role-based access
                .requestMatchers("/dashboard/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/dashboard/finance/**").hasAuthority("FINANCE")
                .requestMatchers("/dashboard/marketing/**").hasAuthority("MARKETING")
                .requestMatchers("/dashboard/shopmanager/**").hasAuthority("SHOP_MANAGER")
                .requestMatchers("/dashboard/delivery/**").hasAuthority("DELIVERY")
                .requestMatchers("/dashboard/customer/**").hasAuthority("CUSTOMER")
                .requestMatchers("/dashboard").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/payment/process", "/payment/refund/**"));

        return http.build();
    }
}