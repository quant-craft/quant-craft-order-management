package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.enums.ProcessingStatus;
import com.quant.craft.ordermanagement.domain.enums.Side;
import com.quant.craft.ordermanagement.domain.order.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.exception.BusinessException;
import com.quant.craft.ordermanagement.exception.ErrorCode;
import com.quant.craft.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order createValidateOrder(OrderDto orderDto) {
        validateOrderDto(orderDto);
        Order order = createOrder(orderDto);
        addConditionalOrders(order, orderDto);
        return order;
    }

    /**
     * 현재는 positionSide 가 무조건 BOTH가 아님.
     * 추후 Hedging mode 추가할 시 수정해야함.
     */
    public Order createOrder(OrderDto orderDto) {
        return Order.builder()
                .orderId(orderDto.getOrderId())
                .tradingBotId(orderDto.getTradingBotId())
                .symbol(orderDto.getSymbol())
                .exchange(orderDto.getExchange())
                .size(orderDto.getSize().abs())
                .price(orderDto.getLimit())
                .leverage(orderDto.getLeverage())
                .type(OrderType.determineOrderType(orderDto.getLimit(), orderDto.getStop()))
                .side(Side.determineBySize(orderDto.getSize()))
                .positionSide(Side.determineBySize(orderDto.getSize()).toPositionSide())
                .action(OrderAction.OPEN)
                .status(OrderStatus.NEW)
                .processingStatus(ProcessingStatus.PENDING)
                .build();
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

    @Transactional(readOnly = true)
    public List<Order> findOpenOrdersByBotIdAndSymbol(long tradingBotId, String symbol) {
        return orderRepository.findOpenOrdersByTradingBotIdAndSymbol(tradingBotId, symbol);
    }

    @Transactional(readOnly = true)
    public Order findOrderByClientOrderId(String clientOrderId) {
        return orderRepository.findByClientOrderIdWithLock(clientOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND_BY_CLIENT_ORDER_ID, "ClientOrderId : " + clientOrderId));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrdersByProcessingStatus(ProcessingStatus processingStatus) {
        return orderRepository.findAllByProcessingStatus(processingStatus);
    }
}