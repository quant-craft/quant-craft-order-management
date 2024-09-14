package com.quant.craft.ordermanagement.common.exception;

public enum ErrorCode {
    INVALID_INPUT(400, "Invalid input"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),

    EXCHANGE_API_ERROR(500, "Exchange API error"),
    EXCHANGE_PARSING_ERROR(500, "Exchange response parsing error"),
    EXCHANGE_CONNECTION_ERROR(500, "Exchange connection error"),

    TRADING_BOT_NOT_FOUND(404, "Trading bot not found"),

    API_KEY_NOT_FOUND(404, "API key not found"),

    ORDER_NOT_FOUND_BY_CLIENT_ORDER_ID(404, "Order not found with the given client order ID"),

    POSITION_NOT_FOUND(404, "Position not found"),

    BINANCE_API_ERROR(500, "Binance API error occurred"),
    BINANCE_WEBSOCKET_ERROR(500, "Binance WebSocket error occurred"),
    BINANCE_ORDER_PROCESSING_ERROR(400, "Error processing Binance order"),
    BINANCE_INVALID_API_CREDENTIALS(401, "Invalid Binance API credentials");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}