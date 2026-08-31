import {useWindowDimensions} from 'react-native';

export type DeviceType = 'phone' | 'tablet';
export type Orientation = 'portrait' | 'landscape';

/**
 * Android's own official tablet threshold (600dp shortest side), applied via
 * min(width, height) rather than raw width so classification stays correct
 * regardless of orientation - a tablet in portrait must still read as
 * 'tablet', not fall back to the phone layout.
 */
const TABLET_MIN_DIMENSION = 600;

function classify(width: number, height: number): DeviceType {
  return Math.min(width, height) >= TABLET_MIN_DIMENSION ? 'tablet' : 'phone';
}

function orient(width: number, height: number): Orientation {
  return width > height ? 'landscape' : 'portrait';
}

/** useWindowDimensions (not Dimensions.get, which is a static snapshot)
 * re-renders automatically on rotation. */
export function useDeviceType(): DeviceType {
  const {width, height} = useWindowDimensions();
  return classify(width, height);
}

export function useOrientation(): Orientation {
  const {width, height} = useWindowDimensions();
  return orient(width, height);
}

export interface ResponsiveLayout {
  deviceType: DeviceType;
  orientation: Orientation;
  width: number;
  height: number;
}

export function useResponsiveLayout(): ResponsiveLayout {
  const {width, height} = useWindowDimensions();
  return {
    deviceType: classify(width, height),
    orientation: orient(width, height),
    width,
    height,
  };
}
