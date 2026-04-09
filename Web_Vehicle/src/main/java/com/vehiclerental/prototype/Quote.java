package com.vehiclerental.prototype;

import com.vehiclerental.dto.RentalQuoteInput;
import lombok.Data;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class Quote implements Cloneable {
    
    private RentalQuoteInput input;
    private BigDecimal baseTotal;
    private BigDecimal finalTotal;
    private String pricingStrategy;
    private Map<String, Object> extras = new HashMap<>();
    
    @Override
    public Quote clone() {
        try {
            Quote cloned = (Quote) super.clone();
            cloned.extras = new HashMap<>(this.extras); // Deep copy the map
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    public Quote withStrategy(String strategy) {
        Quote cloned = this.clone();
        cloned.setPricingStrategy(strategy);
        return cloned;
    }
    
    public Quote withExtra(String key, Object value) {
        Quote cloned = this.clone();
        cloned.getExtras().put(key, value);
        return cloned;
    }
}