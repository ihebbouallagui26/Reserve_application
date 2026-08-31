package com.rezkna.restaurant.restaurant;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.List;

public interface RestaurantRepositoryCustom {

    List<Restaurant> findByStatusAndLocationNear(RestaurantStatus status, Point point, Distance distance);
}
