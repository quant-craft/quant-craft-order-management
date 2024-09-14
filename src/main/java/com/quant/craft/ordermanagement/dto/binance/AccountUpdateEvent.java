package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountUpdateEvent extends BaseBinanceEvent {
    @JsonProperty("a")
    private AccountInfo accountInfo;

    @Data
    public static class AccountInfo {
        @JsonProperty("B")
        private List<Balance> balances;

        @JsonProperty("P")
        private List<Position> positions;

        @JsonProperty("m")
        private String eventReasonType;
    }

    @Data
    public static class Balance {
        @JsonProperty("a")
        private String asset;

        @JsonProperty("wb")
        private BigDecimal walletBalance;

        @JsonProperty("cw")
        private BigDecimal crossWalletBalance;

        @JsonProperty("bc")
        private BigDecimal balanceChange;
    }

    @Data
    public static class Position {
        @JsonProperty("s")
        private String symbol;

        @JsonProperty("pa")
        private BigDecimal positionAmount;

        @JsonProperty("ep")
        private BigDecimal entryPrice;

        @JsonProperty("bep")
        private BigDecimal breakEvenPrice;

        @JsonProperty("cr")
        private BigDecimal accumulatedRealized;

        @JsonProperty("up")
        private BigDecimal unrealizedPnL;

        @JsonProperty("mt")
        private String marginType;

        @JsonProperty("iw")
        private BigDecimal isolatedWallet;

        @JsonProperty("ps")
        private String positionSide;
    }
}