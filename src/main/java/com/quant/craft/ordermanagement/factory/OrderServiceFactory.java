package com.quant.craft.ordermanagement.factory;

import com.quant.craft.ordermanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderServiceFactory {

    private final Map<String, OrderService> orderServices;

    public OrderService getOrderService(String exchange) {
        OrderService service = orderServices.get(exchange.toLowerCase() + "OrderService");
        if (service == null) {
            throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        }
        return service;
    }
}
