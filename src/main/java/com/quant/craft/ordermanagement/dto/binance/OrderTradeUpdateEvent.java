package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderTradeUpdateEvent extends BaseBinanceEvent {
    @JsonProperty("o")
    private OrderInfo orderInfo;

    @Data
    public static class OrderInfo {
        @JsonProperty("s")
        private String symbol;

        @JsonProperty("c")
        private String clientOrderId;

        @JsonProperty("S")
        private String side;

        @JsonProperty("o")
        private String orderType;

        @JsonProperty("f")
        private String timeInForce;

        @JsonProperty("q")
        private BigDecimal originalQuantity;

        @JsonProperty("p")
        private BigDecimal price;

        @JsonProperty("ap")
        private BigDecimal averagePrice;

        @JsonProperty("sp")
        private BigDecimal stopPrice;

        @JsonProperty("x")
        private String executionType;

        @JsonProperty("X")
        private String orderStatus;

        @JsonProperty("i")
        private Long orderId;

        @JsonProperty("l")
        private BigDecimal lastFilledQuantity;

        @JsonProperty("z")
        private BigDecimal filledAccumulatedQuantity;

        @JsonProperty("L")
        private BigDecimal lastFilledPrice;

        @JsonProperty("N")
        private String commissionAsset;

        @JsonProperty("n")
        private BigDecimal commission;

        @JsonProperty("T")
        private Long orderTradeTime;

        @JsonProperty("t")
        private Long tradeId;

        @JsonProperty("b")
        private String bidsNotional;

        @JsonProperty("a")
        private String askNotional;

        @JsonProperty("m")
        private Boolean isMaker;

        @JsonProperty("R")
        private Boolean isReduceOnly;

        @JsonProperty("wt")
        private String stopPriceWorkingType;

        @JsonProperty("ot")
        private String originalOrderType;

        @JsonProperty("ps")
        private String positionSide;

        @JsonProperty("cp")
        private Boolean isCloseAll;

        @JsonProperty("AP")
        private BigDecimal activationPrice;

        @JsonProperty("cr")
        private BigDecimal callbackRate;

        @JsonProperty("pP")
        private Boolean isPriceProtection;

        @JsonProperty("si")
        private Integer selfTradePreventionId;

        @JsonProperty("ss")
        private Integer selfTradePreventionQuantity;

        @JsonProperty("rp")
        private BigDecimal realizedProfit;

        @JsonProperty("V")
        private String stpMode;

        @JsonProperty("pm")
        private String priceMatchMode;

        @JsonProperty("gtd")
        private Long gtdOrderCancelTime;
    }
}