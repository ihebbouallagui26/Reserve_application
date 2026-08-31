import React, {useCallback, useEffect, useState} from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {BackButton} from '../../../components/BackButton';
import {Button} from '../../../components/Button';
import {LoadingState} from '../../../components/LoadingState';
import {ErrorState} from '../../../components/ErrorState';
import {ChefHatIcon} from '../../../components/icons/RestaurantIcons';
import {restaurantApi} from '../../../services/api/restaurantApi';
import {isApiError, getErrorMessage} from '../../../models/errors';
import {layout, radius, spacing, typography, useThemeColors} from '../../../theme';
import type {RestaurantPublicView} from '../../../models/restaurant';
import type {MainStackParamList} from '../navigation/types';

type Status = 'loading' | 'success' | 'notFound' | 'error';
type Props = NativeStackScreenProps<MainStackParamList, 'RestaurantDetail'>;

/**
 * Only backend-provided fields are shown (name/address/city) - no rating,
 * price range, hours, phone or photo, none of which RestaurantPublicView
 * returns. Pushed from Explore, so a real previous screen always exists -
 * BackButton renders unconditionally here (see its own canGoBack() guard).
 */
export function RestaurantDetailScreen({route, navigation}: Props) {
  const {restaurantId} = route.params;
  const colors = useThemeColors();
  const [status, setStatus] = useState<Status>('loading');
  const [restaurant, setRestaurant] = useState<RestaurantPublicView | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setErrorMessage(null);
    try {
      const data = await restaurantApi.getById(restaurantId);
      setRestaurant(data);
      setStatus('success');
    } catch (err) {
      if (isApiError(err) && err.kind === 'notFound') {
        setStatus('notFound');
      } else {
        setErrorMessage(getErrorMessage(err));
        setStatus('error');
      }
    }
  }, [restaurantId]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <ScreenContainer>
      <BackButton color={colors.textPrimary} background={colors.surfaceAlt} />

      {status === 'loading' ? <LoadingState /> : null}

      {status === 'notFound' ? <ErrorState message="Ce restaurant est introuvable." /> : null}

      {status === 'error' ? (
        <ErrorState message={errorMessage ?? 'Une erreur est survenue.'} onRetry={load} />
      ) : null}

      {status === 'success' && restaurant ? (
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <View style={styles.content}>
            <View style={[styles.hero, {backgroundColor: colors.surfaceAlt}]}>
              <ChefHatIcon size={56} color={colors.primary} />
            </View>

            <Text style={[typography.h1, {color: colors.textPrimary}]}>{restaurant.name}</Text>
            <Text style={[typography.body, styles.line, {color: colors.textSecondary}]}>
              {restaurant.city}
            </Text>
            <Text style={[typography.body, styles.line, {color: colors.textSecondary}]}>
              {restaurant.address}
            </Text>

            <Button
              label="Voir le menu"
              onPress={() => navigation.navigate('RestaurantMenu', {restaurantId})}
              style={styles.cta}
            />
          </View>
        </ScrollView>
      ) : null}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  scrollContent: {
    paddingTop: spacing.xxl,
    paddingBottom: spacing.xl,
  },
  content: {
    width: '100%',
    maxWidth: layout.detailMaxWidth,
    alignSelf: 'center',
    paddingHorizontal: spacing.lg,
  },
  hero: {
    height: 160,
    borderRadius: radius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.lg,
  },
  line: {
    marginTop: spacing.xs,
  },
  cta: {
    marginTop: spacing.xl,
    alignSelf: 'flex-start',
    paddingHorizontal: spacing.xl,
  },
});
