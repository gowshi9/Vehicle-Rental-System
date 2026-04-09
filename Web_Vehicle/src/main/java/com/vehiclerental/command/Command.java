package com.vehiclerental.command;

public interface Command<R> {
    R execute();
    String description();
}