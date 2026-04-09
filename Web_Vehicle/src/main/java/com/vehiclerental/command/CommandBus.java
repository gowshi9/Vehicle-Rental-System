package com.vehiclerental.command;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommandBus {
    
    public <R> R execute(Command<R> command) {
        log.info("Executing command: {}", command.description());
        var result = command.execute();
        log.info("Command completed: {}", command.description());
        return result;
    }
}