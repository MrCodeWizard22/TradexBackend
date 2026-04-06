package com.piyush.tradex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.piyush.tradex.dto.OrderRequestDTO;
import com.piyush.tradex.dto.OrderResponseDTO;
import com.piyush.tradex.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<OrderResponseDTO> buyStock(@Valid @RequestBody OrderRequestDTO request) {
        String email = getAuthenticatedEmail();
        OrderResponseDTO response = orderService.buyStock(email, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    public ResponseEntity<OrderResponseDTO> sellStock(@Valid @RequestBody OrderRequestDTO request) {
        String email = getAuthenticatedEmail();
        OrderResponseDTO response = orderService.sellStock(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory() {
        String email = getAuthenticatedEmail();
        List<OrderResponseDTO> response = orderService.getOrderHistory(email);
        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
