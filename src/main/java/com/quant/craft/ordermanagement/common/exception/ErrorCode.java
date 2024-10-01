package com.quant.craft.ordermanagement.common.exception;

public enum ErrorCode {
    EXCHANGE_API_ERROR(500, "거래소 API 오류"),
    EXCHANGE_PARSING_ERROR(500, "거래소 응답 파싱 오류"),
    EXCHANGE_CONNECTION_ERROR(500, "거래소 연결 오류"),

    TRADING_BOT_NOT_FOUND(404, "트레이딩 봇을 찾을 수 없음"),

    API_KEY_NOT_FOUND(404, "API 키를 찾을 수 없음"),

    ORDER_NOT_FOUND_BY_CLIENT_ORDER_ID(404, "주어진 클라이언트 주문 ID로 주문을 찾을 수 없음"),

    POSITION_NOT_FOUND(404, "포지션을 찾을 수 없음"),

    BINANCE_API_ERROR(500, "바이낸스 API 오류 발생"),
    BINANCE_WEBSOCKET_ERROR(500, "바이낸스 웹소켓 오류 발생"),
    BINANCE_ORDER_PROCESSING_ERROR(400, "바이낸스 주문 처리 오류"),
    BINANCE_INVALID_API_CREDENTIALS(401, "유효하지 않은 바이낸스 API 자격 증명"),

    BOT_WEBSOCKET_CONNECTION_ERROR(500, "봇 웹소켓 연결 오류"),
    BOT_WEBSOCKET_DISCONNECTION_ERROR(500, "봇 웹소켓 연결 해제 오류"),
    BOT_WEBSOCKET_LISTEN_KEY_ERROR(500, "봇 웹소켓 ListenKey 오류");

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