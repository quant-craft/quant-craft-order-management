package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;

/**
 * TODO
 * 추후 다른 거래소가 필요해 질 경우
 * 인터페이스로 두고 각 거래소 에맞게 구현해야함.
 */
@Slf4j
@RequiredArgsConstructor
public class BotWebSocketConnection implements WebSocketHandler {

    private static final String BASE_URL = "wss://fstream.binance.com/ws/";
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024; // 1MB

    private final Long botId;
    private final String apiKey;
    private final BinanceApiClient binanceApiClient;
    private final BinanceMessageHandler messageHandler;

    private String listenKey;
    private WebSocketSession webSocketSession;
    private StringBuilder messageBuffer = new StringBuilder();

    public void init() {
        this.listenKey = binanceApiClient.createListenKey(apiKey);
        connectWebSocket();
    }

    public void keepAliveListenKey() {
        if (this.listenKey == null) {
            throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BOT_WEBSOCKET_LISTEN_KEY_ERROR, "ListenKey가 없어 갱신할 수 없습니다.");
        }
        binanceApiClient.keepAliveListenKey(apiKey);
    }


    public void disconnect() {
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
            } catch (Exception e) {
                throw new ExchangeException(ExchangeType.BINANCE, ErrorCode.BOT_WEBSOCKET_DISCONNECTION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("봇 {} 웹소켓 연결 설정 완료", botId);
        this.webSocketSession = session;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String payload = textMessage.getPayload();

            if (messageBuffer.length() + payload.length() > MAX_MESSAGE_SIZE) {
                log.error("봇 {} 메시지 크기 초과. 현재 크기: {}, 수신 크기: {}",
                        botId, messageBuffer.length(), payload.length());
                messageBuffer.setLength(0);
                return;
            }

            messageBuffer.append(payload);

            if (textMessage.isLast()) {
                String completeMessage = messageBuffer.toString();
                messageBuffer.setLength(0);
                messageHandler.handleMessage(completeMessage, botId);
            }
        } else {
            log.info("봇 {} 텍스트가 아닌 메시지 수신: {}", botId, message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("봇 {} 웹소켓 전송 오류: {}", botId, exception.getMessage());
        reconnectWebSocket();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("봇 {} 웹소켓 연결 종료. 상태: {}", botId, closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    private void connectWebSocket() {
        String wsUrl = BASE_URL + this.listenKey;

        StandardWebSocketClient client = new StandardWebSocketClient();
        client.execute(this, new WebSocketHttpHeaders(), URI.create(wsUrl))
                .thenAccept(session -> {
                    this.webSocketSession = session;
                    log.info("봇 {} 웹소켓 연결 성공", botId);
                })
                .exceptionally(ex -> {
                    log.error("봇 {} 웹소켓 연결 실패: {}", botId, ex.getMessage());
                    reconnectWebSocket();
                    return null;
                });
    }

    private void reconnectWebSocket() {
        log.info("봇 {} 웹소켓 재연결 시도 중", botId);
        try {
            Thread.sleep(5000); // 5초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connectWebSocket();
    }

//    public void sendSubscriptionMessages() {
//        String[] subscriptions = {
//                this.listenKey + "@position",
//                this.listenKey + "@account",
//                this.listenKey + "@balance",
//        };
//
//        String subscriptionMessage = "{\"method\": \"REQUEST\", \"params\": [\"" +
//                String.join("\", \"", subscriptions) +
//                "\"], \"id\": 1}";
//
//        try {
//            webSocketSession.sendMessage(new TextMessage(subscriptionMessage));
//            log.info("봇 {} 구독 메시지 전송 완료: {}", botId, subscriptionMessage);
//        } catch (Exception e) {
//            log.error("봇 {} 구독 메시지 전송 실패: {}", botId, e.getMessage());
//        }
//    }
}