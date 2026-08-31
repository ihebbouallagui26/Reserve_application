import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {FlatList, Pressable, StyleSheet, Text, View} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {SearchBar} from '../../../components/SearchBar';
import {RestaurantList} from '../../../components/RestaurantList';
import {LoadingState} from '../../../components/LoadingState';
import {EmptyState} from '../../../components/EmptyState';
import {ErrorState} from '../../../components/ErrorState';
import {CustomBottomNavigation} from '../../../components/CustomBottomNavigation';
import {RestaurantMap} from '../../../components/RestaurantMap';
import {AccountIcon} from '../../../components/icons/UiIcons';
import {restaurantApi} from '../../../services/api/restaurantApi';
import {getCurrentPosition} from '../../../services/locationService';
import {getErrorMessage} from '../../../models/errors';
import {radius, spacing, typography, useResponsiveLayout, useThemeColors} from '../../../theme';
import type {RestaurantPublicView} from '../../../models/restaurant';
import type {MainStackParamList} from '../navigation/types';

type Status = 'loading' | 'success' | 'empty' | 'error';
type Navigation = NativeStackNavigationProp<MainStackParamList>;

/** Tablet gives the map slightly more room in landscape than portrait -
 * the list stays readable at 40% just as well as 45%, and the extra width
 * benefits the map's sense of place more than an already-comfortable list. */
const LIST_RATIO_PORTRAIT = 0.45;
const LIST_RATIO_LANDSCAPE = 0.4;

/** Matches the old backend's own default nearby-search radius
 * (DiscoveryController.nearby, @RequestParam(defaultValue = "10")) - a real
 * historical precedent for "a reasonable walking/driving distance", not an
 * arbitrary guess, and well under the backend's 100km hard limit. */
const DEFAULT_SEARCH_RADIUS_KM = 10;

/**
 * Single device-adaptive screen (not two separate files): phone renders a
 * vertical list under a light header with a bottom tab bar; tablet renders
 * a persistent header (logo / search / account) over a list+map split view,
 * with no bottom bar at all. The data-fetching/search/selection logic is
 * shared between both compositions.
 */
