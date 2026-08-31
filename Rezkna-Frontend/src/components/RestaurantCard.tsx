import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {ShopIcon} from './icons/RestaurantIcons';
import {radius, shadows, spacing, typography, useThemeColors} from '../theme';
import type {RestaurantPublicView} from '../models/restaurant';

interface RestaurantCardProps {
  restaurant: RestaurantPublicView;
  selected?: boolean;
  onPress: (restaurant: RestaurantPublicView) => void;
}

/**
 * The backend never provides a photo (RestaurantPublicView is id/name/
 * address/city only) - the visual banner is a deliberate REZKNA substitute
 * (tinted surface + an existing RestaurantIcons glyph), never a fabricated
 * image. `selected` drives the tablet list<->map sync highlight (Checkpoint 7).
 */
export function RestaurantCard({restaurant, selected = false, onPress}: RestaurantCardProps) {
  const colors = useThemeColors();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`${restaurant.name}, ${restaurant.city}`}
      onPress={() => onPress(restaurant)}
      style={({pressed}) => [
        styles.card,
        shadows.sm,
        {
          backgroundColor: colors.surface,
          borderColor: selected ? colors.primary : colors.border,
          borderWidth: selected ? 2 : 1,
          opacity: pressed ? 0.92 : 1,
        },
      ]}>
      <View style={[styles.visual, {backgroundColor: colors.surfaceAlt}]}>
        <ShopIcon size={30} color={colors.primary} />
      </View>
      <View style={styles.body}>
        <Text style={[typography.h3, {color: colors.textPrimary}]} numberOfLines={1}>
          {restaurant.name}
        </Text>
        <Text style={[typography.caption, styles.line, {color: colors.textSecondary}]} numberOfLines={1}>
          {restaurant.city}
        </Text>
        <Text style={[typography.caption, styles.line, {color: colors.textSecondary}]} numberOfLines={1}>
          {restaurant.address}
        </Text>
        <Text style={[typography.bodyBold, styles.cta, {color: colors.primary}]}>Voir →</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.lg,
    overflow: 'hidden',
  },
  visual: {
    height: 84,
    alignItems: 'center',
    justifyContent: 'center',
  },
  body: {
    padding: spacing.md,
  },
  line: {
    marginTop: 2,
  },
  cta: {
    marginTop: spacing.sm,
    textAlign: 'right',
  },
});
