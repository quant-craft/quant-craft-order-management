package com.quant.craft.ordermanagement.domain;

import com.quant.craft.ordermanagement.EMS.OrderRequestType;
import org.springframework.context.ApplicationEvent;

public class OrderResponseEvent extends ApplicationEvent {
    private final Order order;
    private final OrderRequestType type;

    public OrderResponseEvent(Order order, OrderRequestType type) {
        super(order);
        this.order = order;
        this.type = type;
    }

    public Order getOrder() {
        return order;
    }

    public OrderRequestType getType() {
        return type;
    }
}
