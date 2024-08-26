package com.quant.craft.ordermanagement.domain;

public enum OrderStatus {
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED;

    public boolean isActive() {
        return this == OPEN || this == PARTIALLY_FILLED;
    }
}
