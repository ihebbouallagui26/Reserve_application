package com.rezkna.restaurant.restaurant;

import jakarta.validation.constraints.NotBlank;

public record RestaurantUpdateRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String city,
        Double lat,
        Double lng
) {
}
