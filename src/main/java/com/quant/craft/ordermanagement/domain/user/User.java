package com.quant.craft.ordermanagement.domain.user;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeApiKey;
import com.quant.craft.ordermanagement.domain.bot.TradingBot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "user")
    private List<ExchangeApiKey> exchangeApiKeys;
    @OneToMany(mappedBy = "user")
    private List<TradingBot> tradingBots;
}