package com.rezkna.restaurant.restaurant;

import jakarta.validation.constraints.NotBlank;

public record RestaurantStatusRequest(
        @NotBlank String status
) {
}
