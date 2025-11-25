package com.restaurant.app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Order {
    private static final double TAX_RATE = 0.08;

    private final String id;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items;
    private OrderStatus status;
    private String tableNumber;
    private String notes;

    public Order() {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.status = OrderStatus.NEW;
        this.tableNumber = "TBD";
        this.notes = "";
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addOrIncrement(MenuItem menuItem) {
        Optional<OrderItem> existing = items.stream()
            .filter(item -> item.getMenuItem().getCode().equals(menuItem.getCode()))
            .findFirst();

        if (existing.isPresent()) {
            existing.get().increaseQuantity(1);
        } else {
            items.add(new OrderItem(menuItem, 1));
        }
    }

    public void addItem(MenuItem menuItem, int quantity) {
        Optional<OrderItem> existing = items.stream()
            .filter(item -> item.getMenuItem().getCode().equals(menuItem.getCode()))
            .findFirst();

        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
        } else {
            items.add(new OrderItem(menuItem, Math.max(1, quantity)));
        }
    }

    public void remove(MenuItem menuItem) {
        items.removeIf(item -> item.getMenuItem().getCode().equals(menuItem.getCode()));
    }

    public void updateQuantity(MenuItem menuItem, int quantity) {
        items.stream()
            .filter(item -> item.getMenuItem().getCode().equals(menuItem.getCode()))
            .findFirst()
            .ifPresent(item -> item.setQuantity(quantity));
    }

    public double getSubtotal() {
        return items.stream()
            .mapToDouble(OrderItem::getLineTotal)
            .sum();
    }

    public double getTax() {
        return getSubtotal() * TAX_RATE;
    }

    public double getTotal() {
        return getSubtotal() + getTax();
    }

    public String summaryLine() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        return String.format(
            "#%s • Table %s • %s • $%.2f",
            id,
            tableNumber,
            formatter.format(createdAt),
            getTotal()
        );
    }
}


