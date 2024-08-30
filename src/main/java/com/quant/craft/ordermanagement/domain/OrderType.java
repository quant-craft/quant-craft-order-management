package com.quant.craft.ordermanagement.domain;

import java.math.BigDecimal;

public enum OrderType {
    MARKET,
    LIMIT,
    STOP_MARKET,
    STOP_LIMIT,
    LIQUIDATION;

    public static OrderType determineOrderType(BigDecimal limitPrice, BigDecimal stopPrice) {

        if (isLimitOrder(limitPrice)) {
            return isStopOrder(stopPrice) ? STOP_LIMIT : LIMIT;
        } else if (isStopOrder(stopPrice)) {
            return STOP_MARKET;
        } else {
            return MARKET;
        }
    }

    private static boolean isLimitOrder(BigDecimal limitPrice) {
        return limitPrice != null && limitPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean isStopOrder(BigDecimal stopPrice) {
        return stopPrice != null && stopPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}
