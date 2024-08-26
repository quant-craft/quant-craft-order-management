package com.quant.craft.ordermanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDto {

    @NotNull
    private String orderId;
    @NotNull
    private Long botId;
    @NotNull
    private Long tradingBotId;
    @NotNull
    private String symbol;
    @NotNull
    private Exchange exchange;
    @NotNull
    private Double size;
    private Double limit;
    private Double stop;
    private Double sl;
    private Double tp;
    private Double leverage;
    private Boolean exclusiveOrders;
    private Boolean hedgeMode;

    // 추후 변경 Config에서 지원하는 Exchange 가져오도록.
    public enum Exchange {
        binance,
        coinbase,
        kraken,
        simulated,
    }
}

