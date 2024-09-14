package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountConfigUpdateEvent extends BaseBinanceEvent {
    @JsonProperty("ac")
    private AccountConfig accountConfig;

    @JsonProperty("ai")
    private AccountInfo accountInfo;

    @Data
    public static class AccountConfig {
        @JsonProperty("s")
        private String symbol;

        @JsonProperty("l")
        private Integer leverage;
    }

    @Data
    public static class AccountInfo {
        @JsonProperty("j")
        private Boolean isMultiAssetsMode;
    }
}