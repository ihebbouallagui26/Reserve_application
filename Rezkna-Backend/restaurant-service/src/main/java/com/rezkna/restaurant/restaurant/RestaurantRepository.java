package com.rezkna.restaurant.restaurant;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String>, RestaurantRepositoryCustom {

    List<Restaurant> findByStatus(RestaurantStatus status);
}
