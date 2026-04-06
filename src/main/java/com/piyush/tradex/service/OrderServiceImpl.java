package com.piyush.tradex.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piyush.tradex.dto.OrderRequestDTO;
import com.piyush.tradex.dto.OrderResponseDTO;
import com.piyush.tradex.enitity.Order;
import com.piyush.tradex.enitity.User;
import com.piyush.tradex.enitity.Wallet;
import com.piyush.tradex.enums.OrderStatus;
import com.piyush.tradex.enums.OrderType;
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

    @Override
    @Transactional
    public OrderResponseDTO buyStock(String email, OrderRequestDTO request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user: " + email);
        }

        double totalValue = request.getPrice() * request.getQuantity();

        if (wallet.getBalance() < totalValue) {
            throw new RuntimeException(
                "Insufficient balance. Required: ₹" + totalValue + ", Available: ₹" + wallet.getBalance()
            );
        }

        // Deduct from wallet
        wallet.setBalance(wallet.getBalance() - totalValue);
        walletRepository.save(wallet);

        // Store order
        Order order = new Order();
        order.setUser(user);
        order.setOrderType(OrderType.BUY);
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.EXECUTED);
        order.setCreatedAt(new Date());
        orderRepository.save(order);

        return new OrderResponseDTO(
            order.getOrderId(),
            user.getUserId(),
            OrderType.BUY,
            request.getPrice(),
            request.getQuantity(),
            totalValue,
            OrderStatus.EXECUTED,
            "BUY order placed successfully. ₹" + totalValue + " deducted.",
            wallet.getBalance()
        );
    }

    @Override
    @Transactional
    public OrderResponseDTO sellStock(String email, OrderRequestDTO request) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        double totalValue = request.getPrice() * request.getQuantity();

        // Store order (no wallet change for now)
        Order order = new Order();
        order.setUser(user);
        order.setOrderType(OrderType.SELL);
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.EXECUTED);
        order.setCreatedAt(new Date());
        orderRepository.save(order);

        return new OrderResponseDTO(
            order.getOrderId(),
            user.getUserId(),
            OrderType.SELL,
            request.getPrice(),
            request.getQuantity(),
            totalValue,
            OrderStatus.EXECUTED,
            "SELL order placed successfully.",
            null
        );
    }

    @Override
    public List<OrderResponseDTO> getOrderHistory(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
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
            order.getOrderType() + " order on " + order.getCreatedAt(),
            null
        )).collect(Collectors.toList());
    }
}
