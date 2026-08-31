import {apiRequest} from './client';
import type {MenuPublicView} from '../../models/menu';

/** Relative to restaurant-service, via the Gateway. Public - no token. */
export const menuApi = {
  getByRestaurantId: (restaurantId: string) =>
    apiRequest<MenuPublicView>(`/api/restaurant/restaurants/public/${restaurantId}/menu`),
};
