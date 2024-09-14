package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeLiteEvent extends BaseBinanceEvent {
    @JsonProperty("s")
    private String symbol;

    @JsonProperty("q")
    private BigDecimal quantity;

    @JsonProperty("p")
    private BigDecimal price;

    @JsonProperty("m")
    private Boolean isMakerSide;

    @JsonProperty("c")
    private String clientOrderId;

    @JsonProperty("S")
    private String side;

    @JsonProperty("L")
    private BigDecimal lastFilledPrice;

    @JsonProperty("l")
    private BigDecimal lastFilledQuantity;

    @JsonProperty("t")
    private Long tradeId;

    @JsonProperty("i")
    private Long orderId;
}