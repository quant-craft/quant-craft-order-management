package com.quant.craft.ordermanagement.service;

import com.quant.craft.ordermanagement.domain.*;
import com.quant.craft.ordermanagement.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionService {
    private final PositionRepository positionRepository;
    private final TradingBotService tradingBotService;

    @Transactional(readOnly = true)
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Optional<Position> findExistingPosition(long tradingBotId, String symbol, ExchangeType exchangeType) {
        return positionRepository.findOpenPositionsByTradingBotIdAndSymbolWithLock(tradingBotId, symbol, exchangeType);
    }

    @Transactional
    public void updatePosition(long tradingBotId, String symbol, ExchangeType exchangeType,
                               TradeDirection direction, BigDecimal size, BigDecimal price,
                               OrderAction action, int leverage) {
        Optional<Position> positionOptional = findExistingPosition(tradingBotId, symbol, exchangeType);

        if (positionOptional.isPresent()) {
            Position position = positionOptional.get();
            if (position.getDirection() == direction) {
                updateExistingPosition(position, size, price);
            } else {
                BigDecimal remainingSize = closeOrReducePosition(position, size, price);
                if (remainingSize.compareTo(BigDecimal.ZERO) > 0) {
                    createNewPosition(tradingBotId, symbol, exchangeType, direction, remainingSize, price, leverage);
                }
            }
        } else if (action == OrderAction.OPEN) {
            createNewPosition(tradingBotId, symbol, exchangeType, direction, size, price, leverage);
        }
    }

    private void updateExistingPosition(Position position, BigDecimal additionalSize, BigDecimal newPrice) {
        BigDecimal newSize = position.getSize().add(additionalSize);
        BigDecimal newEntryPrice = calculateNewEntryPrice(position, additionalSize, newPrice);
        position.updatePosition(newSize, newEntryPrice);
    }

    private BigDecimal closeOrReducePosition(Position position, BigDecimal closeSize, BigDecimal exitPrice) {
        BigDecimal remainingSize = closeSize.subtract(position.getSize());
        if (remainingSize.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal pnl = position.partialClose(closeSize, exitPrice);
            tradingBotService.updateCashWithOptimisticLock(position.getTradingBotId(), pnl);
            positionRepository.save(position);
            return BigDecimal.ZERO;
        } else {
            BigDecimal pnl = position.closePosition(exitPrice);
            tradingBotService.updateCashWithOptimisticLock(position.getTradingBotId(), pnl);
            positionRepository.save(position);
            return remainingSize;
        }
    }

    private void createNewPosition(long tradingBotId, String symbol, ExchangeType exchangeType,
                                   TradeDirection direction, BigDecimal size, BigDecimal price, int leverage) {
        Position newPosition = Position.builder()
                .positionId(generatePositionId())
                .tradingBotId(tradingBotId)
                .symbol(symbol)
                .exchange(exchangeType)
                .size(size)
                .entryPrice(price)
                .leverage(leverage)
                .direction(direction)
                .status(PositionStatus.OPEN)
                .build();
        positionRepository.save(newPosition);
    }

    private BigDecimal calculateNewEntryPrice(Position position, BigDecimal additionalSize, BigDecimal newPrice) {
        BigDecimal totalValue = position.getSize().multiply(position.getEntryPrice())
                .add(additionalSize.multiply(newPrice));
        BigDecimal totalSize = position.getSize().add(additionalSize);
        return totalValue.divide(totalSize, 8, RoundingMode.HALF_UP);
    }

    private String generatePositionId() {
        return UUID.randomUUID().toString();
    }
}