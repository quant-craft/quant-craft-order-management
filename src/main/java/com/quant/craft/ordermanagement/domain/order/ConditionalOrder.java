package com.quant.craft.ordermanagement.domain.order;

import com.quant.craft.ordermanagement.domain.enums.ProcessingStatus;
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

// 이건 사실 나중에 어떻게 해야할 지 더 세부적으로 구현 필요함
@Entity
@Table(name = "conditional_orders", schema = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ConditionalOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_order_id")
    private Order parentOrder;

    @Enumerated(EnumType.STRING)
    private ConditionalOrderType type;

    private BigDecimal triggerPrice;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ConditionalOrder(ConditionalOrderType type, BigDecimal triggerPrice) {
        this.type = type;
        this.triggerPrice = triggerPrice;
        this.processingStatus = ProcessingStatus.PENDING;
    }

    void setParentOrder(Order parentOrder) {
        this.parentOrder = parentOrder;
    }

    public void updateProcessingStatus(ProcessingStatus newStatus) {
        this.processingStatus = newStatus;
    }
}
