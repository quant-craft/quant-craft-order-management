package com.quant.craft.ordermanagement.domain.bot;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeApiKey;
import com.quant.craft.ordermanagement.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_bots", schema = "trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradingBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dry_run", nullable = false)
    private Boolean dryRun;

    @Column(name = "cash", precision = 30, scale = 8, nullable = false)
    private BigDecimal cash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradingBotStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "exchange_api_key_id")
    private ExchangeApiKey exchangeApiKey;

    @Column(name = "strategy_id", nullable = false)
    private Long strategyId;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateCash(BigDecimal amount) {
        if (this.cash == null) {
            this.cash = BigDecimal.ZERO;
        }
        this.cash = this.cash.add(amount);
    }

}