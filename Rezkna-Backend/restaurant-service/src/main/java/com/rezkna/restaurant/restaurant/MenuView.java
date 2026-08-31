package com.rezkna.restaurant.restaurant;

import java.time.Instant;
import java.util.List;

/** Partner-facing view (write response) - includes restaurantId/updatedAt, unlike
 * MenuPublicView, mirroring the RestaurantView/RestaurantPublicView split. */
public record MenuView(
        String restaurantId,
        List<MenuItemView> items,
        Instant updatedAt
) {
    public static MenuView from(Menu menu) {
        List<MenuItemView> items = menu.getItems().stream().map(MenuItemView::from).toList();
        return new MenuView(menu.getRestaurantId(), items, menu.getUpdatedAt());
    }
}
