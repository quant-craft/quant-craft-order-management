package com.quant.craft.ordermanagement.domain.bot;

import com.quant.craft.ordermanagement.domain.ExchangeApiKey;
import com.quant.craft.ordermanagement.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_bots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TradingBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Boolean dryRun;
    private BigDecimal cash;
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private TradingBotStatus status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "exchange_api_key_id")
    private ExchangeApiKey exchangeApiKey;

    @JoinColumn(name = "strategy_id")
    private Long strategy;

    @Version
    private Long version;

    public void updateCash(BigDecimal amount) {
        if (this.cash == null) {
            this.cash = BigDecimal.ZERO;
        }
        this.cash = this.cash.add(amount);
    }

}