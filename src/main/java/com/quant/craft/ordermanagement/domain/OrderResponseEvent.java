package com.quant.craft.ordermanagement.domain;

import org.springframework.context.ApplicationEvent;

public class OrderResponseEvent extends ApplicationEvent {
    private final Order order;
    private final Trade trade;

    public OrderResponseEvent(Order order, Trade trade) {
        super(order);
        this.order = order;
        this.trade = trade;
    }

    public Order getOrder() {
        return order;
    }
    public Trade getTrade(){
        return trade;
    }
}
