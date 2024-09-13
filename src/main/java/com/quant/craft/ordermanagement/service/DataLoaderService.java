package com.quant.craft.ordermanagement.service;
import com.quant.craft.ordermanagement.dto.OHLCVData;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DataLoaderService {

    private final ConcurrentMap<String, OHLCVData> latestOHLCVData = new ConcurrentHashMap<>();

    public void processOHLCVData(OHLCVData data) {
        String key = getKey(data.getExchange(), data.getSymbol());
        latestOHLCVData.put(key, data);
    }

    public OHLCVData getLatestOHLCVData(String exchange, String symbol) {
        exchange = exchange.toLowerCase();
        String key = getKey(exchange, symbol);
        return latestOHLCVData.get(key);
    }

    private String getKey(String exchange, String symbol) {
        exchange = exchange.toLowerCase();
        if(exchange.equals("simulated")) return "binance:" + symbol;
        return exchange + ":" + symbol;
    }

}
