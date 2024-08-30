package com.quant.craft.ordermanagement.factory;

import com.quant.craft.ordermanagement.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderServiceFactory {

    private final Map<String, TradingService> orderServices;

    public TradingService getOrderService(String exchange) {
        TradingService service = orderServices.get(exchange.toLowerCase() + "TradingService");
        if (service == null) {
            throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        }
        return service;
    }
}
