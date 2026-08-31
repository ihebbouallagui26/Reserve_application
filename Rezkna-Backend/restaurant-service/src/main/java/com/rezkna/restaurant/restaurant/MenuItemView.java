package com.rezkna.restaurant.restaurant;

import java.math.BigDecimal;

public record MenuItemView(
        String id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available
) {
    public static MenuItemView from(MenuItem item) {
        return new MenuItemView(
                item.getId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getCategory(), item.isAvailable());
    }
}
