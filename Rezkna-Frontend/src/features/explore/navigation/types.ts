/**
 * Param list for the public + diner shell (MainNavigator). Explore/Map/Account
 * are reachable with no session at all - only RestaurantDetail/RestaurantMenu
 * carry params, and only the id, never the full restaurant/menu object, since
 * the id is all a screen needs to refetch via restaurantApi/menuApi.
 */
export type MainStackParamList = {
  Explore: undefined;
  Map: undefined;
  Account: undefined;
  RestaurantDetail: {restaurantId: string};
  RestaurantMenu: {restaurantId: string};
};
