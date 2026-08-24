package com.rezkna.restaurant.restaurant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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
}
