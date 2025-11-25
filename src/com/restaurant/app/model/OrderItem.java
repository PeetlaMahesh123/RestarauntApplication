package com.restaurant.app.model;

import java.util.Objects;

public class OrderItem {
    private final MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = Objects.requireNonNull(menuItem);
        this.quantity = Math.max(1, quantity);
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity(int delta) {
        quantity = Math.max(1, quantity + delta);
    }

    public void setQuantity(int newQuantity) {
        quantity = Math.max(1, newQuantity);
    }

    public double getLineTotal() {
        return quantity * menuItem.getPrice();
    }
}


