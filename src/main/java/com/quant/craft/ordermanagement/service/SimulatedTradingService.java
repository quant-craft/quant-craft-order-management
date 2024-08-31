package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.EMS.*;
import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulatedTradingService implements TradingService {

    private final OrderService orderService;
    private final PositionService positionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void processOrder(OrderDto orderDto) {
        Order mainOrder = orderService.createAndSaveOrder(orderDto);

        handleExclusiveOrders(orderDto);

        processAndExecuteOrder(mainOrder, OrderRequestType.OPEN);

        log.info("Processed order: {}", mainOrder.getOrderId());
    }

    private void handleExclusiveOrders(OrderDto orderDto) {
        if (Boolean.TRUE.equals(orderDto.getExclusiveOrders())) {
            closeExistingPositions(orderDto);
            cancelExistingOrders(orderDto);
        }
    }

    private void cancelExistingOrders(OrderDto orderDto) {
        List<Order> existingOrders = orderService.findOpenOrdersByBotIdAndSymbol(
                orderDto.getBotId(), orderDto.getSymbol());
        existingOrders.forEach(order ->
                processAndExecuteOrder(order, OrderRequestType.CANCEL)
        );
    }

    private void closeExistingPositions(OrderDto orderDto) {
        List<Position> existingPositions = positionService.findExistingPositions(orderDto.getBotId(), orderDto.getSymbol());

        for (Position position : existingPositions) {
            Order closeOrder = orderService.createCloseOrder(position);
            processAndExecuteOrder(closeOrder, OrderRequestType.OPEN);
        }
    }

    @Transactional
    public void processAndExecuteOrder(Order order, OrderRequestType requestType) {
        eventPublisher.publishEvent(new OrderRequestEvent(order, requestType));
        log.info("Processed and executed order: {}, Type: {}", order.getOrderId(), requestType);
    }

    /**
     * TODO
     * - Trade 정보를 보고 Position을 생성하거나 없애거나 삭제하거나 업데이트 해야함.
     * - 그리고 Trade가 없다면 Order에 대해 변경된 것일거임. 수정 취소 생성.
     *  - 부분 filled 추가 해야함
     */

    @EventListener
    @Transactional
    public void handleTradeExecuted(OrderResponseEvent event) {
        Trade trade = event.getTrade();

        Order order = event.getOrder();
        orderService.updateOrderStatus(order, OrderStatus.FILLED, ProcessingStatus.COMPLETED);

        positionService.updatePosition(trade, order.getLeverage());
    }
}