package com.quant.craft.ordermanagement.controller;

import com.quant.craft.ordermanagement.domain.Order;
import com.quant.craft.ordermanagement.domain.Position;
import com.quant.craft.ordermanagement.domain.ProcessingStatus;
import com.quant.craft.ordermanagement.domain.Trade;
import com.quant.craft.ordermanagement.service.OrderService;
import com.quant.craft.ordermanagement.service.PositionService;
import com.quant.craft.ordermanagement.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TradingController {
    private final PositionService positionService;
    private final OrderService orderService;
    private final TradeService tradeService;

    @GetMapping("/trading")
    public String getPositions(Model model) {
        List<Position> positions = positionService.getAllPositions();
        List<Order> openOrders = orderService.getAllOrdersByProcessingStatus(ProcessingStatus.PENDING);
        List<Order> orderHistory = orderService.getAllOrdersByProcessingStatus(ProcessingStatus.COMPLETED);
        List<Trade> tradeHistory = tradeService.getAllTrades();

        for (Position position : positions) {
            position.updateCurrentPrice(new BigDecimal("0.18"));
        }


        model.addAttribute("positions", positions);
        model.addAttribute("openOrders", openOrders);
        model.addAttribute("orderHistory", orderHistory);
        model.addAttribute("tradeHistory", tradeHistory);
        return "trading";
    }
}
