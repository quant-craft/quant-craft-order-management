package com.quant.craft.ordermanagement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "positions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String positionId;

    private Long botId;
    private Long tradingBotId;
    private String symbol;
    private String exchange;
    private BigDecimal size;
    private BigDecimal entryPrice;

    @Transient
    private BigDecimal currentPrice;
    @Transient
    private BigDecimal unrealizedPnl;
    @Transient
    private BigDecimal realizedPnl;
    @Transient
    private BigDecimal margin;
    private int leverage;

    @Enumerated(EnumType.STRING)
    private PositionStatus status;

    @Enumerated(EnumType.STRING)
    private TradeDirection direction;

    @CreatedDate
    private LocalDateTime openedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    @Builder
    public Position(String positionId, Long botId, Long tradingBotId, String symbol, String exchange,
                    BigDecimal size, BigDecimal entryPrice, int leverage, PositionStatus status, TradeDirection direction) {
        this.positionId = positionId;
        this.botId = botId;
        this.tradingBotId = tradingBotId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.size = size;
        this.entryPrice = entryPrice;
        this.leverage = leverage;
        this.direction = direction;
        this.status = PositionStatus.OPEN;
        this.currentPrice = entryPrice;
        this.unrealizedPnl = BigDecimal.ZERO;
        this.realizedPnl = BigDecimal.ZERO;
    }

    public void updateCurrentPrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
        calculateUnrealizedPnl();
        calculateMargin();
    }

    private void calculateMargin() {
        BigDecimal notionalValue = this.currentPrice.multiply(this.size);
        this.margin = notionalValue.divide(BigDecimal.valueOf(leverage), 8, RoundingMode.HALF_UP);
    }

    private void calculateUnrealizedPnl() {
        BigDecimal priceDifference = currentPrice.subtract(entryPrice);
        this.unrealizedPnl = priceDifference.multiply(size);
        if (direction == TradeDirection.SHORT) {
            this.unrealizedPnl = this.unrealizedPnl.negate();
        }
    }

    public void closePosition(BigDecimal exitPrice) {
        this.currentPrice = exitPrice;
        calculateUnrealizedPnl();
        this.realizedPnl = this.unrealizedPnl;
        this.unrealizedPnl = BigDecimal.ZERO;
        this.status = PositionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void partialClose(BigDecimal closeSize, BigDecimal exitPrice) {
        if (closeSize.compareTo(this.size) > 0) {
            throw new IllegalArgumentException("Close size cannot be greater than position size");
        }

        BigDecimal closingRatio = closeSize.divide(this.size, 8, RoundingMode.HALF_UP);
        BigDecimal pnl = calculatePnl(exitPrice).multiply(closingRatio);

        this.realizedPnl = this.realizedPnl.add(pnl);
        this.size = this.size.subtract(closeSize);
    }

    private BigDecimal calculatePnl(BigDecimal exitPrice) {
        BigDecimal priceDifference = exitPrice.subtract(entryPrice);
        BigDecimal pnl = priceDifference.multiply(size);
        return direction == TradeDirection.SHORT ? pnl.negate() : pnl;
    }

    public void updatePosition(BigDecimal newSize, BigDecimal newEntryPrice) {
        this.size = newSize;
        this.entryPrice = newEntryPrice;
        updateCurrentPrice(newEntryPrice);
    }
}