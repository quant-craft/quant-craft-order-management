package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.dto.OrderDto;

public interface TradingService {
    void processOrder(OrderDto orderDto);
}
