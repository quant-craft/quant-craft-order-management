package com.quant.craft.ordermanagement.domain.position;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
import com.quant.craft.ordermanagement.domain.enums.PositionSide;
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
@Table(name = "positions", schema = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "position_id", unique = true, nullable = false)
    private String positionId;

    @Column(name = "trading_bot_id", nullable = false)
    private Long tradingBotId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", nullable = false)
    private ExchangeType exchange;

    @Column(name = "size", precision = 30, scale = 8, nullable = false)
    private BigDecimal size;

    @Column(name = "entry_price", precision = 30, scale = 8, nullable = false)
    private BigDecimal entryPrice;

    @Transient
    private BigDecimal currentPrice;

    @Transient
    private BigDecimal unrealizedPnl;

    @Column(name = "realized_pnl", precision = 30, scale = 8, nullable = false)
    private BigDecimal realizedPnl;

    @Transient
    private BigDecimal margin;

    @Column(name = "leverage", nullable = false)
    private int leverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PositionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_side", nullable = false)
    private PositionSide positionSide;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder
    public Position(String positionId, Long tradingBotId, String symbol, ExchangeType exchange,
                    BigDecimal size, BigDecimal entryPrice, int leverage, PositionStatus status, PositionSide positionSide) {
        this.positionId = positionId;
        this.tradingBotId = tradingBotId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.size = size;
        this.entryPrice = entryPrice;
        this.leverage = leverage;
        this.positionSide = positionSide;
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
        if (positionSide == PositionSide.SHORT) {
            this.unrealizedPnl = this.unrealizedPnl.negate();
        }
    }

    public BigDecimal closePosition(BigDecimal exitPrice) {
        this.currentPrice = exitPrice;
        calculateUnrealizedPnl();
        BigDecimal pnl = this.unrealizedPnl;
        this.realizedPnl = this.realizedPnl.add(pnl);
        this.unrealizedPnl = BigDecimal.ZERO;
        this.status = PositionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        return pnl;
    }

    public BigDecimal partialClose(BigDecimal closeSize, BigDecimal exitPrice) {
        if (closeSize.compareTo(this.size) > 0) {
            throw new IllegalArgumentException("Close size cannot be greater than position size");
        }

        BigDecimal closingRatio = closeSize.divide(this.size, 8, RoundingMode.HALF_UP);
        this.size = this.size.subtract(closeSize);

        return calculatePnl(exitPrice).multiply(closingRatio);
    }

    private BigDecimal calculatePnl(BigDecimal exitPrice) {
        BigDecimal priceDifference = exitPrice.subtract(entryPrice);
        BigDecimal pnl = priceDifference.multiply(size);
        return positionSide == PositionSide.SHORT ? pnl.negate() : pnl;
    }

    public void updatePosition(BigDecimal newSize, BigDecimal newEntryPrice) {
        this.size = newSize;
        this.entryPrice = newEntryPrice;
        updateCurrentPrice(newEntryPrice);
    }
}