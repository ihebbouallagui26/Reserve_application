import React from 'react';
import {StyleSheet, View, useWindowDimensions} from 'react-native';
import {useThemeColors} from '../theme';

/**
 * The curved band separating the blue header from the white content on auth
 * screens. Pure View + borderRadius (no SVG dependency): a single large
 * circle, mostly clipped below the visible band, so only its shallow top cap
 * shows - the classic lightweight way to fake a wave without a new native
 * module. Width-driven so it scales with the device instead of a fixed size.
 */
export function AuthWave() {
  const colors = useThemeColors();
  const {width} = useWindowDimensions();
  const bandHeight = 44;
  const circleDiameter = width * 1.6;
  const capGap = 10;

  return (
    <View style={[styles.band, {height: bandHeight, marginTop: -bandHeight}]}>
      <View
        style={[
          styles.circle,
          {
            width: circleDiameter,
            height: circleDiameter,
            borderRadius: circleDiameter / 2,
            left: (width - circleDiameter) / 2,
            top: capGap,
            backgroundColor: colors.background,
          },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  band: {
    overflow: 'hidden',
  },
  circle: {
    position: 'absolute',
  },
});
