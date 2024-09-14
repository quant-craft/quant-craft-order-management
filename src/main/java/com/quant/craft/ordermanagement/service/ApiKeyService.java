package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeApiKey;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.domain.bot.TradingBot;
import com.quant.craft.ordermanagement.common.exception.ErrorCode;
import com.quant.craft.ordermanagement.common.exception.ExchangeException;
import com.quant.craft.ordermanagement.repository.TradingBotRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO
 * Event 받아서 refresh 할 수 있게끔 해야함.
 */
@Service
public class ApiKeyService {

    private final TradingBotRepository tradingBotRepository;
    private final Map<Long, ExchangeApiKey> apiKeyCache = new ConcurrentHashMap<>();

    public ApiKeyService(TradingBotRepository tradingBotRepository) {
        this.tradingBotRepository = tradingBotRepository;
        refreshCache();
    }

    public ExchangeApiKey getApiKey(Long botId) {
        return apiKeyCache.computeIfAbsent(botId, this::fetchApiKeyFromDatabase);
    }

    private ExchangeApiKey fetchApiKeyFromDatabase(Long botId) {
        TradingBot bot = tradingBotRepository.findById(botId)
                .orElseThrow(() -> new ExchangeException(
                        ExchangeType.UNKNOWN,
                        ErrorCode.TRADING_BOT_NOT_FOUND,
                        "TradingBotID: " + botId
                ));

        if (bot.getExchangeApiKey() == null) {
            throw new ExchangeException(
                    bot.getExchangeApiKey().getExchange(),
                    ErrorCode.API_KEY_NOT_FOUND,
                    "TradingBotID: " + botId
            );
        }

        return bot.getExchangeApiKey();
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshCache() {
        tradingBotRepository.findAll().forEach(bot -> {
            if (bot.getExchangeApiKey() != null) {
                apiKeyCache.put(bot.getId(), bot.getExchangeApiKey());
            }
        });
    }
}