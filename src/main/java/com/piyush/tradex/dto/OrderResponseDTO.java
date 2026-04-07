package com.piyush.tradex.dto;

import com.piyush.tradex.enums.OrderStatus;
import com.piyush.tradex.enums.OrderType;

public class OrderResponseDTO {

    private long orderId;
    private long userId;
    private OrderType orderType;
    private double price;
    private int quantity;
    private double totalValue;
    private OrderStatus status;
    private String message;
    private Double newWalletBalance;

    public OrderResponseDTO(long orderId, long userId, OrderType orderType,
            double price, int quantity, double totalValue,
            OrderStatus status, String message, Double newWalletBalance) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.status = status;
        this.message = message;
        this.newWalletBalance = newWalletBalance;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getUserId() {
        return userId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Double getNewWalletBalance() {
        return newWalletBalance;
    }
}
