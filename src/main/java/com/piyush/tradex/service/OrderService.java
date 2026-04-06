package com.piyush.tradex.service;

import java.util.List;

import com.piyush.tradex.dto.OrderRequestDTO;
import com.piyush.tradex.dto.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO buyStock(String email, OrderRequestDTO request);
    OrderResponseDTO sellStock(String email, OrderRequestDTO request);
    List<OrderResponseDTO> getOrderHistory(String email);
}
