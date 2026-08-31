import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {spacing, typography, useThemeColors} from '../theme';
import type {MenuItem} from '../models/menu';

interface MenuItemCardProps {
  item: MenuItem;
}

/** No currency symbol: the backend's `price` carries no currency field, so
 * inventing one (€/TND/$) would be presenting data the contract never gave. */
function formatPrice(price: number): string {
  return price.toFixed(2);
}

/** `available: false` items are dimmed and labelled, never hidden - the
 * Partner marked them unavailable, not deleted them. */
export function MenuItemCard({item}: MenuItemCardProps) {
  const colors = useThemeColors();
  const unavailable = !item.available;
  const textColor = unavailable ? colors.textSecondary : colors.textPrimary;

  return (
    <View style={[styles.row, unavailable && styles.unavailable]}>
      <View style={styles.info}>
        <Text style={[typography.bodyBold, {color: textColor}]}>
          {item.name}
          {unavailable ? ' · Indisponible' : ''}
        </Text>
        {item.description ? (
          <Text style={[typography.caption, styles.description, {color: colors.textSecondary}]}>
            {item.description}
          </Text>
        ) : null}
      </View>
      <Text style={[typography.bodyBold, {color: unavailable ? colors.textSecondary : colors.primary}]}>
        {formatPrice(item.price)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingVertical: spacing.sm,
  },
  unavailable: {
    opacity: 0.5,
  },
  info: {
    flex: 1,
    marginRight: spacing.md,
  },
  description: {
    marginTop: 2,
  },
});
