import React from 'react';
import {ActivityIndicator, StyleSheet, View} from 'react-native';
import {spacing, useThemeColors} from '../theme';

interface LoadingStateProps {
  label?: string;
}

export function LoadingState({label = 'Chargement...'}: LoadingStateProps) {
  const colors = useThemeColors();
  return (
    <View
      style={styles.container}
      accessibilityRole="progressbar"
      accessibilityLabel={label}>
      <ActivityIndicator color={colors.primary} size="large" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.xl,
  },
});
