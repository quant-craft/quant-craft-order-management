package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public Order createAndSaveOrder(OrderDto orderDto) {
        validateOrderDto(orderDto);
        Order order = createOrder(orderDto);
        addConditionalOrders(order, orderDto);
        return orderRepository.save(order);
    }

    public Order createOrder(OrderDto orderDto) {
        return Order.builder()
                .orderId(orderDto.getOrderId())
                .botId(orderDto.getBotId())
                .tradingBotId(orderDto.getTradingBotId())
                .symbol(orderDto.getSymbol())
                .exchange(orderDto.getExchange().name())
                .size(orderDto.getSize().abs())
                .price(orderDto.getLimit())
                .leverage(orderDto.getLeverage())
                .type(OrderType.determineOrderType(orderDto.getLimit(), orderDto.getStop()))
                .direction(TradeDirection.determineBySize(orderDto.getSize()))
                .action(OrderAction.OPEN)
                .status(OrderStatus.NONE)
                .processingStatus(ProcessingStatus.PENDING)
                .build();
    }

    public Order createCloseOrder(Position position) {
        return Order.builder()
                .orderId(generateOrderId())
                .botId(position.getBotId())
                .tradingBotId(position.getTradingBotId())
                .symbol(position.getSymbol())
                .exchange(position.getExchange())
                .size(position.getSize())
                .type(OrderType.MARKET)
                .direction(position.getDirection())
                .action(OrderAction.CLOSE)
                .status(OrderStatus.NONE)
                .processingStatus(ProcessingStatus.PENDING)
                .build();
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }

    private void validateOrderDto(OrderDto orderDto) {
        if (orderDto.getSize().compareTo(BigDecimal.ZERO) == 0 || orderDto.getSize() == null) {
            throw new IllegalArgumentException("Order size cannot be zero");
        }
    }

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
    public void updateOrderStatus(Order order, OrderStatus status, ProcessingStatus processingStatus) {
        order.setStatus(status);
        order.setProcessingStatus(processingStatus);
        orderRepository.save(order);
    }

    public List<Order> findOpenOrdersByBotIdAndSymbol(long botId, String symbol) {
        return orderRepository.findOpenOrdersByBotIdAndSymbol(botId, symbol);
    }

}