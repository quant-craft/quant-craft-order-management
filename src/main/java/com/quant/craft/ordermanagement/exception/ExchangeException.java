package com.quant.craft.ordermanagement.exception;


import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;

public class ExchangeException extends BusinessException {
    private final ExchangeType exchangeType;

    public ExchangeException(ExchangeType exchangeType, ErrorCode errorCode) {
        super(errorCode);
        this.exchangeType = exchangeType;
    }

    public ExchangeException(ExchangeType exchangeType, ErrorCode errorCode, String detail) {
        super(errorCode, detail);
        this.exchangeType = exchangeType;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }
}
