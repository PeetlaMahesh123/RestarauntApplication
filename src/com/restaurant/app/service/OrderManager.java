package com.restaurant.app.service;

import com.restaurant.app.model.Order;
import com.restaurant.app.model.OrderStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class OrderManager {
    private final Deque<Order> orderHistory = new ArrayDeque<>();
    private final int historyLimit;

    public OrderManager() {
        this(12);
    }

    public OrderManager(int historyLimit) {
        this.historyLimit = Math.max(5, historyLimit);
    }

    public void addOrder(Order order) {
        order.setStatus(OrderStatus.SERVED);
        orderHistory.addFirst(order);
        while (orderHistory.size() > historyLimit) {
            orderHistory.removeLast();
        }
    }

    public List<Order> getHistory() {
        return new ArrayList<>(orderHistory);
    }

    public void clearHistory() {
        orderHistory.clear();
    }
}


