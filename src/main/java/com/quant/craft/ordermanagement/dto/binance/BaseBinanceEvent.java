package com.quant.craft.ordermanagement.dto.binance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseBinanceEvent {
    @JsonProperty("e")
    private String eventType;

    @JsonProperty("E")
    private Long eventTime;

    @JsonProperty("T")
    private Long transactionTime;

    private Long botId;
}
