package com.vehiclerental.notification.bridge;

public interface Notification {
    void send(String to, String subject, String body);
}