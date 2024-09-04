package com.quant.craft.ordermanagement.EMS;

import com.quant.craft.ordermanagement.domain.Order;
import com.quant.craft.ordermanagement.domain.OrderResponseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExecutionManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final MatchingEngine matchingEngine;


    /**
     * Type을 보고 처리해야하는 로직이 달라져야함. 현재는 바로바로. 테스트중
     * TODO
     * 이후 EMS 서버 분리해야함.
     * 매칭엔진 작성해야함.
     * 주문 순위에 추적해야함.
     * 주문 응답 부분과, Trade 응답 부분을 나눠야함.
     */

    @Transactional
    public void executeOrder(Order order, OrderRequestType type) {
        matchingEngine.match(order);

        eventPublisher.publishEvent(new OrderResponseEvent(order, type));
    }

}
