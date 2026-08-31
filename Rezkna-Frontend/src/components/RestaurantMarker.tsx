import React from 'react';
import {Marker} from 'react-native-maps';
import {MapPinIcon} from './icons/UiIcons';
import {useThemeColors} from '../theme';

interface RestaurantMarkerProps {
  name: string;
  latitude: number;
  longitude: number;
  selected?: boolean;
  /** Tapping the pin selects it (mirrors a RestaurantCard tap). */
  onPress: () => void;
  /** Tapping the popup callout (the name bubble shown after selecting)
   * opens the fiche - the map equivalent of tapping an already-selected
   * RestaurantCard a second time. */
  onCalloutPress: () => void;
}

export function RestaurantMarker({
  name,
  latitude,
  longitude,
  selected = false,
  onPress,
  onCalloutPress,
}: RestaurantMarkerProps) {
  const colors = useThemeColors();
  return (
    <Marker
      coordinate={{latitude, longitude}}
      title={name}
      accessibilityLabel={name}
      onPress={onPress}
      onCalloutPress={onCalloutPress}>
      <MapPinIcon size={selected ? 34 : 26} color={selected ? colors.primary : colors.textSecondary} />
    </Marker>
  );
}
