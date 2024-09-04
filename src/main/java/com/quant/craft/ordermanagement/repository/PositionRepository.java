package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position,Long> {

    @Query("SELECT p FROM Position p WHERE p.tradingBotId = :tradingBotId AND p.symbol = :symbol AND p.status = 'OPEN'")
    List<Position> findOpenPositionsByBotIdAndSymbol(
            @Param("tradingBotId") Long tradingBotId,
            @Param("symbol") String symbol);
}
