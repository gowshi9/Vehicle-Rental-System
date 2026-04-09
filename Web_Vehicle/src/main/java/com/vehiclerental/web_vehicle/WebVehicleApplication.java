package com.vehiclerental.web_vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.vehiclerental")
@EnableJpaRepositories(basePackages = "com.vehiclerental.common.repository")
@EntityScan(basePackages = "com.vehiclerental.common.entity")
@EnableScheduling
public class WebVehicleApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebVehicleApplication.class, args);
    }
}