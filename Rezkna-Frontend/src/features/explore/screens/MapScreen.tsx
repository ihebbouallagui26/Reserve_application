import React, {useCallback, useEffect, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {CustomBottomNavigation} from '../../../components/CustomBottomNavigation';
import {RestaurantMap} from '../../../components/RestaurantMap';
import {LoadingState} from '../../../components/LoadingState';
import {ErrorState} from '../../../components/ErrorState';
import {restaurantApi} from '../../../services/api/restaurantApi';
import {getErrorMessage} from '../../../models/errors';
import {spacing, typography, useDeviceType, useThemeColors} from '../../../theme';
import type {RestaurantPublicView} from '../../../models/restaurant';
import type {MainStackParamList} from '../navigation/types';

type Status = 'loading' | 'success' | 'error';
type Navigation = NativeStackNavigationProp<MainStackParamList>;

/**
 * Phone-only destination (tablet's map lives inline in ExploreScreen's split
 * view - tablet never routes here). Fetches its own restaurant list
 * independently of ExploreScreen, matching the rest of this app's per-screen
 * data ownership (no shared cache/context exists, e.g. ProfileScreen also
 * refetches on its own).
 */
export function MapScreen() {
  const colors = useThemeColors();
  const navigation = useNavigation<Navigation>();
  const deviceType = useDeviceType();
  const [status, setStatus] = useState<Status>('loading');
  const [restaurants, setRestaurants] = useState<RestaurantPublicView[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setErrorMessage(null);
    try {
      const list = await restaurantApi.list();
      setRestaurants(list);
      setStatus('success');
    } catch (err) {
      setErrorMessage(getErrorMessage(err));
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const openDetail = useCallback(
    (restaurantId: string) => {
      navigation.navigate('RestaurantDetail', {restaurantId});
    },
    [navigation],
  );

  return (
    <ScreenContainer>
      <View style={styles.body}>
        <View style={styles.header}>
          <Text style={[typography.h2, {color: colors.textPrimary}]}>Carte</Text>
        </View>

        {status === 'loading' ? <LoadingState /> : null}
        {status === 'error' ? (
          <ErrorState message={errorMessage ?? 'Une erreur est survenue.'} onRetry={load} />
        ) : null}
        {status === 'success' ? (
          <RestaurantMap
            restaurants={restaurants}
            selectedRestaurantId={selectedId}
            onSelectRestaurant={setSelectedId}
            onOpenRestaurant={openDetail}
          />
        ) : null}
      </View>
      {deviceType === 'phone' ? <CustomBottomNavigation active="map" /> : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  body: {
    flex: 1,
  },
  header: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
    paddingBottom: spacing.sm,
  },
});
