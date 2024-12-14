package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.dto.binance.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceMessageHandler {
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.trading-events}")
    private String tradingEventsTopic;

    public void handleMessage(String message, Long botId) {
        try {
            BaseBinanceEvent baseEvent = objectMapper.readValue(message, BaseBinanceEvent.class);

            switch (baseEvent.getEventType()) {
                case "TRADE_LITE":
                    TradeLiteEvent tradeLiteEvent = objectMapper.readValue(message, TradeLiteEvent.class);
                    tradeLiteEvent.setBotId(botId);
                    publishToKafkaAndEventBus(tradeLiteEvent,botId);
                    break;
                case "ORDER_TRADE_UPDATE":
                    OrderTradeUpdateEvent orderTradeUpdateEvent = objectMapper.readValue(message, OrderTradeUpdateEvent.class);
                    orderTradeUpdateEvent.setBotId(botId);
                    publishToKafkaAndEventBus(orderTradeUpdateEvent,botId);
                    break;
                case "ACCOUNT_UPDATE":
                    AccountUpdateEvent accountUpdateEvent = objectMapper.readValue(message, AccountUpdateEvent.class);
                    accountUpdateEvent.setBotId(botId);
                    publishToKafkaAndEventBus(accountUpdateEvent,botId);
                    break;
                case "MARGIN_CALL":
                    MarginCallEvent marginCallEvent = objectMapper.readValue(message, MarginCallEvent.class);
                    marginCallEvent.setBotId(botId);
                    publishToKafkaAndEventBus(marginCallEvent,botId);
                    break;
                case "ACCOUNT_CONFIG_UPDATE":
                    AccountConfigUpdateEvent accountConfigUpdateEvent = objectMapper.readValue(message, AccountConfigUpdateEvent.class);
                    accountConfigUpdateEvent.setBotId(botId);
                    publishToKafkaAndEventBus(accountConfigUpdateEvent,botId);
                    break;
                default:
                    throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BINANCE_WEBSOCKET_ERROR,
                            "Unknown event type: " + baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BINANCE_WEBSOCKET_ERROR,
                    "Error processing message: " + e.getMessage());
        }
    }
    //

    private void publishToKafkaAndEventBus(Object event,Long partitionKey) {
        try {
            // 1. Event 발행 - DB 업데이트 - 동기적으로 실행. 현재 스레드에서.
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EVENT_PUBLISHING_ERROR,
                    "Error publishing event: " + e.getMessage());
        }

        // 2. Kafka 메시지 발행 - 비동기적으로 실행. 다른 스레드에서. - Kafka 발행 실패시에도 DB 작업은 커밋 가능케 해야함.
        // 따라서 로그만 남기기.
        kafkaTemplate.send(tradingEventsTopic, partitionKey.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event to Kafka topic {}: {}", tradingEventsTopic, ex.getMessage(), ex);
                    } else {
                        log.debug("Successfully published event to Kafka topic {}: {}", tradingEventsTopic, event);
                    }
                });
    }
}