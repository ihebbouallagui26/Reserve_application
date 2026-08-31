import React from 'react';
import Svg, {Circle, Line, Path} from 'react-native-svg';

export interface UiIconProps {
  size: number;
  color: string;
}

/** Same hand-authored convention as RestaurantIcons.tsx: simple geometric
 * strokes on a 24x24 grid, not traced or copied from any icon library. */
const strokeProps = {
  fill: 'none',
  strokeWidth: 1.6,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

export function SearchIcon({size, color}: UiIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Circle cx="10.5" cy="10.5" r="6.5" stroke={color} {...strokeProps} />
      <Line x1="15.3" y1="15.3" x2="20.5" y2="20.5" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function MapPinIcon({size, color}: UiIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M12 21s7-6.5 7-12a7 7 0 0 0-14 0c0 5.5 7 12 7 12Z" stroke={color} {...strokeProps} />
      <Circle cx="12" cy="9" r="2.4" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function CompassIcon({size, color}: UiIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Circle cx="12" cy="12" r="9" stroke={color} {...strokeProps} />
      <Path d="M15 9l-2 5-4 1 2-5 4-1Z" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function AccountIcon({size, color}: UiIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Circle cx="12" cy="8" r="3.4" stroke={color} {...strokeProps} />
      <Path d="M5 20c1.2-4 4-6 7-6s5.8 2 7 6" stroke={color} {...strokeProps} />
    </Svg>
  );
}
