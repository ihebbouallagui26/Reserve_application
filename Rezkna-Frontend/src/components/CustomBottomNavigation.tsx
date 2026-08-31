import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {useSafeAreaInsets} from 'react-native-safe-area-context';
import {CompassIcon, MapPinIcon, AccountIcon} from './icons/UiIcons';
import type {UiIconProps} from './icons/UiIcons';
import {spacing, typography, useThemeColors} from '../theme';
import type {MainStackParamList} from '../features/explore/navigation/types';

type Tab = 'explore' | 'map' | 'account';
type Navigation = NativeStackNavigationProp<MainStackParamList>;

interface Item {
  key: Tab;
  label: string;
  route: 'Explore' | 'Map' | 'Account';
  Icon: (props: UiIconProps) => React.ReactElement;
}

const ITEMS: Item[] = [
  {key: 'explore', label: 'Explore', route: 'Explore', Icon: CompassIcon},
  {key: 'map', label: 'Map', route: 'Map', Icon: MapPinIcon},
  {key: 'account', label: 'Account', route: 'Account', Icon: AccountIcon},
];

interface CustomBottomNavigationProps {
  active: Tab;
}

/**
 * Phone-only (see ExploreScreen/MapScreen, which render this conditionally
 * on useDeviceType() === 'phone' - tablet never shows a bottom bar).
 * Explicitly not @react-navigation/bottom-tabs: three Pressables navigating
 * MainNavigator's own stack, nothing more.
 */
export function CustomBottomNavigation({active}: CustomBottomNavigationProps) {
  const navigation = useNavigation<Navigation>();
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();

  return (
    <View
      style={[
        styles.bar,
        {
          backgroundColor: colors.surface,
          borderTopColor: colors.border,
          paddingBottom: Math.max(insets.bottom, spacing.sm),
        },
      ]}>
      {ITEMS.map(item => {
        const isActive = item.key === active;
        const color = isActive ? colors.primary : colors.textSecondary;
        return (
          <Pressable
            key={item.key}
            accessibilityRole="tab"
            accessibilityState={{selected: isActive}}
            accessibilityLabel={item.label}
            hitSlop={8}
            onPress={() => navigation.navigate(item.route)}
            style={styles.item}>
            <item.Icon size={22} color={color} />
            <Text style={[typography.caption, styles.label, {color}]}>{item.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    flexDirection: 'row',
    borderTopWidth: 1,
    paddingTop: spacing.sm,
  },
  item: {
    flex: 1,
    minHeight: 48,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    marginTop: 2,
  },
});
