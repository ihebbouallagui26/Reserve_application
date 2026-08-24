import React from 'react';
import {StyleSheet, View} from 'react-native';
import {useThemeColors} from '../theme';

interface AuthBackgroundProps {
  height: number;
  children: React.ReactNode;
}

/** Concentric ring outlines - a light, dependency-free stand-in for a topographic
 * contour pattern. Positions are percentages of the header box, so it scales with
 * it instead of relying on any one device's fixed pixel size. */
const RINGS: Array<{size: number; xPct: number; yPct: number; opacity: number}> = [
  {size: 0.95, xPct: 0.62, yPct: 0.12, opacity: 0.1},
  {size: 0.7, xPct: 0.68, yPct: 0.22, opacity: 0.14},
  {size: 0.46, xPct: 0.74, yPct: 0.34, opacity: 0.18},
  {size: 0.8, xPct: -0.12, yPct: 0.55, opacity: 0.1},
  {size: 0.55, xPct: -0.02, yPct: 0.68, opacity: 0.14},
  {size: 0.32, xPct: 0.08, yPct: 0.78, opacity: 0.18},
];

/** Blue header zone shared by every auth screen: solid brand background plus a
 * subtle topographic-style ring pattern, clipped to the header's own bounds. */
export function AuthBackground({height, children}: AuthBackgroundProps) {
  const colors = useThemeColors();
  return (
    <View style={[styles.header, {height, backgroundColor: colors.primary}]}>
      {RINGS.map((ring, index) => {
        const size = height * ring.size;
        return (
          <View
            key={index}
            style={[
              styles.ring,
              {
                width: size,
                height: size,
                borderRadius: size / 2,
                left: height * ring.xPct,
                top: height * ring.yPct,
                borderColor: `rgba(255,255,255,${ring.opacity})`,
              },
            ]}
          />
        );
      })}
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  header: {
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  ring: {
    position: 'absolute',
    borderWidth: 1.5,
  },
});
