package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.Order;
import com.quant.craft.ordermanagement.domain.ProcessingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.tradingBotId = :tradingBotId AND o.symbol = :symbol AND o.status IN (com.quant.craft.ordermanagement.domain.OrderStatus.OPEN, com.quant.craft.ordermanagement.domain.OrderStatus.PARTIALLY_FILLED)")
    List<Order> findOpenOrdersByTradingBotIdAndSymbol(
            @Param("tradingBotId") Long tradingBotId,
            @Param("symbol") String symbol);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT o FROM Order o WHERE o.clientOrderId = :clientOrderId")
    Optional<Order> findByClientOrderIdWithLock(@Param("clientOrderId") String clientOrderId);

    List<Order> findAllByProcessingStatus(ProcessingStatus processingStatus);
}
