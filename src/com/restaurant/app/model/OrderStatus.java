package com.restaurant.app.model;

public enum OrderStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    SERVED("Served"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}


