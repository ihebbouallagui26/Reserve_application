package com.rezkna.restaurant.restaurant;

import java.time.Instant;

/** Client-facing view of a Restaurant. */
public record RestaurantView(
        String id,
        String name,
        String address,
        String city,
        RestaurantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static RestaurantView from(Restaurant restaurant) {
        return new RestaurantView(
                restaurant.getId(), restaurant.getName(), restaurant.getAddress(), restaurant.getCity(),
                restaurant.getStatus(), restaurant.getCreatedAt(), restaurant.getUpdatedAt());
    }
}
