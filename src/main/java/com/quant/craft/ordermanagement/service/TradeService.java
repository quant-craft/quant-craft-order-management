package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.trade.Trade;
import com.quant.craft.ordermanagement.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;

    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}
