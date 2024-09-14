package com.quant.craft.ordermanagement.domain.enums;

import java.math.BigDecimal;

public enum Side {
    BUY,SELL;

    public static Side fromString(String side) {
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }

        switch (side.toUpperCase()) {
            case "BUY":
                return BUY;
            case "SELL":
                return SELL;
            default:
                throw new IllegalArgumentException("Unknown side: " + side);
        }
    }

    public static Side determineBySize(BigDecimal size) {
        if (size == null) {
            throw new IllegalArgumentException("Size cannot be null");
        }

        return (size.compareTo(BigDecimal.ZERO) >= 0) ? BUY : SELL;
    }

    public PositionSide toPositionSide() {
        switch (this) {
            case BUY:
                return PositionSide.LONG;
            case SELL:
                return PositionSide.SHORT;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
