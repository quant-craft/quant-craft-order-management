package com.quant.craft.ordermanagement.domain;

import java.math.BigDecimal;

public enum TradeDirection {
    LONG,SHORT;

    public static TradeDirection determineBySize(BigDecimal size) {
        if (size == null) {
            throw new IllegalArgumentException("Size cannot be null");
        }

        return (size.compareTo(BigDecimal.ZERO) >= 0) ? LONG : SHORT;
    }
}
