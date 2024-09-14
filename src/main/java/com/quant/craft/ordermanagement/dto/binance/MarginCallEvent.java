package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MarginCallEvent extends BaseBinanceEvent {
    @JsonProperty("cw")
    private BigDecimal crossWalletBalance;

    @JsonProperty("p")
    private List<MarginCallPosition> positions;

    @Data
    public static class MarginCallPosition {
        @JsonProperty("s")
        private String symbol;

        @JsonProperty("ps")
        private String positionSide;

        @JsonProperty("pa")
        private BigDecimal positionAmount;

        @JsonProperty("mt")
        private String marginType;

        @JsonProperty("iw")
        private String isolatedWallet;

        @JsonProperty("mp")
        private BigDecimal markPrice;

        @JsonProperty("up")
        private BigDecimal unrealizedPnL;

        @JsonProperty("mm")
        private String maintenanceMarginRequired;
    }
}