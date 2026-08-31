import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {ExploreScreen} from '../features/explore/screens/ExploreScreen';
import {MapScreen} from '../features/explore/screens/MapScreen';
import {RestaurantDetailScreen} from '../features/explore/screens/RestaurantDetailScreen';
import {RestaurantMenuScreen} from '../features/explore/screens/RestaurantMenuScreen';
import {AccountEntryScreen} from '../features/main/screens/AccountEntryScreen';
import type {MainStackParamList} from '../features/explore/navigation/types';

const Stack = createNativeStackNavigator<MainStackParamList>();

/**
 * Public + diner shell, mounted by RootNavigator for both 'none' and 'diner'
 * session status - Explore is reachable with no session at all. This
 * navigator never inspects session status itself; only AccountEntryScreen
 * does, to choose between the guest sign-in flow and the diner's own
 * profile stack.
 */
export function MainNavigator() {
  return (
    <Stack.Navigator initialRouteName="Explore" screenOptions={{headerShown: false}}>
      <Stack.Screen name="Explore" component={ExploreScreen} />
      <Stack.Screen name="Map" component={MapScreen} />
      <Stack.Screen name="Account" component={AccountEntryScreen} />
      <Stack.Screen name="RestaurantDetail" component={RestaurantDetailScreen} />
      <Stack.Screen name="RestaurantMenu" component={RestaurantMenuScreen} />
    </Stack.Navigator>
  );
}
