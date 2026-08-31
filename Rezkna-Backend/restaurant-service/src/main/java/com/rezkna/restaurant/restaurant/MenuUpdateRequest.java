package com.rezkna.restaurant.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** An empty list is a valid menu (a Partner clearing it out or starting fresh) -
 * mirrors the old backend's saveMenu(), which never rejected an empty section list. */
public record MenuUpdateRequest(
        @NotNull @Valid List<MenuItemRequest> items
) {
}
