import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {useThemeColors} from '../../../theme';
import {ProfileScreen} from '../screens/ProfileScreen';
import {EditProfileScreen} from '../screens/EditProfileScreen';
import {NotificationsScreen} from '../screens/NotificationsScreen';
import type {DinerStackParamList} from './types';

const Stack = createNativeStackNavigator<DinerStackParamList>();

export function DinerNavigator() {
  const colors = useThemeColors();
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: {backgroundColor: colors.surface},
        headerTintColor: colors.textPrimary,
        headerShadowVisible: false,
      }}>
      <Stack.Screen name="Profile" component={ProfileScreen} options={{title: 'Profil'}} />
      <Stack.Screen name="EditProfile" component={EditProfileScreen} options={{title: 'Modifier le profil'}} />
      <Stack.Screen name="Notifications" component={NotificationsScreen} options={{title: 'Notifications'}} />
    </Stack.Navigator>
  );
}
