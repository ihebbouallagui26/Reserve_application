package com.rezkna.restaurant.restaurant;

/** Diner-facing view of a Restaurant. Deliberately excludes status/createdAt/updatedAt -
 * internal lifecycle metadata with no value to a diner browsing published restaurants. */
public record RestaurantPublicView(
        String id,
        String name,
        String address,
        String city
) {
    public static RestaurantPublicView from(Restaurant restaurant) {
        return new RestaurantPublicView(
                restaurant.getId(), restaurant.getName(), restaurant.getAddress(), restaurant.getCity());
    }
}
