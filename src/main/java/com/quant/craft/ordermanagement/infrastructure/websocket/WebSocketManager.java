package com.quant.craft.ordermanagement.infrastructure.websocket;

import com.quant.craft.ordermanagement.domain.bot.TradingBot;
import com.quant.craft.ordermanagement.domain.bot.TradingBotStatus;
import com.quant.craft.ordermanagement.repository.TradingBotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketManager {
    private final Map<Long, BotWebSocketConnection> botConnections = new ConcurrentHashMap<>();
    private final BinanceApiClient binanceApiClient;
    private final TradingBotRepository tradingBotRepository;
    private final BinanceMessageHandler binanceMessageHandler;

    @Scheduled(fixedRate = 60000)
    public void pollAndManageBots() {
        List<TradingBot> runningBots = tradingBotRepository.findAllByStatus(TradingBotStatus.RUNNING);
        Set<Long> runningBotIds = runningBots.stream()
                .map(TradingBot::getId)
                .collect(Collectors.toSet());

        addNewRunningBots(runningBots);
        removeStoppedBots(runningBotIds);
    }

    private void addNewRunningBots(List<TradingBot> runningBots) {
        for (TradingBot bot : runningBots) {
            if (!botConnections.containsKey(bot.getId())) {
                addBot(bot.getId(), bot.getExchangeApiKey().getApiKey());
            }
        }
    }

    private void removeStoppedBots(Set<Long> runningBotIds) {
        botConnections.keySet().removeIf(botId -> {
            if (!runningBotIds.contains(botId)) {
                removeBot(botId);
                return true;
            }
            return false;
        });
    }

    private void addBot(Long botId, String apiKey) {
        BotWebSocketConnection connection = createWebSocketConnection(botId, apiKey);
        botConnections.put(botId, connection);
        connection.init();
    }

    private BotWebSocketConnection createWebSocketConnection(Long botId, String apiKey) {
        return new BotWebSocketConnection(botId, apiKey, binanceApiClient, binanceMessageHandler);
    }

    private void removeBot(Long botId) {
        BotWebSocketConnection connection = botConnections.remove(botId);
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void keepAliveListenKeys() {
        for (BotWebSocketConnection connection : botConnections.values()) {
            connection.keepAliveListenKey();
        }
    }
}