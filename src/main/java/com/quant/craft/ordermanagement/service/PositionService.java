package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.Trade;
import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final PositionRepository positionRepository;

    public List<Position> findExistingPositions(long botId, String symbol) {
        List<Position> existingPositions = positionRepository.findOpenPositionsByBotIdAndSymbol(
                botId, symbol);
        return existingPositions;
    }


    @Transactional
    public void updatePosition(Trade trade, int leverage) {
        List<Position> existingPositions = positionRepository.findOpenPositionsByBotIdAndSymbol(
                trade.getBotId(), trade.getSymbol());

        boolean positionUpdated = false;

        for (Position position : existingPositions) {
            if (position.getDirection() == trade.getDirection()) {
                updateExistingPosition(position, trade);
                positionUpdated = true;
                break;
            } else {
                BigDecimal remainingSize = closeOrReducePosition(position, trade);
                if (remainingSize.compareTo(BigDecimal.ZERO) > 0) {
                    createNewPosition(trade, leverage, remainingSize);
                }
                positionUpdated = true;
            }
        }

        if (!positionUpdated && trade.getAction() == OrderAction.OPEN) {
            createNewPosition(trade, leverage, trade.getExecutedSize());
        }
    }


    private void updateExistingPosition(Position position, Trade trade) {
        BigDecimal newSize = position.getSize().add(trade.getExecutedSize());
        BigDecimal newEntryPrice = calculateNewEntryPrice(position, trade);
        position.updatePosition(newSize, newEntryPrice);
    }


    private BigDecimal closeOrReducePosition(Position position, Trade trade) {
        BigDecimal remainingSize = trade.getExecutedSize().subtract(position.getSize());
        if (remainingSize.compareTo(BigDecimal.ZERO) <= 0) {
            position.partialClose(trade.getExecutedSize(), trade.getExecutedPrice());
            return BigDecimal.ZERO;
        } else {
            position.closePosition(trade.getExecutedPrice());
            return remainingSize;
        }
    }

    private void createNewPosition(Trade trade, int leverage, BigDecimal size) {
        Position newPosition = Position.builder()
                .positionId(generatePositionId())
                .botId(trade.getBotId())
                .tradingBotId(trade.getTradingBotId())
                .symbol(trade.getSymbol())
                .exchange(trade.getExchange())
                .size(size)
                .entryPrice(trade.getExecutedPrice())
                .leverage(leverage)
                .direction(trade.getDirection())
                .status(PositionStatus.OPEN)
                .build();
        positionRepository.save(newPosition);
    }

    private BigDecimal calculateNewEntryPrice(Position position, Trade trade) {
        BigDecimal totalValue = position.getSize().multiply(position.getEntryPrice())
                .add(trade.getExecutedSize().multiply(trade.getExecutedPrice()));
        BigDecimal totalSize = position.getSize().add(trade.getExecutedSize());
        return totalValue.divide(totalSize, 8, RoundingMode.HALF_UP);
    }

    private String generatePositionId() {
        return UUID.randomUUID().toString();
    }
}
