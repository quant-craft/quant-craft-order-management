package com.quant.craft.ordermanagement.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeResponseEvent{
    private final Trade trade;
    private final Order order;
}
