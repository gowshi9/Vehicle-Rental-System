package com.vehiclerental.notification.bridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailSender implements NotificationSender {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@driveease.com}")
    private String fromEmail;
    
    @Override
    public void deliver(String to, String subject, String body) {
        try {
            if (mailSender != null && isValidEmail(to)) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                
                mailSender.send(message);
                log.info("Email sent successfully to: {}", to);
            } else {
                // Fallback to logging when mail sender is not configured
                log.info("EMAIL SIMULATION - To: {}, Subject: {}", to, subject);
            }
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}