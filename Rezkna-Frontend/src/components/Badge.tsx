import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {radius, spacing, typography, useThemeColors} from '../theme';

interface BadgeProps {
  label: string;
  tone?: 'primary' | 'neutral';
}

export function Badge({label, tone = 'neutral'}: BadgeProps) {
  const colors = useThemeColors();
  const isPrimary = tone === 'primary';
  return (
    <View
      style={[
        styles.badge,
        {
          backgroundColor: isPrimary ? colors.primary : colors.surfaceAlt,
          borderColor: isPrimary ? colors.primary : colors.border,
        },
      ]}>
      <Text style={[typography.caption, {color: isPrimary ? colors.onPrimary : colors.textSecondary}]}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderRadius: radius.pill,
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
  },
});
