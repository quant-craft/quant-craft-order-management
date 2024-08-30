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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;

    private Long botId;
    private Long tradingBotId;
    private String symbol;
    private String exchange;
    private BigDecimal size;
    private BigDecimal price;
    private int leverage;

    // 실질적으로 고객에게 보여지는 주문상태
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // 주문 유형 Market LIMIT 등등
    @Enumerated(EnumType.STRING)
    private OrderType type;

    // LONG , SHORT
    @Enumerated(EnumType.STRING)
    private TradeDirection direction;

    // OPEN , CLOSE
    @Enumerated(EnumType.STRING)
    private OrderAction action;

    // 진행상태를 위해 인위적으로 만든 Status
    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    @OneToMany(mappedBy = "parentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConditionalOrder> conditionalOrders = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Order(String orderId, Long botId, Long tradingBotId, String symbol, String exchange,
                 BigDecimal size, BigDecimal price, int leverage,
                 OrderType type, OrderStatus status, TradeDirection direction, OrderAction action, ProcessingStatus processingStatus) {
        this.orderId = orderId;
        this.botId = botId;
        this.tradingBotId = tradingBotId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.size = size;
        this.price = price;
        this.leverage = leverage;
        this.type = type;
        this.direction = direction;
        this.status = status;
        this.action = action;
        this.processingStatus = processingStatus;
    }

    public void addConditionalOrder(ConditionalOrder conditionalOrder) {
        conditionalOrders.add(conditionalOrder);
        conditionalOrder.setParentOrder(this);
    }

    public void removeConditionalOrder(ConditionalOrder conditionalOrder) {
        conditionalOrders.remove(conditionalOrder);
        conditionalOrder.setParentOrder(null);
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public void setStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }
}
