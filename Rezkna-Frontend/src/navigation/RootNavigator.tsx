import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {useSession} from '../session/SessionProvider';
import {AuthNavigator} from '../features/auth/navigation/AuthNavigator';
import {DinerNavigator} from '../features/diner/navigation/DinerNavigator';
import {PartnerNavigator} from '../features/partner/navigation/PartnerNavigator';
import type {RootStackParamList} from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();

/**
 * A diner session can only ever reach the Diner branch, a partner session
 * only Partner, and no session only Auth - enforced structurally by only
 * ever mounting one Stack.Screen at a time based on SessionProvider's
 * status, which itself guarantees diner/partner are mutually exclusive.
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
        {status === 'diner' ? (
          <Stack.Screen name="Diner" component={DinerNavigator} />
        ) : status === 'partner' ? (
          <Stack.Screen name="Partner" component={PartnerNavigator} />
        ) : (
          <Stack.Screen name="Auth" component={AuthNavigator} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
