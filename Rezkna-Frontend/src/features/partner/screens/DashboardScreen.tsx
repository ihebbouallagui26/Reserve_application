import React, {useCallback, useEffect, useState} from 'react';
import {ActivityIndicator, ScrollView, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {Button} from '../../../components/Button';
import {Badge} from '../../../components/Badge';
import {layout, radius, spacing, typography, useThemeColors} from '../../../theme';
import {partnerApi} from '../../../services/api/partnerApi';
import {useSession} from '../../../session/SessionProvider';
import {usePartnerAuthErrorHandler} from '../../../session/usePartnerAuthErrorHandler';
import type {PartnerMeResponse} from '../../../models/partner';
import type {PartnerStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<PartnerStackParamList, 'Dashboard'>;

/** Display-only French label for a partner role - the underlying value (used
 * for permission checks) is never translated, only what the user reads. */
function roleLabel(role: string): string {
  return role === 'OWNER' ? 'Propriétaire' : role === 'HOST' ? 'Hôte' : role;
}

/**
 * Deliberately minimal: identity-service only ever returns {user,
 * propertyId} for a partner - no Property object, no bookings, no covers,
 * no menu, no reports. Nothing here is invented to "fill space"; a real
 * dashboard is a later sprint's job once restaurant-service exists.
 */
export function DashboardScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {partnerSession, signOutPartner} = useSession();
  const handleAuthError = usePartnerAuthErrorHandler();
  const [me, setMe] = useState<PartnerMeResponse | null>(
    partnerSession ? {user: partnerSession.user, propertyId: partnerSession.propertyId} : null,
  );
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setError(null);
    setRefreshing(true);
    try {
      const fresh = await partnerApi.getMe();
      setMe(fresh);
    } catch (err) {
      setError(handleAuthError(err));
    } finally {
      setRefreshing(false);
    }
  }, [handleAuthError]);

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!me) {
    return (
      <ScreenContainer style={styles.centered}>
        <ActivityIndicator color={colors.primary} />
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <ScrollView contentContainerStyle={styles.content}>
        {refreshing ? (
          <Text style={[typography.caption, {color: colors.textSecondary, marginBottom: spacing.xs}]}>
            Actualisation...
          </Text>
        ) : null}
        {error ? (
          <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{error}</Text>
        ) : null}

        <View style={[styles.card, {backgroundColor: colors.surfaceAlt, borderColor: colors.border}]}>
          <Text style={[typography.h2, {color: colors.textPrimary}]}>{me.user.name}</Text>
          <Text style={[typography.caption, styles.email, {color: colors.textSecondary}]}>{me.user.email}</Text>
          <View style={styles.badgeRow}>
            <Badge label={roleLabel(me.user.role)} tone={me.user.role === 'OWNER' ? 'primary' : 'neutral'} />
          </View>
          <View style={styles.propertyRow}>
            <Text style={[typography.caption, {color: colors.textSecondary}]}>Identifiant de l'établissement</Text>
            <Text style={[typography.body, {color: colors.textPrimary}]}>{me.propertyId}</Text>
          </View>
        </View>

        <Button label="Équipe" onPress={() => navigation.navigate('Staff')} style={styles.action} />
        <Button label="Se déconnecter" onPress={signOutPartner} variant="secondary" style={styles.action} />
      </ScrollView>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  centered: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    width: '100%',
    maxWidth: layout.contentMaxWidth,
    alignSelf: 'center',
    padding: spacing.lg,
  },
  card: {
    borderWidth: 1,
    borderRadius: radius.lg,
    padding: spacing.md,
  },
  email: {
    marginTop: 2,
  },
  badgeRow: {
    marginTop: spacing.sm,
  },
  propertyRow: {
    marginTop: spacing.md,
  },
  action: {
    marginTop: spacing.md,
  },
});
