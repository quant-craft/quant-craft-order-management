package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.EMS.OrderRequestEvent;
import com.quant.craft.ordermanagement.EMS.OrderRequestType;
import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulatedTradingService implements TradingService {

    private final OrderService orderService;
    private final PositionService positionService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * TODO
     *  현재 Order를 각각 따로 보내고있지만, 해당 요청이 오는 순간 모든 Order를 정상적으로 커밋해야함.
     *  즉, 1 Consume -> N Produce 를 원자적으로 수행해야할 필요가 있음.
     *  현실적으로 가능할지는 모름..
     */

    @Override
    @Transactional
    public void processOrder(OrderDto orderDto) {
        Order mainOrder = orderService.createValidateOrder(orderDto);

        processAndExecuteOrder(mainOrder, OrderRequestType.OPEN);

        log.info("Processed order: {}", mainOrder.getOrderId());
    }


    public void processAndExecuteOrder(Order order, OrderRequestType requestType) {
        eventPublisher.publishEvent(new OrderRequestEvent(order, requestType));
        log.info("Processed and executed order: {}, Type: {}", order.getOrderId(), requestType);
    }

    /**
     * TODO
     * - Trade 정보를 보고 Position을 생성하거나 없애거나 삭제하거나 업데이트 해야함.
     * - 그리고 Trade가 없다면 Order에 대해 변경된 것일거임. 수정 취소 생성.
     *  - 부분 filled 추가 해야함
     *  - 원자적으로 수행되어야함.
     */

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTradeExecuted(TradeResponseEvent event) {
        Trade trade = event.getTrade();

        Order order = event.getOrder();
        orderService.updateOrderStatus(order, OrderStatus.FILLED, ProcessingStatus.COMPLETED);

        positionService.updatePosition(
                trade.getTradingBotId(),
                trade.getSymbol(),
                trade.getExchange(),
                trade.getSide(),
                trade.getExecutedSize(),
                trade.getExecutedPrice(),
                trade.getAction(),
                order.getLeverage()
        );
    }

    /**
     * Event를 발생시키는 주체가 트랜잭션이 완료되고 나서 수행된다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderExecuted(OrderResponseEvent event) {
        Order order = event.getOrder();
        OrderRequestType type = event.getType();

        if (type == OrderRequestType.OPEN) {
            order.setProcessingStatus(ProcessingStatus.COMPLETED);
            orderService.saveOrder(order);
        }

        if (type == OrderRequestType.CANCEL) {
            orderService.updateOrderStatus(order, OrderStatus.CANCELED, ProcessingStatus.COMPLETED);
        }

        /**
         * 현재 업데이트할 일은 없긴함.
         */

        if (type == OrderRequestType.UPDATE) {
            log.info("업데이트 ");
        }
    }
}