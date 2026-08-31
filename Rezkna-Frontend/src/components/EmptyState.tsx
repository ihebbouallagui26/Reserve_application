import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {ShopIcon} from './icons/RestaurantIcons';
import {Button} from './Button';
import {spacing, typography, useThemeColors} from '../theme';

interface EmptyStateProps {
  message: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({message, actionLabel, onAction}: EmptyStateProps) {
  const colors = useThemeColors();
  return (
    <View style={styles.container}>
      <ShopIcon size={40} color={colors.textSecondary} />
      <Text style={[typography.body, styles.message, {color: colors.textSecondary}]}>{message}</Text>
      {actionLabel && onAction ? (
        <Button label={actionLabel} onPress={onAction} variant="secondary" style={styles.action} />
      ) : null}
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
  message: {
    marginTop: spacing.md,
    textAlign: 'center',
  },
  action: {
    marginTop: spacing.lg,
  },
});
