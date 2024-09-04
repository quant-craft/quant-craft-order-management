package com.quant.craft.ordermanagement.domain;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class TradeResponseEvent extends ApplicationEvent {
    private final Trade trade;
    private final Order order;

    public TradeResponseEvent(Object source, Trade trade, Order order) {
        super(source);
        this.trade = trade;
        this.order = order;
    }

    public Trade getTrade() {
        return trade;
    }

    public Order getOrder() {
        return order;
    }
}
