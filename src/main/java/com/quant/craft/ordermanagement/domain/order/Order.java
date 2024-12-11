package com.quant.craft.ordermanagement.domain.order;

import com.quant.craft.ordermanagement.domain.enums.PositionSide;
import com.quant.craft.ordermanagement.domain.enums.ProcessingStatus;
import com.quant.craft.ordermanagement.domain.enums.Side;
import com.quant.craft.ordermanagement.domain.exchange.ExchangeType;
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
@Table(name = "orders", schema = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "client_order_id", unique = true)
    private String clientOrderId;

    @Column(name = "trading_bot_id", nullable = false)
    private Long tradingBotId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", nullable = false)
    private ExchangeType exchange;

    @Column(name = "size", precision = 30, scale = 8, nullable = false)
    private BigDecimal size;

    @Column(name = "price", precision = 30, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(name = "leverage", nullable = false)
    private int leverage;

    // 실질적으로 고객에게 보여지는 주문상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // 주문 유형 Market LIMIT 등등
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;

    // BUY, SELL
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    // LONG, SHORT, BOTH - Default
    @Enumerated(EnumType.STRING)
    @Column(name = "position_side", nullable = false)
    private PositionSide positionSide;

    // OPEN , CLOSE
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private OrderAction action;

    // 진행상태를 위해 인위적으로 만든 Status
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @OneToMany(mappedBy = "parentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConditionalOrder> conditionalOrders = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder
    public Order(String orderId, String clientOrderId, Long tradingBotId, String symbol, ExchangeType exchange,
                 BigDecimal size, BigDecimal price, int leverage,
                 OrderType type, OrderStatus status, Side side, PositionSide positionSide, OrderAction action, ProcessingStatus processingStatus) {
        this.orderId = orderId;
        this.clientOrderId = clientOrderId;
        this.tradingBotId = tradingBotId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.size = size;
        this.price = price;
        this.leverage = leverage;
        this.type = type;
        this.side = side;
        this.positionSide = positionSide;
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

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }
}
