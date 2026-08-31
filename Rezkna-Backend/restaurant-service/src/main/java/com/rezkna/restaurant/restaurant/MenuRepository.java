package com.rezkna.restaurant.restaurant;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MenuRepository extends MongoRepository<Menu, String> {

    Optional<Menu> findByRestaurantId(String restaurantId);
}
