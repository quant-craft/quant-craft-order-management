package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SimulatedOrderService implements OrderService{
    @Override
    public void processOrder(OrderDto orderDto) {
        log.info("SimulatedOrderService - ProcessOrder");
    }
}
