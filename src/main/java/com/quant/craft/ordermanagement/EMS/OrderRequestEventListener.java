package com.quant.craft.ordermanagement.EMS;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderRequestEventListener {

    private final ExecutionManagementService executionManagementService;

    /**
     * TODO
     * 추후 EMS 서버로 분리해야함 임시적인 메소드
     */
    @EventListener
    public void handleOrderRequestEvent(OrderRequestEvent event) {
        executionManagementService.executeOrder(event.getOrder(),event.getType());
    }
}
