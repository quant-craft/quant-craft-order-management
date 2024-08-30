package com.quant.craft.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("orderbook")

public class OrderBookData extends MarketData {
    private List<List<BigDecimal>> bids;
    private List<List<BigDecimal>> asks;

    @Override
    public String getType() {
        return "orderbook";
    }
}