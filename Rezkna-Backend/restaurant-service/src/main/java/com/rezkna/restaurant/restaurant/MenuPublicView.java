package com.rezkna.restaurant.restaurant;

import java.util.List;

/** Diner-facing menu view. Deliberately excludes restaurantId/updatedAt - internal/
 * administrative metadata with no value to a diner, mirroring RestaurantPublicView. */
public record MenuPublicView(
        List<MenuItemView> items
) {
}
