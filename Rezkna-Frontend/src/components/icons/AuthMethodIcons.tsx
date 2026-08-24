import React from 'react';
import Svg, {Path, Rect} from 'react-native-svg';

export interface AuthMethodIconProps {
  size: number;
  color: string;
}

/**
 * Small set of original, hand-authored line icons for the auth method
 * buttons (Google, Facebook, phone, email) - simple geometric strokes on a
 * 24x24 grid, matching RestaurantIcons.tsx. Google/Facebook are drawn as
 * generic monochrome marks (an open ring + crossbar for "G", a stem + hook
 * for "f") rather than traced brand logos, since the button label already
 * names the provider.
 */
const strokeProps = {
  fill: 'none',
  strokeWidth: 2,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

export function GoogleIcon({size, color}: AuthMethodIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path d="M17 7a7 7 0 1 0 0 10" stroke={color} {...strokeProps} />
      <Path d="M17 11.3h-3.3v2.8" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function FacebookIcon({size, color}: AuthMethodIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Path
        d="M14 9.2c-.4-.15-.9-.2-1.3-.2-1 0-1.7.7-1.7 1.8v7.7"
        stroke={color}
        {...strokeProps}
      />
      <Path d="M10 12.5h4" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function PhoneMethodIcon({size, color}: AuthMethodIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Rect x="7" y="3.5" width="10" height="17" rx="1.6" stroke={color} {...strokeProps} />
      <Path d="M11 17.2h2" stroke={color} {...strokeProps} />
    </Svg>
  );
}

export function EmailMethodIcon({size, color}: AuthMethodIconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 24 24">
      <Rect x="3" y="6" width="18" height="12" rx="1.6" stroke={color} {...strokeProps} />
      <Path d="M4 7.2l8 6 8-6" stroke={color} {...strokeProps} />
    </Svg>
  );
}
