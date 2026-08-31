import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {Button} from './Button';
import {spacing, typography, useThemeColors} from '../theme';

interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({message, onRetry}: ErrorStateProps) {
  const colors = useThemeColors();
  return (
    <View style={styles.container}>
      <Text style={[typography.body, styles.message, {color: colors.error}]}>{message}</Text>
      {onRetry ? <Button label="Réessayer" onPress={onRetry} style={styles.action} /> : null}
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
    textAlign: 'center',
  },
  action: {
    marginTop: spacing.lg,
  },
});
