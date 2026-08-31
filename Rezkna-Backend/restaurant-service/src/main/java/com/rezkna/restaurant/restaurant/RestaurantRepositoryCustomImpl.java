package com.rezkna.restaurant.restaurant;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** MongoDB's $near operator cannot be combined with other filters via $and, which is
 * what a derived query method like findByStatusAndLocationNear would generate - it runs
 * as a $geoNear aggregation instead, which supports a pre-filter query natively and, as a
 * side effect, guarantees nearest-first ordering by construction (not something this
 * class has to sort itself). */
@Repository
public class RestaurantRepositoryCustomImpl implements RestaurantRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public RestaurantRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** distance must carry Metrics.KILOMETERS (or another real Metric, never NEUTRAL):
     * for a spherical $geoNear, Spring Data multiplies the raw Distance value by the
     * metric's Earth-radius-based multiplier to get maxDistance in meters - confirmed by
     * inspecting the actual generated $geoNear document, since NEUTRAL's multiplier is
     * itself the Earth's radius (silently turning a "raw meters" Distance into a value
     * ~6.4 million times too large, not a no-op as its name might suggest). */
    @Override
    public List<Restaurant> findByStatusAndLocationNear(RestaurantStatus status, Point point, Distance distance) {
        // Explicitly GeoJSON, matching Restaurant.location's type - passing a plain Point
        // left $geoNear's maxDistance/spherical interaction under-specified and silently
        // returned results far outside the requested radius during testing.
        NearQuery nearQuery = NearQuery.near(new GeoJsonPoint(point.getX(), point.getY()))
                .spherical(true)
                .maxDistance(distance)
                .query(Query.query(Criteria.where("status").is(status)));

        var geoResults = mongoTemplate.geoNear(nearQuery, Restaurant.class);
        return geoResults.getContent().stream()
                .map(GeoResult::getContent)
                .toList();
    }
}
