package com.quant.craft.ordermanagement.domain;

public enum OrderPosition {
    OPEN_LONG,
    OPEN_SHORT,
    CLOSE_LONG,
    CLOSE_SHORT;

    public boolean isLong() {
        return this == OPEN_LONG || this == CLOSE_SHORT;
    }

    public boolean isShort() {
        return this == OPEN_SHORT || this == CLOSE_LONG;
    }

    public boolean isOpening() {
        return this == OPEN_LONG || this == OPEN_SHORT;
    }

    public boolean isClosing() {
        return this == CLOSE_LONG || this == CLOSE_SHORT;
    }
}