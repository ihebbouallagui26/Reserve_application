import React, {forwardRef} from 'react';
import {FlatList, StyleSheet, View} from 'react-native';
import type {StyleProp, ViewStyle} from 'react-native';
import {RestaurantCard} from './RestaurantCard';
import {spacing} from '../theme';
import type {RestaurantPublicView} from '../models/restaurant';

interface RestaurantListProps {
  restaurants: RestaurantPublicView[];
  selectedRestaurantId?: string | null;
  onSelectRestaurant: (restaurant: RestaurantPublicView) => void;
  contentContainerStyle?: StyleProp<ViewStyle>;
}

function Separator() {
  return <View style={styles.separator} />;
}

/** Thin FlatList wrapper, reused as-is for the phone full-width list and the
 * tablet split-view's left column (Checkpoint 7) - the ref is forwarded so a
 * parent can scroll to the selected restaurant when the map selection drives
 * it, without this component needing to know about the map at all. */
export const RestaurantList = forwardRef<FlatList<RestaurantPublicView>, RestaurantListProps>(
  function RestaurantListInner(
    {restaurants, selectedRestaurantId, onSelectRestaurant, contentContainerStyle},
    ref,
  ) {
    return (
      <FlatList
        ref={ref}
        data={restaurants}
        keyExtractor={item => item.id}
        renderItem={({item}) => (
          <RestaurantCard
            restaurant={item}
            selected={item.id === selectedRestaurantId}
            onPress={onSelectRestaurant}
          />
        )}
        ItemSeparatorComponent={Separator}
        contentContainerStyle={contentContainerStyle}
        // scrollToIndex (tablet selection sync, driven from outside taps on
        // this list) can target a row not yet measured - the default FlatList
        // behavior throws in that case; a no-op handler just skips the scroll
        // instead of crashing the screen.
        onScrollToIndexFailed={() => {}}
      />
    );
  },
);

const styles = StyleSheet.create({
  separator: {
    height: spacing.md,
  },
});
