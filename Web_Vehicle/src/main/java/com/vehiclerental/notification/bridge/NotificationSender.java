package com.vehiclerental.notification.bridge;

public interface NotificationSender {
    void deliver(String to, String subject, String body);
    String getType();
}