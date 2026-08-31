import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {ScrollView, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {BackButton} from '../../../components/BackButton';
import {LoadingState} from '../../../components/LoadingState';
import {EmptyState} from '../../../components/EmptyState';
import {ErrorState} from '../../../components/ErrorState';
import {CategorySection} from '../../../components/CategorySection';
import {menuApi} from '../../../services/api/menuApi';
import {isApiError, getErrorMessage} from '../../../models/errors';
import {layout, spacing, typography, useThemeColors} from '../../../theme';
import type {MenuItem} from '../../../models/menu';
import type {MainStackParamList} from '../navigation/types';

type Status = 'loading' | 'success' | 'empty' | 'notFound' | 'error';
type Props = NativeStackScreenProps<MainStackParamList, 'RestaurantMenu'>;

const OTHER_CATEGORY = 'Autres';

/** Groups the flat item list by category (the backend never nests items
 * under a category structure - see Phase 9). Items with no category, or a
 * blank one, fall under a single generic "Autres" bucket - never a
 * fabricated business category. Order of first appearance is preserved. */
function groupByCategory(items: MenuItem[]): Array<[string, MenuItem[]]> {
  const groups = new Map<string, MenuItem[]>();
  for (const item of items) {
    const key = item.category && item.category.trim() ? item.category : OTHER_CATEGORY;
    const bucket = groups.get(key);
    if (bucket) {
      bucket.push(item);
    } else {
      groups.set(key, [item]);
    }
  }
  return Array.from(groups.entries());
}

/**
 * No ordering/cart/payment logic - display only, per Phase 11 scope. A menu
 * document that exists but has zero items and a restaurant with no menu
 * document at all are indistinguishable here (both come back as
 * items: [] from menuApi, see Phase 9 §6) - both correctly show the same
 * empty state, not an error.
 */
export function RestaurantMenuScreen({route}: Props) {
  const {restaurantId} = route.params;
  const colors = useThemeColors();
  const [status, setStatus] = useState<Status>('loading');
  const [items, setItems] = useState<MenuItem[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setStatus('loading');
    setErrorMessage(null);
    try {
      const menu = await menuApi.getByRestaurantId(restaurantId);
      setItems(menu.items);
      setStatus(menu.items.length === 0 ? 'empty' : 'success');
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

  const categories = useMemo(() => groupByCategory(items), [items]);

  return (
    <ScreenContainer>
      <BackButton color={colors.textPrimary} background={colors.surfaceAlt} />

      {status === 'loading' ? <LoadingState /> : null}

      {status === 'notFound' ? <ErrorState message="Ce restaurant est introuvable." /> : null}

      {status === 'error' ? (
        <ErrorState message={errorMessage ?? 'Une erreur est survenue.'} onRetry={load} />
      ) : null}

      {status === 'empty' ? (
        <EmptyState message="Le menu de ce restaurant est actuellement vide." />
      ) : null}

      {status === 'success' ? (
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <View style={styles.content}>
            <Text style={[typography.h1, styles.title, {color: colors.textPrimary}]}>Menu</Text>
            {categories.map(([category, categoryItems]) => (
              <CategorySection key={category} title={category} items={categoryItems} />
            ))}
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
  title: {
    marginBottom: spacing.lg,
  },
});
