package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.bot.TradingBot;
import com.quant.craft.ordermanagement.domain.bot.TradingBotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TradingBotRepository extends JpaRepository<TradingBot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TradingBot> findWithPessimisticLockById(Long id);

    @EntityGraph(attributePaths = "exchangeApiKey")
    List<TradingBot> findAllByStatus(TradingBotStatus status);
}
