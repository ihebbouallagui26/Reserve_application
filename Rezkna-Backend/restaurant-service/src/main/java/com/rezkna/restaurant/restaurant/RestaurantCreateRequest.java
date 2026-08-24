package com.rezkna.restaurant.restaurant;

import jakarta.validation.constraints.NotBlank;

public record RestaurantCreateRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String city
) {
}
