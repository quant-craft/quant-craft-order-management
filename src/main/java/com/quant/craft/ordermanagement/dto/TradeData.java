package com.quant.craft.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("trade")
public class TradeData extends MarketData {
    private long timestamp;
    private BigDecimal price;
    private BigDecimal amount;
    private String side;

    @Override
    public String getType() {
        return "trade";
    }
}