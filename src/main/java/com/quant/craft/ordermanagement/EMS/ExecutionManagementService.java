package com.quant.craft.ordermanagement.EMS;

import com.quant.craft.ordermanagement.domain.Order;
import com.quant.craft.ordermanagement.domain.OrderResponseEvent;
import com.quant.craft.ordermanagement.domain.Trade;
import com.quant.craft.ordermanagement.dto.OHLCVData;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import com.quant.craft.ordermanagement.service.DataLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final DataLoaderService dataLoaderService;
    private final TradeRepository tradeRepository;

    /**
     * Type을 보고 처리해야하는 로직이 달라져야함. 현재는 바로바로. 테스트중
     * TODO
     * 이후 EMS 서버 분리해야함.
     * 매칭엔진 작성해야함.
     * 주문 순위에 추적해야함.
     */
    @Transactional
    public void executeOrder(Order order, OrderRequestType type) {
        // 최신 시장 데이터 가져오기
        OHLCVData latestData = dataLoaderService.getLatestOHLCVData(order.getExchange(), order.getSymbol());

        // 일단 최신 종가를 사용
        BigDecimal executionPrice = latestData.getClose();

        BigDecimal executionSize = order.getSize();
        //실제 데이터 받아서 넘기기
        Trade trade = executeTrade(order, executionSize, executionPrice);
        tradeRepository.save(trade);

        eventPublisher.publishEvent(new OrderResponseEvent(order, trade));
    }

    private Trade executeTrade(Order order, BigDecimal size, BigDecimal price) {
        return Trade.builder()
                .tradeId(UUID.randomUUID().toString())
                .botId(order.getBotId())
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
