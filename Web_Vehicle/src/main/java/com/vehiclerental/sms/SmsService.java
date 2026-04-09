package com.vehiclerental.sms;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    
    public boolean sendSms(String phoneNumber, String message) {
        // Mock SMS implementation - replace with actual SMS provider (Twilio, AWS SNS, etc.)
        System.out.println("SMS sent to " + phoneNumber + ": " + message);
        return true;
    }
}