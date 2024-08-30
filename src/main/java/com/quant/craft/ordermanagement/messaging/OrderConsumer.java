package com.quant.craft.ordermanagement.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.craft.ordermanagement.dto.OrderDto;
import com.quant.craft.ordermanagement.factory.OrderServiceFactory;
import com.quant.craft.ordermanagement.service.TradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderServiceFactory orderServiceFactory;
    private final ObjectMapper objectMapper;

    // 나중에 설정 주입하기.

    /**
     * TODO
     * 적어도 한번 + 멱등성 통해서 데이터 일관성 유지하기.
     * topics,groupId Config 파일에서 관리하기.
     */
    @KafkaListener(topics = "new_orders", groupId = "oms-group")
    public void consumeOrder(String message) {
        try {
            OrderDto orderDto = objectMapper.readValue(message, OrderDto.class);
            log.info(String.valueOf(orderDto));
            TradingService tradingService = orderServiceFactory.getOrderService(orderDto.getExchange().name());
            tradingService.processOrder(orderDto);
        } catch (Exception e) {
            // 로깅 및 에러 처리 어떻게 할지 고민
            e.printStackTrace();
        }
    }
}
