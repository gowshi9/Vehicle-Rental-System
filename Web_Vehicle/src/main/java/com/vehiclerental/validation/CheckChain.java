package com.vehiclerental.validation;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CheckChain {
    
    private final List<BookingCheck> checks;
    
    public CheckChain(List<BookingCheck> checks) {
        this.checks = checks;
    }
    
    public void run(BookingContext ctx) {
        runAt(0, ctx);
    }
    
    private void runAt(int index, BookingContext ctx) {
        if (index == checks.size()) return;
        checks.get(index).check(ctx, () -> runAt(index + 1, ctx));
    }
}