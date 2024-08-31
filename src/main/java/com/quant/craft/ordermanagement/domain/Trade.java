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

    private Long botId;
    private Long tradingBotId;
    private String orderId;

    private String symbol;
    private String exchange;
    private BigDecimal executedSize;
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    private TradeDirection direction;

    @Enumerated(EnumType.STRING)
    private OrderAction action;

    @CreatedDate
    private LocalDateTime executedAt;

    @Builder
    public Trade(String tradeId, Long botId, Long tradingBotId, String orderId, String symbol, String exchange,
                 BigDecimal executedSize, BigDecimal executedPrice,
                 TradeDirection direction, OrderAction action) {
        this.tradeId = tradeId;
        this.botId = botId;
        this.tradingBotId = tradingBotId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.executedSize = executedSize;
        this.executedPrice = executedPrice;
        this.direction = direction;
        this.action = action;
    }
}