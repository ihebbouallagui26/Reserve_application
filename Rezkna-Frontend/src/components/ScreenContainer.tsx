import React from 'react';
import {StyleSheet} from 'react-native';
import type {ViewStyle} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';
import {useThemeColors} from '../theme';

interface ScreenContainerProps {
  children: React.ReactNode;
  style?: ViewStyle;
}

/** Base wrapper every screen should use: theme-aware background + safe area insets. */
export function ScreenContainer({children, style}: ScreenContainerProps) {
  const colors = useThemeColors();
  return (
    <SafeAreaView style={[styles.container, {backgroundColor: colors.background}, style]}>
      {children}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
