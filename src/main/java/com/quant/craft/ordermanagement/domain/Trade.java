package com.quant.craft.ordermanagement.domain;

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
@Table(name = "trades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tradeId;
    private Long tradingBotId;
    private String orderId;

    private String symbol;
    @Enumerated(EnumType.STRING)
    private ExchangeType exchange;
    @Column(precision = 30, scale = 8)
    private BigDecimal executedSize;
    @Column(precision = 30, scale = 8)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    private Side side;

    @Enumerated(EnumType.STRING)
    private OrderAction action;

    @CreatedDate
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