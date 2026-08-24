import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {useThemeColors} from '../../../theme';
import {DashboardScreen} from '../screens/DashboardScreen';
import {StaffScreen} from '../screens/StaffScreen';
import {AddStaffScreen} from '../screens/AddStaffScreen';
import type {PartnerStackParamList} from './types';

const Stack = createNativeStackNavigator<PartnerStackParamList>();

export function PartnerNavigator() {
  const colors = useThemeColors();
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: {backgroundColor: colors.surface},
        headerTintColor: colors.textPrimary,
        headerShadowVisible: false,
      }}>
      <Stack.Screen name="Dashboard" component={DashboardScreen} options={{title: 'Tableau de bord'}} />
      <Stack.Screen name="Staff" component={StaffScreen} options={{title: 'Équipe'}} />
      <Stack.Screen name="AddStaff" component={AddStaffScreen} options={{title: 'Ajouter un membre'}} />
    </Stack.Navigator>
  );
}
