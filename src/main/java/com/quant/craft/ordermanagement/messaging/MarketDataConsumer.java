package com.quant.craft.ordermanagement.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.craft.ordermanagement.dto.MarketData;
import com.quant.craft.ordermanagement.dto.OHLCVData;
import com.quant.craft.ordermanagement.dto.OrderBookData;
import com.quant.craft.ordermanagement.dto.TradeData;
import com.quant.craft.ordermanagement.service.DataLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDataConsumer {

    private final ObjectMapper objectMapper;
    private final DataLoaderService dataLoaderService;

    /**
     * TODO
     * switch case 문 삭제하고 Config파일에서 관리할 수 있도록하기
     */

    @KafkaListener(topics = "${kafka.topic.ohlcv}", groupId = "${kafka.group-id}")
    public void consume(ConsumerRecord<String, String> record) throws IOException {
        MarketData marketData = objectMapper.readValue(record.value(), MarketData.class);

        switch (marketData.getType()) {
            case "ohlcv":
                processOHLCVData((OHLCVData) marketData);
                break;
            case "orderbook":
                processOrderBookData((OrderBookData) marketData);
                break;
            case "trade":
                processTradeData((TradeData) marketData);
                break;
            default:
                log.warn("Unknown market data type: {}", marketData.getType());
        }
    }

    private void processOHLCVData(OHLCVData ohlcvData) {
        dataLoaderService.processOHLCVData(ohlcvData);
    }

    private void processOrderBookData(OrderBookData orderBookData) {
        log.info("Processing Order Book data for {}: {} bids, {} asks",
                orderBookData.getSymbol(),
                orderBookData.getBids().size(),
                orderBookData.getAsks().size());
    }

    private void processTradeData(TradeData tradeData) {
        log.info("Processing Trade data for {}: timestamp={}, price={}, amount={}, side={}",
                tradeData.getSymbol(),
                tradeData.getTimestamp(),
                tradeData.getPrice(),
                tradeData.getAmount(),
                tradeData.getSide());
    }
}
