package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.tradingBotId = :tradingBotId AND o.symbol = :symbol AND o.status IN (com.quant.craft.ordermanagement.domain.OrderStatus.OPEN, com.quant.craft.ordermanagement.domain.OrderStatus.PARTIALLY_FILLED)")
    List<Order> findOpenOrdersByTradingBotIdAndSymbol(
            @Param("tradingBotId") Long tradingBotId,
            @Param("symbol") String symbol);

}
