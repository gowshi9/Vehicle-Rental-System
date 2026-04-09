package com.vehiclerental.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@Slf4j
public class DatabaseHealthCheck implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            log.info("✅ Database connection successful!");
            log.info("Database URL: {}", connection.getMetaData().getURL());
            log.info("Database Product: {}", connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            log.error("❌ Database connection failed: {}", e.getMessage());
            throw e;
        }
    }
}