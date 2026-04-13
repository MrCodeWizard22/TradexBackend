package com.piyush.tradex.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piyush.tradex.dto.OrderRequestDTO;
import com.piyush.tradex.dto.OrderResponseDTO;
import com.piyush.tradex.enitity.Holding;
import com.piyush.tradex.enitity.Order;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enitity.Wallet;
import com.piyush.tradex.enums.OrderStatus;
import com.piyush.tradex.enums.OrderType;
import com.piyush.tradex.repository.HoldingRepository;
import com.piyush.tradex.repository.OrderRepository;
import com.piyush.tradex.repository.UserRepository;
import com.piyush.tradex.repository.WalletRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    // BUY

    @Override
    @Transactional
    public OrderResponseDTO buyStock(String email, OrderRequestDTO request) {

        // 1. Resolve user
        User buyer = userRepository.findByEmail(email);
        if (buyer == null) {
            throw new RuntimeException("User not found: " + email);
        }

        // 2. Wallet check and deduction
        Wallet buyerWallet = buyer.getWallet();
        if (buyerWallet == null) {
            throw new RuntimeException("Wallet not found for user: " + email);
        }

        double totalValue = request.getPrice() * request.getQuantity();

        if (buyerWallet.getBalance() < totalValue) {
            throw new RuntimeException(
                    "Insufficient balance. Required: ₹" + totalValue +
                            ", Available: ₹" + buyerWallet.getBalance());
        }

        // Deduct money upfront (held against the BUY order)
        buyerWallet.setBalance(buyerWallet.getBalance() - totalValue);
        walletRepository.save(buyerWallet);

        // 3. Save BUY order as PENDING
        Order buyOrder = new Order();
        buyOrder.setUser(buyer);
        buyOrder.setOrderType(OrderType.BUY);
        buyOrder.setPrice(request.getPrice());
        buyOrder.setQuantity(request.getQuantity());
        buyOrder.setStatus(OrderStatus.PENDING);
        buyOrder.setCreatedAt(new Date());
        orderRepository.save(buyOrder);

        // 4. Try to match with a PENDING SELL order
        boolean matched = matchOrder(buyOrder);

        // Re-read wallet balance after potential match
        double currentBalance = walletRepository.findById(buyerWallet.getWalletId())
                .map(w -> w.getBalance())
                .orElse(buyerWallet.getBalance());

        String message = matched
                ? "BUY order EXECUTED! Matched with a SELL order. ₹" + totalValue + " deducted."
                : "BUY order placed and is PENDING. ₹" + totalValue + " reserved. Waiting for a matching SELL.";

        return new OrderResponseDTO(
                buyOrder.getOrderId(),
                buyer.getUserId(),
                OrderType.BUY,
                request.getPrice(),
                request.getQuantity(),
                totalValue,
                buyOrder.getStatus(),
                message,
                currentBalance);
    }

    // SELL

    @Override
    @Transactional
    public OrderResponseDTO sellStock(String email, OrderRequestDTO request) {

        // 1. Resolve user
        User seller = userRepository.findByEmail(email);
        if (seller == null) {
            throw new RuntimeException("User not found: " + email);
        }

        // 2. Validate holding BEFORE creating the order
        Optional<Holding> sellerHoldingOpt = holdingRepository.findByUser(seller);
        if (sellerHoldingOpt.isEmpty()) {
            throw new RuntimeException("Cannot sell: you have no holdings.");
        }
        Holding sellerHolding = sellerHoldingOpt.get();
        if (sellerHolding.getQuantity() < request.getQuantity()) {
            throw new RuntimeException(
                    "Cannot sell: insufficient holding. You have " + sellerHolding.getQuantity() +
                            " unit(s) but tried to sell " + request.getQuantity() + ".");
        }

        double totalValue = request.getPrice() * request.getQuantity();

        // 3. Save SELL order as PENDING
        Order sellOrder = new Order();
        sellOrder.setUser(seller);
        sellOrder.setOrderType(OrderType.SELL);
        sellOrder.setPrice(request.getPrice());
        sellOrder.setQuantity(request.getQuantity());
        sellOrder.setStatus(OrderStatus.PENDING);
        sellOrder.setCreatedAt(new Date());
        orderRepository.save(sellOrder);

        // Try to match with a PENDING BUY order
        boolean matched = matchOrder(sellOrder);

        // Re-read seller wallet balance
        Double newBalance = null;
        if (matched && seller.getWallet() != null) {
            newBalance = walletRepository.findById(seller.getWallet().getWalletId())
                    .map(w -> w.getBalance())
                    .orElse(null);
        }

        String message = matched
                ? "SELL order EXECUTED! Matched with a BUY order. ₹" + totalValue + " credited to your wallet."
                : "SELL order placed and is PENDING. Waiting for a matching BUY order.";

        return new OrderResponseDTO(
                sellOrder.getOrderId(),
                seller.getUserId(),
                OrderType.SELL,
                request.getPrice(),
                request.getQuantity(),
                totalValue,
                sellOrder.getStatus(),
                message,
                newBalance);
    }

    // MATCHING ENGINE (core logic)

    boolean matchOrder(Order incomingOrder) {

        // Determine what type of opposite order we need
        OrderType oppositeType = (incomingOrder.getOrderType() == OrderType.BUY)
                ? OrderType.SELL
                : OrderType.BUY;

        // Search for the earliest matching opposite PENDING order
        List<Order> matchOpt = orderRepository.findFirstMatchingOrders(
                oppositeType,
                incomingOrder.getPrice(),
                incomingOrder.getQuantity(),
                OrderStatus.PENDING);
        if (matchOpt == null || matchOpt.isEmpty()) {
            return false;
        }
        Optional<Order> optionalMatch = Optional.empty();

        for (Order o : matchOpt) {

            if (Long.compare(o.getUser().getUserId(), incomingOrder.getUser().getUserId()) == 0
                    || Long.compare(o.getOrderId(), incomingOrder.getOrderId()) == 0) {
                continue;
            }

            Order sellCandidate = (o.getOrderType() == OrderType.SELL) ? o : incomingOrder;

            User seller = sellCandidate.getUser();
            Optional<Holding> holdingOpt = holdingRepository.findByUser(seller);

            if (holdingOpt.isEmpty() || holdingOpt.get().getQuantity() < sellCandidate.getQuantity()) {
                continue;
            }

            optionalMatch = Optional.of(o);
            break;
        }
        if (optionalMatch.isEmpty()) {
            return false;
        }
        Order matchedOrder = optionalMatch.get();

        // Mark both orders as EXECUTED
        incomingOrder.setStatus(OrderStatus.EXECUTED);
        matchedOrder.setStatus(OrderStatus.EXECUTED);
        orderRepository.save(incomingOrder);
        orderRepository.save(matchedOrder);

        // Identify buyer and seller
        Order sellOrder = (incomingOrder.getOrderType() == OrderType.SELL)
                ? incomingOrder
                : matchedOrder;
        Order buyOrder = (incomingOrder.getOrderType() == OrderType.BUY)
                ? incomingOrder
                : matchedOrder;

        User seller = sellOrder.getUser();
        User buyer = buyOrder.getUser();
        double tradeValue = sellOrder.getPrice() * sellOrder.getQuantity();
        int tradeQty = sellOrder.getQuantity();
        double tradePrice = sellOrder.getPrice();

        // ── Credit the seller's wallet ──────────────────────────────────────
        Wallet sellerWallet = seller.getWallet();
        if (sellerWallet != null) {
            sellerWallet.setBalance(sellerWallet.getBalance() + tradeValue);
            walletRepository.save(sellerWallet);
        }

        // ── Update BUYER holding ────────────────────────────────────────────
        Optional<Holding> buyerHoldingOpt = holdingRepository.findByUser(buyer);
        if (buyerHoldingOpt.isEmpty()) {
            // Case 1: no holding yet — create it
            Holding newHolding = new Holding();
            newHolding.setUser(buyer);
            newHolding.setQuantity(tradeQty);
            newHolding.setAveragePrice(tradePrice);
            holdingRepository.save(newHolding);
        } else {
            // Case 2: holding exists — recalculate average price
            Holding buyerHolding = buyerHoldingOpt.get();
            int oldQty = buyerHolding.getQuantity();
            double oldAvgPrice = buyerHolding.getAveragePrice();
            int newQty = oldQty + tradeQty;
            double newAvgPrice = (oldQty * oldAvgPrice + tradeQty * tradePrice) / newQty;
            buyerHolding.setQuantity(newQty);
            buyerHolding.setAveragePrice(newAvgPrice);
            holdingRepository.save(buyerHolding);
        }

        // ── Update SELLER holding ───────────────────────────────────────────
        Optional<Holding> sellerHoldingOpt = holdingRepository.findByUser(seller);
        if (sellerHoldingOpt.isPresent()) {
            Holding sellerHolding = sellerHoldingOpt.get();
            int newQty = sellerHolding.getQuantity() - tradeQty;
            if (newQty == 0) {
                holdingRepository.delete(sellerHolding);
            } else {
                sellerHolding.setQuantity(newQty);
                holdingRepository.save(sellerHolding);
            }
        }

        return true;
    }

    // ORDER HISTORY

    @Override
    public List<OrderResponseDTO> getOrderHistory(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found: " + email);
        }

        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream().map(order -> new OrderResponseDTO(
                order.getOrderId(),
                user.getUserId(),
                order.getOrderType(),
                order.getPrice(),
                order.getQuantity(),
                order.getPrice() * order.getQuantity(),
                order.getStatus(),
                order.getOrderType() + " order — " + order.getStatus() + " | " + order.getCreatedAt(),
                null)).collect(Collectors.toList());
    }
}
