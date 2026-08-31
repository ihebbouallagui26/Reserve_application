import React, {useMemo} from 'react';
import {StyleSheet, View} from 'react-native';
import MapView from 'react-native-maps';
import {RestaurantMarker} from './RestaurantMarker';
import {EmptyState} from './EmptyState';

/**
 * Forward-compatible: latitude/longitude are optional because the current
 * backend contract (RestaurantPublicView) does not provide them at all
 * (Phase 7/8 - see the Phase 11 checkpoint 8 note). A restaurant with no
 * coordinates is simply never plotted - never a fabricated position. The
 * moment the backend adds real coordinates, passing them through here is
 * enough for markers to appear; no change to this component is needed.
 */
export interface MappableRestaurant {
  id: string;
  name: string;
  latitude?: number;
  longitude?: number;
}

interface MappedRestaurant extends MappableRestaurant {
  latitude: number;
  longitude: number;
}

interface RestaurantMapProps {
  restaurants: MappableRestaurant[];
  selectedRestaurantId?: string | null;
  onSelectRestaurant: (restaurantId: string) => void;
  onOpenRestaurant: (restaurantId: string) => void;
  /** Centers the map when nothing is selected yet - typically the diner's
   * own location (Checkpoint 9), never a restaurant's position invented for
   * lack of a real one. */
  initialRegion?: {latitude: number; longitude: number};
}

const DEFAULT_DELTA = {latitudeDelta: 0.05, longitudeDelta: 0.05};

function hasCoordinates(restaurant: MappableRestaurant): restaurant is MappedRestaurant {
  return restaurant.latitude != null && restaurant.longitude != null;
}

/**
 * Pure presentation - no API calls inside (restaurantApi/locationService stay
 * in the screens that own data-fetching). Today every `restaurants` entry
 * passed in has no coordinates (see MappableRestaurant's own doc comment),
 * so this always shows the explanatory empty state rather than a map with
 * nothing on it - honest about the current limitation, not silently broken.
 */
export function RestaurantMap({
  restaurants,
  selectedRestaurantId,
  onSelectRestaurant,
  onOpenRestaurant,
  initialRegion,
}: RestaurantMapProps) {
  const mappable = useMemo(() => restaurants.filter(hasCoordinates), [restaurants]);

  if (mappable.length === 0) {
    return (
      <View style={styles.container}>
        <EmptyState message="La géolocalisation des restaurants n'est pas encore disponible." />
      </View>
    );
  }

  const region = initialRegion
    ? {...DEFAULT_DELTA, ...initialRegion}
    : {...DEFAULT_DELTA, latitude: mappable[0].latitude, longitude: mappable[0].longitude};

  return (
    <View style={styles.container}>
      <MapView style={styles.map} initialRegion={region} testID="restaurant-map">
        {mappable.map(r => (
          <RestaurantMarker
            key={r.id}
            name={r.name}
            latitude={r.latitude}
            longitude={r.longitude}
            selected={r.id === selectedRestaurantId}
            onPress={() => onSelectRestaurant(r.id)}
            onCalloutPress={() => onOpenRestaurant(r.id)}
          />
        ))}
      </MapView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  map: {
    flex: 1,
  },
});
