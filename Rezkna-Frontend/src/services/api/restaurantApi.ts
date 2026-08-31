import {apiRequest} from './client';
import type {RestaurantPublicView} from '../../models/restaurant';

interface SearchParams {
  lat: number;
  lng: number;
  radiusKm: number;
}

/** All calls relative to restaurant-service, via the Gateway (/api/restaurant/**).
 * Every route here is the public, unauthenticated surface - no token, ever. */
export const restaurantApi = {
  list: () => apiRequest<RestaurantPublicView[]>('/api/restaurant/restaurants/public'),

  getById: (id: string) =>
    apiRequest<RestaurantPublicView>(`/api/restaurant/restaurants/public/${id}`),

  search: ({lat, lng, radiusKm}: SearchParams) =>
    apiRequest<RestaurantPublicView[]>(
      `/api/restaurant/restaurants/public/search?lat=${lat}&lng=${lng}&radiusKm=${radiusKm}`,
    ),
};
