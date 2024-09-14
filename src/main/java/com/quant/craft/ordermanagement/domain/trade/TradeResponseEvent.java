package com.quant.craft.ordermanagement.domain.trade;

import com.quant.craft.ordermanagement.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeResponseEvent{
    private final Trade trade;
    private final Order order;
}
