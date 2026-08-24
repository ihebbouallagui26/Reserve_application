import React, {useEffect, useRef} from 'react';
import {Animated, Easing, StyleSheet} from 'react-native';
import type {RestaurantIconProps} from './icons/RestaurantIcons';

interface FloatingIconProps {
  Icon: React.ComponentType<RestaurantIconProps>;
  size: number;
  color: string;
  top: number | string;
  left: number | string;
  /** 'ltr' drifts left-to-right, 'rtl' drifts right-to-left - mixing the two
   * across a row of icons is what makes them cross paths. */
  direction: 'ltr' | 'rtl';
  /** How far the icon drifts from its resting point, in px, one way. */
  travel?: number;
  durationMs?: number;
  delayMs?: number;
  baseOpacity?: number;
}

/**
 * Purely decorative restaurant-themed glyph. Drifts slowly back and forth
 * horizontally (direction controls which way it starts) with a light
 * opacity breathe, so icons placed on opposite sides visually cross each
 * other over a long, slow cycle. Not interactive - no onPress, no
 * accessibility role - so it never competes with real controls. Uses the
 * Animated API with useNativeDriver so the loop runs on the UI thread.
 */
export function FloatingIcon({
  Icon,
  size,
  color,
  top,
  left,
  direction,
  travel = 70,
  durationMs = 9000,
  delayMs = 0,
  baseOpacity = 0.55,
}: FloatingIconProps) {
  const progress = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(progress, {
          toValue: 1,
          duration: durationMs,
          delay: delayMs,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(progress, {
          toValue: 0,
          duration: durationMs,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ]),
    );
    loop.start();
    return () => loop.stop();
  }, [progress, durationMs, delayMs]);

  const sign = direction === 'ltr' ? 1 : -1;
  const translateX = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [-travel * sign, travel * sign],
  });
  const translateY = progress.interpolate({inputRange: [0, 1], outputRange: [0, -6]});
  const opacity = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [baseOpacity, Math.min(baseOpacity + 0.2, 1)],
  });

  return (
    <Animated.View
      pointerEvents="none"
      style={[styles.wrap, {top, left, opacity, transform: [{translateX}, {translateY}]}]}>
      <Icon size={size} color={color} />
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: 'absolute',
  },
});
