package com.rezkna.restaurant.restaurant;

import com.rezkna.common.exception.BadRequestException;
import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Unauthenticated diner-facing restaurant discovery. Kept separate from
 * RestaurantController (the Platform Owner CRUD) so the public surface can never
 * accidentally inherit an admin-only route or an admin-only response shape. */
@RestController
public class RestaurantPublicController {

    /** Bounds the geo search to a realistic metropolitan/regional radius rather than
     * an effectively unbounded collection scan dressed up as "nearby". */
    private static final double MAX_RADIUS_KM = 100.0;

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;

    public RestaurantPublicController(RestaurantRepository restaurantRepository, MenuRepository menuRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
    }

    @GetMapping("/restaurants/public")
    public ApiResponse<List<RestaurantPublicView>> listPublic() {
        List<RestaurantPublicView> restaurants = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE).stream()
                .map(RestaurantPublicView::from)
                .toList();
        return ApiResponse.success(restaurants);
    }

    @GetMapping("/restaurants/public/{id}")
    public ApiResponse<RestaurantPublicView> getPublicById(@PathVariable String id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .filter(r -> r.getStatus() == RestaurantStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return ApiResponse.success(RestaurantPublicView.from(restaurant));
    }

    /** No menu document yet for an ACTIVE restaurant returns 200 with an empty item list,
     * never 404 - matching the old backend's MenuController.publicMenu() exactly, which
     * always returned an (empty, freshly-built) menu rather than a not-found error. */
    @GetMapping("/restaurants/public/{restaurantId}/menu")
    public ApiResponse<MenuPublicView> getPublicMenu(@PathVariable String restaurantId) {
        restaurantRepository.findById(restaurantId)
                .filter(r -> r.getStatus() == RestaurantStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        List<MenuItemView> items = menuRepository.findByRestaurantId(restaurantId)
                .map(menu -> menu.getItems().stream().map(MenuItemView::from).toList())
                .orElseGet(List::of);
        return ApiResponse.success(new MenuPublicView(items));
    }

    @GetMapping("/restaurants/public/search")
    public ApiResponse<List<RestaurantPublicView>> search(@RequestParam(required = false) Double lat,
                                                            @RequestParam(required = false) Double lng,
                                                            @RequestParam(required = false) Double radiusKm) {
        if (lat == null || lng == null || radiusKm == null) {
            throw new BadRequestException("lat, lng and radiusKm are required");
        }
        if (lat < -90 || lat > 90) {
            throw new BadRequestException("lat must be between -90 and 90");
        }
        if (lng < -180 || lng > 180) {
            throw new BadRequestException("lng must be between -180 and 180");
        }
        if (radiusKm <= 0) {
            throw new BadRequestException("radiusKm must be positive");
        }
        if (radiusKm > MAX_RADIUS_KM) {
            throw new BadRequestException("radiusKm must not exceed " + MAX_RADIUS_KM);
        }

        // GeoJSON order is [longitude, latitude], matching Restaurant.location. Metrics.KILOMETERS
        // is required here, not Metrics.NEUTRAL: for a spherical $geoNear, Spring Data derives
        // maxDistance (in meters) from the Distance's metric multiplier - NEUTRAL's multiplier is
        // itself the Earth's radius, so a "raw meters" Distance would silently become ~6.4 million
        // times too large (confirmed against the actual generated $geoNear document).
        Point point = new Point(lng, lat);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);

        List<RestaurantPublicView> restaurants = restaurantRepository
                .findByStatusAndLocationNear(RestaurantStatus.ACTIVE, point, distance).stream()
                .map(RestaurantPublicView::from)
                .toList();
        return ApiResponse.success(restaurants);
    }
}
