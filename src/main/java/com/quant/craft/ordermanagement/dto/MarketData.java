package com.quant.craft.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OHLCVData.class, name = "ohlcv"),
        @JsonSubTypes.Type(value = OrderBookData.class, name = "orderbook"),
        @JsonSubTypes.Type(value = TradeData.class, name = "trade")
})
public abstract class MarketData {
    private String exchange;
    private String symbol;
    public abstract String getType();

}
