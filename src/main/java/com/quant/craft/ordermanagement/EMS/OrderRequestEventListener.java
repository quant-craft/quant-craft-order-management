package com.quant.craft.ordermanagement.EMS;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRequestEventListener {

    private final ExecutionManagementService executionManagementService;

    @EventListener
    public void handleOrderRequestEvent(OrderRequestEvent event) {
        executionManagementService.executeOrder(event.getOrder(),event.getType());
    }
}
