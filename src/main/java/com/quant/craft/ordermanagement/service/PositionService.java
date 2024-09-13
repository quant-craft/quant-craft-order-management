package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.exception.ErrorCode;
import com.quant.craft.ordermanagement.exception.PositionNotFoundException;
import com.quant.craft.ordermanagement.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final PositionRepository positionRepository;
    private final TradingBotService tradingBotService;

    public Position findExistingPositions(long tradingBotId, String symbol) {
        return positionRepository.findOpenPositionsByTradingBotIdAndSymbolWithLock(
                        tradingBotId, symbol, ExchangeType.SIMULATED)
                .orElseThrow(() -> new PositionNotFoundException(ErrorCode.POSITION_NOT_FOUND,
                        "TradingBotId: " + tradingBotId + ", Symbol: " + symbol));
    }


    @Transactional
    public void updatePosition(Trade trade, int leverage) {
        Position position = findExistingPositions(trade.getTradingBotId(), trade.getSymbol());

        if (position.getDirection() == trade.getDirection()) {
            updateExistingPosition(position, trade);
        } else {
            BigDecimal remainingSize = closeOrReducePosition(position, trade);
            if (remainingSize.compareTo(BigDecimal.ZERO) > 0) {
                createNewPosition(trade, leverage, remainingSize);
            }
        }

        if (trade.getAction() == OrderAction.OPEN) {
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
            BigDecimal pnl = position.partialClose(trade.getExecutedSize(), trade.getExecutedPrice());

            tradingBotService.updateCashWithOptimisticLock(trade.getTradingBotId(), pnl);
            return BigDecimal.ZERO;
        } else {
            BigDecimal pnl = position.closePosition(trade.getExecutedPrice());

            tradingBotService.updateCashWithOptimisticLock(trade.getTradingBotId(), pnl);
            return remainingSize;
        }
    }

    private void createNewPosition(Trade trade, int leverage, BigDecimal size) {
        Position newPosition = Position.builder()
                .positionId(generatePositionId())
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
