import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {MenuItemCard} from './MenuItemCard';
import {spacing, typography, useThemeColors} from '../theme';
import type {MenuItem} from '../models/menu';

interface CategorySectionProps {
  title: string;
  items: MenuItem[];
}

export function CategorySection({title, items}: CategorySectionProps) {
  const colors = useThemeColors();
  return (
    <View style={styles.section}>
      <Text style={[typography.h3, {color: colors.textPrimary}]}>{title}</Text>
      <View style={[styles.divider, {backgroundColor: colors.border}]} />
      {items.map(item => (
        <MenuItemCard key={item.id} item={item} />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    marginBottom: spacing.xl,
  },
  divider: {
    height: 1,
    marginTop: spacing.xs,
    marginBottom: spacing.sm,
  },
});
