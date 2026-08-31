import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {useSession} from '../session/SessionProvider';
import {MainNavigator} from './MainNavigator';
import {PartnerNavigator} from '../features/partner/navigation/PartnerNavigator';
import type {RootStackParamList} from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();

/**
 * A partner session can only ever reach Partner; a diner session and no
 * session at all both reach Main, since Explore (inside MainNavigator) must
 * be public - AccountEntryScreen is what actually distinguishes guest from
 * diner within that branch. Enforced structurally by only ever mounting one
 * Stack.Screen at a time based on SessionProvider's status.
 */
export function RootNavigator() {
  const {status} = useSession();

  if (status === 'loading') {
    // Phase 1/2: no splash screen yet - added in Phase 7 (UI/UX polish).
    return null;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{headerShown: false}}>
        {status === 'partner' ? (
          <Stack.Screen name="Partner" component={PartnerNavigator} />
        ) : (
          <Stack.Screen name="Main" component={MainNavigator} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
