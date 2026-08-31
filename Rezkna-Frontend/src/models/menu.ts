/**
 * Mirrors restaurant-service's MenuItemView/MenuPublicView exactly (see
 * Rezkna-Backend/restaurant-service/.../restaurant/MenuItemView.java and
 * MenuPublicView.java). The public menu endpoint never exposes the Mongo
 * Menu document itself (id/restaurantId/updatedAt) - only items.
 */
export interface MenuItem {
  id: string;
  name: string;
  description: string | null;
  price: number;
  category: string | null;
  available: boolean;
}

export interface MenuPublicView {
  items: MenuItem[];
}
