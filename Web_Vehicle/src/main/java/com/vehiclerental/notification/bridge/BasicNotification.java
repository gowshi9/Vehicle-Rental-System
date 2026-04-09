package com.vehiclerental.notification.bridge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class BasicNotification implements Notification {
    
    private final NotificationSender sender;
    
    @Override
    public void send(String to, String subject, String body) {
        log.info("Sending notification via {}: {} to {}", sender.getType(), subject, to);
        sender.deliver(to, subject, body);
    }
}