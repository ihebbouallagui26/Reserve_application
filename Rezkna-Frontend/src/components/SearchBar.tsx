import React from 'react';
import {StyleSheet, TextInput, View} from 'react-native';
import {SearchIcon} from './icons/UiIcons';
import {radius, spacing, typography, useThemeColors} from '../theme';

interface SearchBarProps {
  value: string;
  onChangeText: (value: string) => void;
  placeholder?: string;
}

/** Pill-shaped, icon-prefixed - visually distinct from TextField's labelled
 * form-field style, matching a discovery app's search affordance rather than
 * a form input. Filters the already-loaded restaurant list locally; there is
 * no backend text-search endpoint (see restaurantApi.search, which is
 * geo-only) so this never calls the network. */
export function SearchBar({value, onChangeText, placeholder = 'Rechercher un restaurant...'}: SearchBarProps) {
  const colors = useThemeColors();
  return (
    <View
      style={[styles.container, {backgroundColor: colors.surfaceAlt, borderColor: colors.border}]}
      accessibilityRole="search">
      <SearchIcon size={18} color={colors.textSecondary} />
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textSecondary}
        autoCapitalize="none"
        autoCorrect={false}
        style={[styles.input, {color: colors.textPrimary}]}
        accessibilityLabel="Rechercher un restaurant"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 44,
    borderWidth: 1,
    borderRadius: radius.pill,
    paddingHorizontal: spacing.md,
  },
  input: {
    flex: 1,
    marginLeft: spacing.sm,
    fontSize: typography.body.fontSize,
    padding: 0,
  },
});
