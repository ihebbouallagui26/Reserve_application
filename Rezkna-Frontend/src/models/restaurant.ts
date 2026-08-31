/**
 * Mirrors restaurant-service's RestaurantPublicView exactly (see
 * Rezkna-Backend/restaurant-service/.../restaurant/RestaurantPublicView.java).
 * Never add a field here that the backend does not actually return - no
 * status/coordinates/photo, none of which the public DTO exposes.
 */
export interface RestaurantPublicView {
  id: string;
  name: string;
  address: string;
  city: string;
}
