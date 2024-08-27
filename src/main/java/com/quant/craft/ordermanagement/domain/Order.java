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
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(unique = true, nullable = false)
    private String orderId;

    private Long botId;
    private Long tradingBotId;
    private String symbol;
    private String exchange;
    private BigDecimal size;
    private BigDecimal limit;
    private BigDecimal stop;
    private BigDecimal sl;
    private BigDecimal tp;
    private BigDecimal leverage;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    private OrderPosition position;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Order(String orderId, Long botId, Long tradingBotId, String symbol, String exchange,
                 BigDecimal size, BigDecimal limit, BigDecimal stop, BigDecimal sl, BigDecimal tp, BigDecimal leverage,
                 OrderType type, OrderPosition position, OrderStatus status) {
        this.orderId = orderId;
        this.botId = botId;
        this.tradingBotId = tradingBotId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.size = size;
        this.limit = limit;
        this.stop = stop;
        this.sl = sl;
        this.tp = tp;
        this.leverage = leverage;
        this.type = type;
        this.position = position;
        setStatus(status);
    }

    private void setStatus(OrderStatus status) {
        if (status == null || !status.isActive()) {
            throw new IllegalArgumentException("Initial order status must be OPEN or PARTIALLY_FILLED");
        }
        this.status = status;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null || !newStatus.isActive()) {
            throw new IllegalArgumentException("Updated order status must be OPEN or PARTIALLY_FILLED");
        }
        this.status = newStatus;
    }

}
