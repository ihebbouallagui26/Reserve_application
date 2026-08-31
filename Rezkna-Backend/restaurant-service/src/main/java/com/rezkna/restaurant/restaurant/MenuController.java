package com.rezkna.restaurant.restaurant;

import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Partner-scoped menu write. Kept separate from RestaurantController (Platform Owner)
 * and RestaurantPublicController (unauthenticated) - one controller, one authorization
 * model, matching the split already established between those two. */
@RestController
@Validated
public class MenuController {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final PartnerTokenVerifier partnerTokenVerifier;

    public MenuController(MenuRepository menuRepository,
                           RestaurantRepository restaurantRepository,
                           PartnerTokenVerifier partnerTokenVerifier) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
        this.partnerTokenVerifier = partnerTokenVerifier;
    }

    /** Replaces the restaurant's whole menu. No restaurant-status gate: a Partner can
     * prepare their menu before Platform Owner activation, exactly as the old backend
     * never gated PartnerController.updateProperty()/MenuController.saveMenu() on
     * Property.published - that flag only ever gated public discovery visibility. */
    @PutMapping("/restaurants/{restaurantId}/menu")
    public ApiResponse<MenuView> save(@RequestHeader("Authorization") String authorization,
                                       @PathVariable String restaurantId,
                                       @Valid @RequestBody MenuUpdateRequest request) {
        requirePartnerForRestaurant(authorization, restaurantId);

        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new ResourceNotFoundException("Restaurant not found");
        }

        Menu menu = menuRepository.findByRestaurantId(restaurantId)
                .orElseGet(() -> {
                    Menu fresh = new Menu();
                    fresh.setRestaurantId(restaurantId);
                    return fresh;
                });

        List<MenuItem> items = request.items().stream().map(this::toMenuItem).toList();
        menu.setItems(items);
        menu.setUpdatedAt(Instant.now());

        Menu saved = menuRepository.save(menu);
        return ApiResponse.success(MenuView.from(saved));
    }

    private MenuItem toMenuItem(MenuItemRequest request) {
        MenuItem item = new MenuItem();
        item.setId(UUID.randomUUID().toString());
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(request.category());
        item.setAvailable(request.available() == null || request.available());
        return item;
    }

    /** No role check beyond ownership: the old backend's MenuController.saveMenu() never
     * restricted by role (unlike PartnerController's OWNER-only actions), so both OWNER
     * and HOST may edit their own restaurant's menu - confirmed by that omission, not
     * assumed. Checked before restaurant existence, so a Partner probing a restaurantId
     * they do not own never learns whether it exists. */
    private void requirePartnerForRestaurant(String authorizationHeader, String restaurantId) {
        PartnerPrincipal principal = partnerTokenVerifier.requireClaims(authorizationHeader);
        if (!restaurantId.equals(principal.propertyId())) {
            throw new AccessDeniedException("Not authorized for this restaurant");
        }
    }
}
