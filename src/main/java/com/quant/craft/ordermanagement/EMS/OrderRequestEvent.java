package com.quant.craft.ordermanagement.EMS;

import com.quant.craft.ordermanagement.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequestEvent {
    private Order order;
    private OrderRequestType type;
}
