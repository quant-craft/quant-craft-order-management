package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.ExchangeType;
import com.quant.craft.ordermanagement.domain.Position;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position,Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Position p WHERE p.tradingBotId = :tradingBotId AND p.symbol = :symbol AND p.exchange =:exchange AND p.status = 'OPEN'")
    Optional<Position> findOpenPositionsByTradingBotIdAndSymbolWithLock(
            @Param("tradingBotId") Long tradingBotId,
            @Param("symbol") String symbol,
            @Param("exchange") ExchangeType exchange);
}
