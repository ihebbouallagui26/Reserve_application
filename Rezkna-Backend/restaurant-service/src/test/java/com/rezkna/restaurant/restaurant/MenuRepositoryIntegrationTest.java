package com.rezkna.restaurant.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class MenuRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MenuRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    private MenuItem item(String name, String price) {
        MenuItem item = new MenuItem();
        item.setId(java.util.UUID.randomUUID().toString());
        item.setName(name);
        item.setPrice(new BigDecimal(price));
        item.setAvailable(true);
        return item;
    }

    @Test
    void saveAndFindMenuByRestaurantId() {
        Menu menu = new Menu();
        menu.setRestaurantId("restaurant-A");
        menu.setItems(List.of(item("Couscous", "12.50")));
        menu.setUpdatedAt(Instant.now());

        repository.save(menu);

        Optional<Menu> found = repository.findByRestaurantId("restaurant-A");
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getName()).isEqualTo("Couscous");
        assertThat(found.get().getItems().get(0).getPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    void findByRestaurantIdReturnsEmptyForUnknownRestaurant() {
        Optional<Menu> found = repository.findByRestaurantId("does-not-exist");
        assertThat(found).isEmpty();
    }

    @Test
    void enforcesUniqueRestaurantId() {
        Menu first = new Menu();
        first.setRestaurantId("restaurant-A");
        repository.save(first);

        Menu duplicate = new Menu();
        duplicate.setRestaurantId("restaurant-A");

        assertThatThrownBy(() -> repository.save(duplicate)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void updateReplacesItems() {
        Menu menu = new Menu();
        menu.setRestaurantId("restaurant-A");
        menu.setItems(List.of(item("Old Dish", "5.00")));
        menu.setUpdatedAt(Instant.now());
        Menu saved = repository.save(menu);

        saved.setItems(List.of(item("New Dish", "8.00")));
        saved.setUpdatedAt(Instant.now());
        repository.save(saved);

        Menu updated = repository.findByRestaurantId("restaurant-A").orElseThrow();
        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getItems().get(0).getName()).isEqualTo("New Dish");
    }
}