export function ExploreScreen() {
  const colors = useThemeColors();
  const navigation = useNavigation<Navigation>();
  const {deviceType, orientation} = useResponsiveLayout();
  const listRef = useRef<FlatList<RestaurantPublicView>>(null);

  const [status, setStatus] = useState<Status>('loading');
  const [restaurants, setRestaurants] = useState<RestaurantPublicView[]>([]);
  const [usedLocation, setUsedLocation] = useState(false);
  const [query, setQuery] = useState('');
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  /**
   * Tries the diner's own position first (one-shot, never watchPosition -
   * see locationService); denied/unavailable/timeout all fall back to the
   * plain unfiltered list rather than blocking Explore. This runs once per
   * `load()` call, never in a loop, and only from this single effect below.
   */
  const load = useCallback(async () => {
    setStatus('loading');
    setErrorMessage(null);
    try {
      const position = await getCurrentPosition();
      const list =
        position.status === 'success'
          ? await restaurantApi.search({
              lat: position.latitude,
              lng: position.longitude,
              radiusKm: DEFAULT_SEARCH_RADIUS_KM,
            })
          : await restaurantApi.list();
      setUsedLocation(position.status === 'success');
      setRestaurants(list);
      setStatus(list.length === 0 ? 'empty' : 'success');
    } catch (err) {
      setErrorMessage(getErrorMessage(err));
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) {
      return restaurants;
    }
    return restaurants.filter(
      r =>
        r.name.toLowerCase().includes(q) ||
        r.city.toLowerCase().includes(q) ||
        r.address.toLowerCase().includes(q),
    );
  }, [restaurants, query]);

  const openDetail = useCallback(
    (restaurantId: string) => {
      navigation.navigate('RestaurantDetail', {restaurantId});
    },
    [navigation],
  );

  /**
   * Phone has no map to sync, so a tap opens the fiche immediately. Tablet
   * keeps the split view alive: a first tap only selects (highlighting the
   * card and, from Checkpoint 8, its map marker) - the fiche opens on a
   * second tap of an already-selected card, or later a marker tap could
   * select the same way a card tap does, driving the same state.
   */
  const handleCardPress = useCallback(
    (restaurant: RestaurantPublicView) => {
      if (deviceType === 'phone') {
        openDetail(restaurant.id);
        return;
      }
      if (selectedId === restaurant.id) {
        openDetail(restaurant.id);
      } else {
        setSelectedId(restaurant.id);
      }
    },
    [deviceType, openDetail, selectedId],
  );

  // Scrolls the list to a selection made from outside it (the future map
  // marker tap, Checkpoint 8) - a tap on the list itself is already in view.
  useEffect(() => {
    if (deviceType !== 'tablet' || !selectedId) {
      return;
    }
    const index = filtered.findIndex(r => r.id === selectedId);
    if (index >= 0) {
      listRef.current?.scrollToIndex({index, animated: true, viewPosition: 0.5});
    }
  }, [selectedId, filtered, deviceType]);

  const listBody =
    status === 'loading' ? (
      <LoadingState />
    ) : status === 'error' ? (
      <ErrorState message={errorMessage ?? 'Une erreur est survenue.'} onRetry={load} />
    ) : status === 'empty' ? (
      <EmptyState
        message={
          usedLocation
            ? 'Nous n\'avons trouvé aucun restaurant à proximité.'
            : "Nous n'avons trouvé aucun restaurant."
        }
      />
    ) : filtered.length === 0 ? (
      <EmptyState message="Aucun résultat pour cette recherche." />
    ) : (
      <RestaurantList
        ref={listRef}
        restaurants={filtered}
        selectedRestaurantId={selectedId}
        onSelectRestaurant={handleCardPress}
        contentContainerStyle={styles.listContent}
      />
    );

  if (deviceType === 'tablet') {
    const listFlex = orientation === 'landscape' ? LIST_RATIO_LANDSCAPE : LIST_RATIO_PORTRAIT;
    return (
      <ScreenContainer>
        <View style={[styles.tabletHeader, {borderBottomColor: colors.border}]}>
          <Text style={[typography.h3, {color: colors.primary}]}>REZKNA</Text>
          <View style={styles.tabletSearch}>
            <SearchBar value={query} onChangeText={setQuery} />
          </View>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Account"
            hitSlop={8}
            onPress={() => navigation.navigate('Account')}
            style={styles.tabletAccount}>
            <AccountIcon size={22} color={colors.textPrimary} />
          </Pressable>
        </View>

        <View style={styles.tabletBody}>
          <View style={[styles.tabletColumn, {flex: listFlex}]}>{listBody}</View>
          <View style={[styles.tabletMap, {flex: 1 - listFlex, backgroundColor: colors.surfaceAlt}]}>
            <RestaurantMap
              restaurants={filtered}
              selectedRestaurantId={selectedId}
              onSelectRestaurant={setSelectedId}
              onOpenRestaurant={openDetail}
            />
          </View>
        </View>
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <View style={styles.body}>
        <View style={styles.header}>
          <Text style={[typography.h2, {color: colors.textPrimary}]}>Explore</Text>
        </View>
        <View style={styles.searchWrap}>
          <SearchBar value={query} onChangeText={setQuery} />
        </View>
        {listBody}
      </View>
      <CustomBottomNavigation active="explore" />
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
  },
  searchWrap: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
    paddingBottom: spacing.sm,
  },
  listContent: {
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xl,
  },
  tabletHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
  },
  tabletSearch: {
    flex: 1,
    marginHorizontal: spacing.xl,
    maxWidth: 480,
    alignSelf: 'center',
  },
  tabletAccount: {
    width: 40,
    height: 40,
    borderRadius: radius.pill,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabletBody: {
    flex: 1,
    flexDirection: 'row',
  },
  tabletColumn: {
    paddingTop: spacing.md,
  },
  tabletMap: {
    justifyContent: 'center',
  },
});
