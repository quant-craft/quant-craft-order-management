package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.dto.OrderDto;

public interface OrderService {
    void processOrder(OrderDto orderDto);
}
