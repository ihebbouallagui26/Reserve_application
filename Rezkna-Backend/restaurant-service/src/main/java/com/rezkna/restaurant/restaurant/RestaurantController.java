package com.rezkna.restaurant.restaurant;

import com.rezkna.common.exception.BadRequestException;
import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@Validated
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final PlatformTokenVerifier platformTokenVerifier;

    public RestaurantController(RestaurantRepository restaurantRepository,
                                 PlatformTokenVerifier platformTokenVerifier) {
        this.restaurantRepository = restaurantRepository;
        this.platformTokenVerifier = platformTokenVerifier;
    }

    @PostMapping("/restaurants")
    public ApiResponse<RestaurantView> create(@RequestHeader("Authorization") String authorization,
                                               @Valid @RequestBody RestaurantCreateRequest request) {
        requirePlatformOwner(authorization);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        restaurant.setCity(request.city());
        restaurant.setStatus(RestaurantStatus.PENDING);
        restaurant.setLocation(resolveLocation(request.lat(), request.lng()));
        Instant now = Instant.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);

        Restaurant saved = restaurantRepository.save(restaurant);
        return ApiResponse.success(RestaurantView.from(saved));
    }

    @GetMapping("/restaurants")
    public ApiResponse<List<RestaurantView>> list(@RequestHeader("Authorization") String authorization) {
        requirePlatformOwner(authorization);

        List<RestaurantView> restaurants = restaurantRepository.findAll().stream()
                .map(RestaurantView::from)
                .toList();
        return ApiResponse.success(restaurants);
    }

    @GetMapping("/restaurants/{id}")
    public ApiResponse<RestaurantView> getById(@RequestHeader("Authorization") String authorization,
                                                @PathVariable String id) {
        requirePlatformOwner(authorization);
        return ApiResponse.success(RestaurantView.from(requireRestaurant(id)));
    }

    @PutMapping("/restaurants/{id}")
    public ApiResponse<RestaurantView> update(@RequestHeader("Authorization") String authorization,
                                               @PathVariable String id,
                                               @Valid @RequestBody RestaurantUpdateRequest request) {
        requirePlatformOwner(authorization);

        Restaurant restaurant = requireRestaurant(id);
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        restaurant.setCity(request.city());
        restaurant.setLocation(resolveLocation(request.lat(), request.lng()));
        restaurant.setUpdatedAt(Instant.now());

        Restaurant saved = restaurantRepository.save(restaurant);
        return ApiResponse.success(RestaurantView.from(saved));
    }

    @PatchMapping("/restaurants/{id}/status")
    public ApiResponse<RestaurantView> updateStatus(@RequestHeader("Authorization") String authorization,
                                                     @PathVariable String id,
                                                     @Valid @RequestBody RestaurantStatusRequest request) {
        requirePlatformOwner(authorization);

        Restaurant restaurant = requireRestaurant(id);
        restaurant.setStatus(parseStatus(request.status()));
        restaurant.setUpdatedAt(Instant.now());

        Restaurant saved = restaurantRepository.save(restaurant);
        return ApiResponse.success(RestaurantView.from(saved));
    }

    private Restaurant requireRestaurant(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    /** Coordinates are optional (a restaurant can exist without them, per Phase 8's rule
     * that such restaurants simply never surface in a geo search) but must be provided
     * together and within valid ranges when given at all. */
    private GeoJsonPoint resolveLocation(Double lat, Double lng) {
        if (lat == null && lng == null) {
            return null;
        }
        if (lat == null || lng == null) {
            throw new BadRequestException("lat and lng must both be provided together");
        }
        if (lat < -90 || lat > 90) {
            throw new BadRequestException("lat must be between -90 and 90");
        }
        if (lng < -180 || lng > 180) {
            throw new BadRequestException("lng must be between -180 and 180");
        }
        return new GeoJsonPoint(lng, lat);
    }

    private RestaurantStatus parseStatus(String value) {
        try {
            return RestaurantStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status - must be ACTIVE, INACTIVE, or PENDING");
        }
    }

    private void requirePlatformOwner(String authorizationHeader) {
        PlatformPrincipal principal = platformTokenVerifier.requireClaims(authorizationHeader);
        if (!"PLATFORM_OWNER".equals(principal.role())) {
            throw new AccessDeniedException("PLATFORM_OWNER role required");
        }
    }
}
