package com.rezkna.restaurant.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class RestaurantRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private RestaurantRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void saveAndFindRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Le Rezkna");
        restaurant.setAddress("1 Avenue Habib Bourguiba");
        restaurant.setCity("Tunis");
        restaurant.setStatus(RestaurantStatus.PENDING);
        Instant now = Instant.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);

        Restaurant saved = repository.save(restaurant);

        Optional<Restaurant> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Le Rezkna");
        assertThat(found.get().getStatus()).isEqualTo(RestaurantStatus.PENDING);
    }

    @Test
    void updateRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Le Rezkna");
        restaurant.setAddress("1 Avenue Habib Bourguiba");
        restaurant.setCity("Tunis");
        restaurant.setStatus(RestaurantStatus.PENDING);
        Instant now = Instant.now();
        restaurant.setCreatedAt(now);
        restaurant.setUpdatedAt(now);
        Restaurant saved = repository.save(restaurant);

        saved.setStatus(RestaurantStatus.ACTIVE);
        saved.setUpdatedAt(Instant.now());
        repository.save(saved);

        Restaurant updated = repository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RestaurantStatus.ACTIVE);
    }

    @Test
    void listsAllRestaurants() {
        Restaurant first = new Restaurant();
        first.setName("Restaurant A");
        first.setAddress("Address A");
        first.setCity("Tunis");
        first.setStatus(RestaurantStatus.PENDING);
        first.setCreatedAt(Instant.now());
        first.setUpdatedAt(Instant.now());
        repository.save(first);

        Restaurant second = new Restaurant();
        second.setName("Restaurant B");
        second.setAddress("Address B");
        second.setCity("Sfax");
        second.setStatus(RestaurantStatus.ACTIVE);
        second.setCreatedAt(Instant.now());
        second.setUpdatedAt(Instant.now());
        repository.save(second);

        List<Restaurant> all = repository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void repositoryReturnsEmptyForUnknownId() {
        Optional<Restaurant> found = repository.findById("does-not-exist");
        assertThat(found).isEmpty();
    }

    @Test
    void findByStatusReturnsOnlyMatchingRestaurants() {
        Restaurant active = new Restaurant();
        active.setName("Active Restaurant");
        active.setAddress("Address A");
        active.setCity("Tunis");
        active.setStatus(RestaurantStatus.ACTIVE);
        active.setCreatedAt(Instant.now());
        active.setUpdatedAt(Instant.now());
        repository.save(active);

        Restaurant pending = new Restaurant();
        pending.setName("Pending Restaurant");
        pending.setAddress("Address B");
        pending.setCity("Sfax");
        pending.setStatus(RestaurantStatus.PENDING);
        pending.setCreatedAt(Instant.now());
        pending.setUpdatedAt(Instant.now());
        repository.save(pending);

        List<Restaurant> activeOnly = repository.findByStatus(RestaurantStatus.ACTIVE);

        assertThat(activeOnly).hasSize(1);
        assertThat(activeOnly.get(0).getName()).isEqualTo("Active Restaurant");
    }

    // --- findByStatusAndLocationNear (geospatial search) ---

    private static final double TUNIS_LAT = 36.8065;
    private static final double TUNIS_LNG = 10.1815;
    // ~55km north of Tunis (1 degree of latitude is ~111km) - well outside a 10km search.
    private static final double FAR_LAT = 37.3065;

    private Restaurant restaurantAt(String name, RestaurantStatus status, Double lat, Double lng) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress("Address");
        restaurant.setCity("Tunis");
        restaurant.setStatus(status);
        if (lat != null && lng != null) {
            restaurant.setLocation(new GeoJsonPoint(lng, lat));
        }
        restaurant.setCreatedAt(Instant.now());
        restaurant.setUpdatedAt(Instant.now());
        return restaurant;
    }

    @Test
    void findByStatusAndLocationNearReturnsNearbyActiveRestaurant() {
        repository.save(restaurantAt("Le Rezkna", RestaurantStatus.ACTIVE, TUNIS_LAT, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(10, Metrics.KILOMETERS));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Le Rezkna");
    }

    @Test
    void findByStatusAndLocationNearExcludesRestaurantsOutsideRadius() {
        repository.save(restaurantAt("Nearby", RestaurantStatus.ACTIVE, TUNIS_LAT, TUNIS_LNG));
        repository.save(restaurantAt("Far Away", RestaurantStatus.ACTIVE, FAR_LAT, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(10, Metrics.KILOMETERS));

        assertThat(results).extracting(Restaurant::getName).containsExactly("Nearby");
    }

    @Test
    void findByStatusAndLocationNearIncludesFarRestaurantWithinALargerRadius() {
        repository.save(restaurantAt("Far Away", RestaurantStatus.ACTIVE, FAR_LAT, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        // ~55km away, so a 100km radius (in meters) must include it - proves the distance
        // conversion is real kilometers-to-meters, not an accidentally tiny/huge unit.
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(100, Metrics.KILOMETERS));

        assertThat(results).extracting(Restaurant::getName).contains("Far Away");
    }

    @Test
    void findByStatusAndLocationNearExcludesInactiveRestaurants() {
        repository.save(restaurantAt("Nearby Inactive", RestaurantStatus.INACTIVE, TUNIS_LAT, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(10, Metrics.KILOMETERS));

        assertThat(results).isEmpty();
    }

    @Test
    void findByStatusAndLocationNearExcludesPendingRestaurants() {
        repository.save(restaurantAt("Nearby Pending", RestaurantStatus.PENDING, TUNIS_LAT, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(10, Metrics.KILOMETERS));

        assertThat(results).isEmpty();
    }

    @Test
    void findByStatusAndLocationNearExcludesRestaurantsWithoutCoordinates() {
        repository.save(restaurantAt("No Coordinates", RestaurantStatus.ACTIVE, null, null));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(10, Metrics.KILOMETERS));

        assertThat(results).isEmpty();
    }

    @Test
    void findByStatusAndLocationNearOrdersResultsNearestFirst() {
        // ~11km away
        repository.save(restaurantAt("Closer", RestaurantStatus.ACTIVE, TUNIS_LAT + 0.1, TUNIS_LNG));
        // ~22km away
        repository.save(restaurantAt("Farther", RestaurantStatus.ACTIVE, TUNIS_LAT + 0.2, TUNIS_LNG));

        Point searchPoint = new Point(TUNIS_LNG, TUNIS_LAT);
        List<Restaurant> results = repository.findByStatusAndLocationNear(
                RestaurantStatus.ACTIVE, searchPoint, new Distance(30, Metrics.KILOMETERS));

        assertThat(results).extracting(Restaurant::getName).containsExactly("Closer", "Farther");
    }
}
