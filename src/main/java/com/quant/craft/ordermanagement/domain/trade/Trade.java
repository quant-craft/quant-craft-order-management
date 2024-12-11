package com.quant.craft.ordermanagement.domain.trade;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.domain.enums.Side;
import com.quant.craft.ordermanagement.domain.order.OrderAction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades", schema = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "trade_id", unique = true, nullable = false)
    private String tradeId;

    @Column(name = "trading_bot_id", nullable = false)
    private Long tradingBotId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", nullable = false)
    private ExchangeType exchange;

    @Column(name = "executed_size", precision = 30, scale = 8, nullable = false)
    private BigDecimal executedSize;

    @Column(name = "executed_price", precision = 30, scale = 8, nullable = false)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private OrderAction action;

    @CreatedDate
    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Builder
    public Trade(String tradeId, Long tradingBotId, String orderId, String symbol, ExchangeType exchange,
                 BigDecimal executedSize, BigDecimal executedPrice,
                 Side side, OrderAction action) {
        this.tradeId = tradeId;
        this.tradingBotId = tradingBotId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.executedSize = executedSize;
        this.executedPrice = executedPrice;
        this.side = side;
        this.action = action;
    }
}