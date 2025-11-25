package com.restaurant.app.model;

import java.util.Objects;

public final class MenuItem {
    private final String code;
    private final String name;
    private final String category;
    private final String description;
    private final double price;

    public MenuItem(String code, String name, String category, String description, double price) {
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.category = Objects.requireNonNull(category);
        this.description = Objects.requireNonNullElse(description, "");
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " (" + String.format("$%.2f", price) + ")";
    }
}


