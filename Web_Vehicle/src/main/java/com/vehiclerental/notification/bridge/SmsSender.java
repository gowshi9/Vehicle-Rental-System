package com.vehiclerental.notification.bridge;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SmsSender implements NotificationSender {
    
    @Override
    public void deliver(String to, String subject, String body) {
        // Simulate SMS sending
        String smsBody = subject + ": " + body;
        log.info("SMS SENT: To={}, Message={}", to, smsBody.substring(0, Math.min(100, smsBody.length())));
        // In real implementation: Twilio, AWS SNS, etc.
    }
    
    @Override
    public String getType() {
        return "SMS";
    }
}