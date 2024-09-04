package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.bot.TradingBot;
import com.quant.craft.ordermanagement.repository.TradingBotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TradingBotService {
    private final TradingBotRepository tradingBotRepository;

    @Transactional
    @Retryable(
            retryFor = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void updateCash(Long tradingBotId, BigDecimal amount) {
        try {
            TradingBot tradingBot = tradingBotRepository.findWithPessimisticLockById(tradingBotId)
                    .orElseThrow(() -> new IllegalArgumentException("Trading bot not found !! : " + tradingBotId));

            tradingBot.updateCash(amount);
        } catch (PessimisticLockingFailureException e) {
            // 로그 기록해야함.
            throw e;
        }
    }

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void updateCashWithOptimisticLock(Long tradingBotId, BigDecimal amount) {
        TradingBot tradingBot = tradingBotRepository.findById(tradingBotId)
                .orElseThrow(() -> new IllegalArgumentException("Trading bot not found!!! : " + tradingBotId));

        tradingBot.updateCash(amount);
    }
}