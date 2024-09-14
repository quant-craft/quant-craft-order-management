package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.dto.binance.AccountUpdateEvent;
import com.quant.craft.ordermanagement.dto.binance.OrderTradeUpdateEvent;
import com.quant.craft.ordermanagement.dto.binance.TradeLiteEvent;
import com.quant.craft.ordermanagement.exception.BinanceException;
import com.quant.craft.ordermanagement.exception.ErrorCode;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import com.quant.craft.ordermanagement.socket.BinanceWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class BinanceTradingService implements TradingService {

    private final BinanceWebSocketClient webSocketClient;
    private final OrderService orderService;
    private final TradeRepository tradeRepository;
    private final PositionService positionService;
    private final ApiKeyService apiKeyService;

    /**
     * TODO
     * 예외처리 필요.
     */
    @Transactional
    @Override
    public void processOrder(OrderDto orderDto) {
        log.info("BinanceTradingService - ProcessOrder");
        ExchangeApiKey apiKey = apiKeyService.getApiKey(orderDto.getTradingBotId());
        validateApiCredentials(apiKey);

        Order validateOrder = orderService.createValidateOrder(orderDto);
        validateOrder.setClientOrderId(generateClientOrderId("ORD-", validateOrder.getOrderId()));
        orderService.saveOrder(validateOrder);

        try {
            webSocketClient.placeOrder(apiKey.getApiKey(), apiKey.getSecretKey(), validateOrder, orderDto.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order", e);
            throw new BinanceException(ErrorCode.BINANCE_ORDER_PROCESSING_ERROR, e.getMessage());
        }
    }

    @EventListener
    @Transactional
    public void handleTradeLite(TradeLiteEvent event) {
        Order order = orderService.findOrderByClientOrderId(event.getClientOrderId());

        Trade trade = createTradeFromEvent(event);
        tradeRepository.save(trade);

        updatePositionUsingPositionService(order, event);
    }

    @EventListener
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public void handleOrderTradeUpdate(OrderTradeUpdateEvent event) {
        Order order = orderService.findOrderByClientOrderId(event.getOrderInfo().getClientOrderId());
        updateOrderStatus(order, event);
        orderService.saveOrder(order);
    }


    @EventListener
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public void handleAccountUpdate(AccountUpdateEvent event) {
        log.info("Received ACCOUNT_UPDATE event");
    }

    private void validateApiCredentials(ExchangeApiKey apiKey) {
        if (apiKey == null || apiKey.getApiKey() == null || apiKey.getSecretKey() == null) {
            throw new BinanceException(ErrorCode.BINANCE_INVALID_API_CREDENTIALS, "API key and secret key are required");
        }
    }

    private String generateClientOrderId(String prefix, String orderId) {
        long timestamp = Instant.now().toEpochMilli();
        String combined = orderId.replace("-", "") + timestamp;
        String encoded = Base64.getEncoder().encodeToString(combined.getBytes())
                .replaceAll("[+/=]", "");
        return (prefix + encoded).substring(0, Math.min(36, (prefix + encoded).length()));
    }

    private void updatePositionUsingPositionService(Order order, TradeLiteEvent event) {
        positionService.updatePosition(
                order.getTradingBotId(),
                event.getSymbol(),
                ExchangeType.BINANCE,
                event.getSide().equals("BUY") ? TradeDirection.LONG : TradeDirection.SHORT,
                event.getQuantity(),
                event.getLastFilledPrice(),
                OrderAction.OPEN,
                order.getLeverage()
        );
    }

    private Trade createTradeFromEvent(TradeLiteEvent event) {
        return Trade.builder()
                .tradeId(event.getTradeId().toString())
                .tradingBotId(event.getBotId())
                .orderId(event.getClientOrderId())
                .symbol(event.getSymbol())
                .exchange(ExchangeType.BINANCE)
                .executedSize(event.getQuantity())
                .executedPrice(event.getLastFilledPrice())
                .direction(event.getSide().equals("BUY") ? TradeDirection.LONG : TradeDirection.SHORT)
                .action(OrderAction.OPEN)
                .build();
    }

    private void updateOrderStatus(Order order, OrderTradeUpdateEvent event) {
        OrderStatus newStatus = mapBinanceStatusToOrderStatus(event.getOrderInfo().getOrderStatus());
        ProcessingStatus newProcessingStatus = mapBinanceExecutionTypeToProcessingStatus(event.getOrderInfo().getExecutionType());
        order.setStatus(newStatus);
        order.setProcessingStatus(newProcessingStatus);
    }

    /**
     * TODO
     * Case 문에 쓰이는 필드 리팩토링 필요
     */

    private OrderStatus mapBinanceStatusToOrderStatus(String binanceStatus) {
        switch (binanceStatus) {
            case "NEW":
                return OrderStatus.OPEN;
            case "PARTIALLY_FILLED":
                return OrderStatus.PARTIALLY_FILLED;
            case "FILLED":
                return OrderStatus.FILLED;
            case "CANCELED":
                return OrderStatus.CANCELED;
            case "EXPIRED":
                return OrderStatus.EXPIRED;
            default:
                throw new BinanceException(ErrorCode.BINANCE_ORDER_PROCESSING_ERROR, "Unknown Binance order status: " + binanceStatus);
        }
    }

    /**
     * TODO
     * Case 문에 쓰이는 필드 리팩토링 필요
     * ProcessingStatus 수정하고 다시 정의할 필요가 있음.
     */
    private ProcessingStatus mapBinanceExecutionTypeToProcessingStatus(String executionType) {
        switch (executionType) {
            case "NEW":
                return ProcessingStatus.SUBMITTED;
            case "CANCELED":
                return ProcessingStatus.CANCELED;
            case "CALCULATED":
                return ProcessingStatus.CANCELED;
            case "EXPIRED":
                return ProcessingStatus.CANCELED;
            case "TRADE":
                return ProcessingStatus.COMPLETED;
            case "AMENDMENT":
                return ProcessingStatus.SUBMITTED;
            default:
                throw new BinanceException(ErrorCode.BINANCE_ORDER_PROCESSING_ERROR, "Unknown Binance execution type: " + executionType);
        }
    }
}