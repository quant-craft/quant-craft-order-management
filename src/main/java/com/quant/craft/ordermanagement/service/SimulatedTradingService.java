package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.EMS.*;
import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.factory.OrderFactory;
import com.quant.craft.ordermanagement.repository.OrderRepository;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulatedOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final OrderFactory orderFactory;
    private final PositionService positionService;
    private final ExecutionManagementService executionManagementService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문을 처리하는 메인 메서드.
     * 주문 DTO를 받아 주 주문과 필요한 추가 주문들을 생성하고 저장.
     * 현재는 성능, 데이터 일관성 정합성 상관없이 직접 EMS 호출하여 executeOrder 수행중
     * mainOrder , additionalOrder 등 순서와 원자성 보장되어야함. 또한 조건부주문도 가능하게 만들어야함.
     * 추후에 EMS 서버 분리해야함
     */
    @Override
    @Transactional
    public void processOrder(OrderDto orderDto) {
        validateOrderDto(orderDto);
        Order mainOrder = orderFactory.createOrder(orderDto);

        handleExclusiveOrders(orderDto);

        addConditionalOrders(mainOrder, orderDto);
        orderRepository.save(mainOrder);

        processAndExecuteOrder(mainOrder, OrderRequestType.OPEN);

        log.info("Processed order: {}", mainOrder.getOrderId());
    }

    /**
     * 현재는 size가 0 혹은 Null인지만 확인
     */
    private void validateOrderDto(OrderDto orderDto) {
        if (orderDto.getSize().compareTo(BigDecimal.ZERO) == 0 || orderDto.getSize() == null) {
            throw new IllegalArgumentException("Order size cannot be zero");
        }
    }

    private void handleExclusiveOrders(OrderDto orderDto) {
        if (Boolean.TRUE.equals(orderDto.getExclusiveOrders())) {
            closeExistingPositions(orderDto);
            cancelExistingOrders(orderDto);
        }
    }

    private void cancelExistingOrders(OrderDto orderDto) {
        List<Order> existingOrders = orderRepository.findOpenOrdersByBotIdAndSymbol(
                orderDto.getBotId(), orderDto.getSymbol());
        existingOrders
                .forEach(order -> processAndExecuteOrder(order, OrderRequestType.CANCEL));
    }


    private void closeExistingPositions(OrderDto orderDto) {
        List<Position> existingPositions = positionService.findExistingPositions(orderDto.getBotId(), orderDto.getSymbol());

        for (Position position : existingPositions) {
            Order closeOrder = orderFactory.createCloseOrder(position);
            processAndExecuteOrder(closeOrder, OrderRequestType.OPEN);
        }
    }
    @Transactional
    public void processAndExecuteOrder(Order order, OrderRequestType requestType) {
        if(requestType != OrderRequestType.CANCEL) orderRepository.save(order);

        eventPublisher.publishEvent(new OrderRequestEvent(order,requestType));
        log.info("Processed and executed order: {}, Type: {}", order.getOrderId(), requestType);
    }

    /**
     * 주 주문에 조건부 주문(스탑로스, 테이크프로핏)을 추가합니다.
     *
     * @param mainOrder 주 주문 객체
     * @param orderDto 주문 DTO
     */
    private void addConditionalOrders(Order mainOrder, OrderDto orderDto) {
        if (orderDto.getSl() != null) {
            ConditionalOrder slOrder = new ConditionalOrder(ConditionalOrderType.STOP_LOSS, orderDto.getSl());
            mainOrder.addConditionalOrder(slOrder);
        }

        if (orderDto.getTp() != null) {
            ConditionalOrder tpOrder = new ConditionalOrder(ConditionalOrderType.TAKE_PROFIT, orderDto.getTp());
            mainOrder.addConditionalOrder(tpOrder);
        }
    }


    @Transactional
    public void updateOrderStatus(Order order) {
        // EMS로부터 주문 실행 완료 이벤트를 받아 처리
        order.setProcessingStatus(ProcessingStatus.SUBMITTED);
        order.setStatus(OrderStatus.FILLED);
        orderRepository.save(order);
        log.info("Updated order status to FILLED: {}", order.getOrderId());
    }

    @EventListener
    @Transactional
    public void handleTradeExecuted(OrderResponseEvent event) {
        Trade trade = event.getTrade();
        tradeRepository.save(trade);

        Order order = event.getOrder();
        order.setStatus(OrderStatus.FILLED);
        orderRepository.save(order);

        positionService.updatePosition(trade,order.getLeverage());
    }
}
