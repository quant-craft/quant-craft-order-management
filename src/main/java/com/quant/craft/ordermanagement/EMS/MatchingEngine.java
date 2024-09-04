package com.quant.craft.ordermanagement.EMS;

import com.quant.craft.ordermanagement.domain.Order;
import com.quant.craft.ordermanagement.domain.Trade;
import com.quant.craft.ordermanagement.domain.TradeResponseEvent;
import com.quant.craft.ordermanagement.dto.OHLCVData;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import com.quant.craft.ordermanagement.service.DataLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private final DataLoaderService dataLoaderService;
    private final TradeRepository tradeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * TODO
     * 현재는 무조건 시장가로 거래가 되지만 추후에 로직필요
     */
    @Transactional
    public void match(Order order) {
        OHLCVData latestData = dataLoaderService.getLatestOHLCVData(order.getExchange(), order.getSymbol());
        BigDecimal executionPrice = latestData.getClose();

        Trade trade = executeTrade(order, order.getSize(), executionPrice);
        trade = tradeRepository.save(trade);

        eventPublisher.publishEvent(new TradeResponseEvent(trade,order));
    }

    private Trade executeTrade(Order order, BigDecimal size, BigDecimal price) {
        return Trade.builder()
                .tradeId(UUID.randomUUID().toString())
                .tradingBotId(order.getTradingBotId())
                .orderId(order.getOrderId())
                .symbol(order.getSymbol())
                .exchange(order.getExchange())
                .executedSize(size)
                .executedPrice(price)
                .direction(order.getDirection())
                .action(order.getAction())
                .build();
    }
}
