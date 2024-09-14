package com.quant.craft.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDto {

    @NotNull
    @JsonProperty("order_id")
    private String orderId;
    @NotNull
    @JsonProperty("bot_id")
    private Long botId;
    @NotNull
    @JsonProperty("trading_bot_id")
    private Long tradingBotId;
    @NotNull
    private String symbol;
    @NotNull
    private ExchangeType exchange;
    @NotNull
    private BigDecimal size;
    private BigDecimal limit;
    private BigDecimal stop;
    private BigDecimal sl;
    private BigDecimal tp;
    private int leverage;
    @JsonProperty("exclusive_orders")
    private Boolean exclusiveOrders;
    @JsonProperty("hedge_mode")
    private Boolean hedgeMode;
}

