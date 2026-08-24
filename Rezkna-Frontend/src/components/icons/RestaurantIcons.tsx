import React from 'react';
import Svg, {Circle, Path} from 'react-native-svg';

export interface RestaurantIconProps {
  size: number;
  color: string;
}

/**
 * Small set of original, hand-authored line icons for restaurant-themed
 * decoration (chef hat, storefront, fork+spoon, serving cloche, coffee cup).
 * Simple geometric strokes on a 24x24 grid - not traced or copied from any
 * icon library or proprietary asset, just common, unownable conventions for
 * drawing these objects as outlines.
 */
const strokeProps = {
  fill: 'none',
  strokeWidth: 1.6,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

export function ChefHatIcon({size, color}: RestaurantIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M7 9a3 3 0 0 1 1-5.7A3.5 3.5 0 0 1 12 2a3.5 3.5 0 0 1 4 1.3A3 3 0 0 1 17 9" stroke={color} {...strokeProps} />
      <Path d="M7 9v4a5 5 0 0 0 10 0V9" stroke={color} {...strokeProps} />
      <Path d="M7.5 17.5h9" stroke={color} {...strokeProps} />
      <Path d="M8 20.2h8" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function ShopIcon({size, color}: RestaurantIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M4 9l1.4-4.2h13.2L20 9" stroke={color} {...strokeProps} />
      <Path
        d="M4 9a2 2 0 0 0 4 0 2 2 0 0 0 4 0 2 2 0 0 0 4 0 2 2 0 0 0 4 0"
        stroke={color}
        {...strokeProps}
      />
      <Path d="M5 9.5V19h14V9.5" stroke={color} {...strokeProps} />
      <Path d="M10 19v-5h4v5" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function ForkSpoonIcon({size, color}: RestaurantIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M7 2v6a2 2 0 0 0 4 0V2" stroke={color} {...strokeProps} />
      <Path d="M9 2v20" stroke={color} {...strokeProps} />
      <Path d="M17 2c-1.6 1-2.4 2.8-2.4 4.8S15.4 10.4 17 11" stroke={color} {...strokeProps} />
      <Path d="M17 2v20" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function ClocheIcon({size, color}: RestaurantIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M3 17a9 9 0 0 1 18 0" stroke={color} {...strokeProps} />
      <Path d="M2 17.2h20" stroke={color} {...strokeProps} />
      <Path d="M2 20h20" stroke={color} {...strokeProps} />
      <Circle cx="12" cy="5.2" r="1.1" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function CoffeeCupIcon({size, color}: RestaurantIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M4 8h13v6a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5V8Z" stroke={color} {...strokeProps} />
      <Path d="M17 9.5h1.3a2.3 2.3 0 0 1 0 4.6H17" stroke={color} {...strokeProps} />
      <Path d="M8 3.6c0 1-1 1-1 2M12 3.6c0 1-1 1-1 2" stroke={color} {...strokeProps} />
    </Svg>
  );
}
