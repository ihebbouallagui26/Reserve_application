import React from 'react';
import {Pressable, StyleSheet} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import Svg, {Path} from 'react-native-svg';

interface BackButtonProps {
  /** Arrow stroke color. Defaults to white, for use on the blue header. */
  color?: string;
  /** Circular pill background behind the arrow. Pass 'transparent' on a
   * light/white screen where the pill isn't needed. */
  background?: string;
}

/**
 * Circular back-chevron button for screens using a custom (headerShown:false)
 * shell - the auth flow doesn't get React Navigation's automatic native
 * back button, so this fills that gap. Renders nothing when there is no
 * previous screen to go back to (e.g. Welcome, or Landing right after
 * Welcome replaces itself), so it never appears as a dead control.
 */
export function BackButton({color = '#ffffff', background = 'rgba(255,255,255,0.18)'}: BackButtonProps) {
  const navigation = useNavigation();

  if (!navigation.canGoBack()) {
    return null;
  }

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel="Retour"
      hitSlop={12}
      onPress={() => navigation.goBack()}
      style={({pressed}) => [styles.button, {backgroundColor: background, opacity: pressed ? 0.6 : 1}]}>
      <Svg width={20} height={20} viewBox="0 0 24 24">
        <Path
          d="M15 5l-7 7 7 7"
          stroke={color}
          fill="none"
          strokeWidth={2.2}
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </Svg>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    position: 'absolute',
    top: 16,
    left: 16,
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
});
