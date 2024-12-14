package com.quant.craft.ordermanagement.domain.exchange;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum ExchangeType {
    @JsonProperty("binance")
    BINANCE,
    @JsonProperty("coinbase")
    COINBASE,
    @JsonProperty("kraken")
    KRAKEN,
    @JsonProperty("bybit")
    BYBIT,
    @JsonProperty("simulated")
    SIMULATED,
    @JsonProperty("unknown")
    UNKNOWN
}
