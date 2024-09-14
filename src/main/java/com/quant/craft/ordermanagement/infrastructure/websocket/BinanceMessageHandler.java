package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.dto.binance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceMessageHandler {
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void handleMessage(String message,Long botId) {
        try {
            BaseBinanceEvent baseEvent = objectMapper.readValue(message, BaseBinanceEvent.class);

            switch (baseEvent.getEventType()) {
                case "TRADE_LITE":
                    TradeLiteEvent tradeLiteEvent = objectMapper.readValue(message, TradeLiteEvent.class);
                    tradeLiteEvent.setBotId(botId);
                    eventPublisher.publishEvent(tradeLiteEvent);
                    break;
                case "ORDER_TRADE_UPDATE":
                    OrderTradeUpdateEvent orderTradeUpdateEvent = objectMapper.readValue(message, OrderTradeUpdateEvent.class);
                    orderTradeUpdateEvent.setBotId(botId);
                    eventPublisher.publishEvent(orderTradeUpdateEvent);
                    break;
                case "ACCOUNT_UPDATE":
                    AccountUpdateEvent accountUpdateEvent = objectMapper.readValue(message, AccountUpdateEvent.class);
                    accountUpdateEvent.setBotId(botId);
                    eventPublisher.publishEvent(accountUpdateEvent);
                    break;
                case "MARGIN_CALL":
                    MarginCallEvent marginCallEvent = objectMapper.readValue(message, MarginCallEvent.class);
                    marginCallEvent.setBotId(botId);
                    eventPublisher.publishEvent(marginCallEvent);
                    break;
                case "ACCOUNT_CONFIG_UPDATE":
                    AccountConfigUpdateEvent accountConfigUpdateEvent = objectMapper.readValue(message, AccountConfigUpdateEvent.class);
                    accountConfigUpdateEvent.setBotId(botId);
                    eventPublisher.publishEvent(accountConfigUpdateEvent);
                    break;
                default:
                    throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BINANCE_WEBSOCKET_ERROR, "Unknown event type: " + baseEvent.getEventType());
            }
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BINANCE_WEBSOCKET_ERROR,
                    "Error processing message: " + e.getMessage());
        }
    }
}