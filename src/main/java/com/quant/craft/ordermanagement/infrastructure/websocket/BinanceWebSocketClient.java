package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.craft.ordermanagement.domain.order.Order;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceWebSocketClient extends TextWebSocketHandler {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${binance.futures.ws.url}")
    private String wsUrl;

    private WebSocketSession session;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        connect();
    }

    public void connect() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            this.session = client.execute(this, new WebSocketHttpHeaders(), URI.create(wsUrl)).get();
            log.info("웹소켓 연결이 설정되었습니다.");
        } catch (InterruptedException | ExecutionException e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BOT_WEBSOCKET_CONNECTION_ERROR, "웹소켓 연결 초기화 실패");
        }
    }

    public void placeOrder(String apiKey, String secretKey, Order order, String UUID) {
        validateSession();

        long timestamp = System.currentTimeMillis();
        TreeMap<String, String> params = buildOrderParams(apiKey, timestamp, order);

        String queryString = buildQueryString(params);
        String signature = generateSignature(queryString, secretKey);
        params.put("signature", signature);

        String request = buildOrderRequest(UUID, params);

        sendMessage(request);
    }

    public void requestAllPositionsInformation(String apiKey, String secretKey, String UUID) {
        validateSession();

        long timestamp = System.currentTimeMillis();
        TreeMap<String, String> params = buildBaseParams(apiKey, timestamp);

        String queryString = buildQueryString(params);
        String signature = generateSignature(queryString, secretKey);
        params.put("signature", signature);

        String request = buildPositionsRequest(UUID, params);

        sendMessage(request);
    }

    public void checkMultiAssetMode(String apiKey, String secretKey, String UUID) {
        validateSession();

        long timestamp = System.currentTimeMillis();
        TreeMap<String, String> params = buildBaseParams(apiKey, timestamp);

        String queryString = buildQueryString(params);
        String signature = generateSignature(queryString, secretKey);
        params.put("signature", signature);

        String request = buildAccountStatusRequest(UUID, params);

        sendMessage(request);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("웹소켓 전송 오류: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("웹소켓 연결이 종료되었습니다: {}", status);
        this.session = null;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("메시지 수신: {}", message.getPayload());
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    private void validateSession() {
        if (session == null || !session.isOpen()) {
            log.warn("세션이 열려있지 않습니다. 재연결을 시도합니다!");
            connect();
        }
    }

    private TreeMap<String, String> buildBaseParams(String apiKey, long timestamp) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("apiKey", apiKey);
        params.put("timestamp", String.valueOf(timestamp));
        return params;
    }

    private TreeMap<String, String> buildOrderParams(String apiKey, long timestamp, Order order) {
        TreeMap<String, String> params = buildBaseParams(apiKey, timestamp);
        params.put("symbol", order.getSymbol());
        params.put("side", order.getSide().toString());
        params.put("type", order.getType().toString());
        params.put("quantity", order.getSize().toString());
        if (order.getPrice() != null) {
            params.put("price", order.getPrice().toString());
        }
        params.put("newClientOrderId", order.getClientOrderId());
        return params;
    }

    private String buildQueryString(TreeMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(params.get(key));
        }
        return sb.toString();
    }

    private String generateSignature(String data, String secretKey) {
        try {
            Mac sha256HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256HMAC.init(secretKeySpec);
            byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_API_ERROR, "서명 생성 실패");
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String buildOrderRequest(String UUID, TreeMap<String, String> params) {
        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            return String.format("{\"id\":\"%s\",\"method\":\"order.place\",\"params\":%s}", UUID, paramsJson);
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_PARSING_ERROR, "주문 요청 생성 실패");
        }
    }

    private String buildPositionsRequest(String UUID, TreeMap<String, String> params) {
        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            return String.format("{\"id\":\"%s\",\"method\":\"account.position\",\"params\":%s}", UUID, paramsJson);
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_PARSING_ERROR, "포지션 정보 요청 생성 실패");
        }
    }

    private String buildAccountStatusRequest(String UUID, TreeMap<String, String> params) {
        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            return String.format("{\"id\":\"%s\",\"method\":\"account.status\",\"params\":%s}", UUID, paramsJson);
        } catch (Exception e) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_PARSING_ERROR, "계정 상태 요청 생성 실패");
        }
    }

    private void sendMessage(String request) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                session.sendMessage(new TextMessage(request));
                log.info("요청이 성공적으로 전송되었습니다.");
                return;
            } catch (Exception e) {
                log.warn("메시지 전송 실패 (시도 {}/{}): {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());

                if (attempt == MAX_RETRY_ATTEMPTS) {
                    log.error("최대 재시도 횟수 초과. 메시지 전송 실패");
                    throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.EXCHANGE_API_ERROR, "메시지 전송 실패");
                }

            }
        }
    }
}