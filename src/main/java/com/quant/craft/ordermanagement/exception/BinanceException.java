package com.quant.craft.ordermanagement.exception;

public class BinanceException extends BusinessException {

    public BinanceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BinanceException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}