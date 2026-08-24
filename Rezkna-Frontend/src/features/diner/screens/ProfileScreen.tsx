import React, {useCallback, useEffect, useState} from 'react';
import {ActivityIndicator, ScrollView, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {Button} from '../../../components/Button';
import {layout, radius, spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {useSession} from '../../../session/SessionProvider';
import {useDinerAuthErrorHandler} from '../../../session/useDinerAuthErrorHandler';
import type {DinerAccount} from '../../../models/diner';
import type {DinerStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<DinerStackParamList, 'Profile'>;

function InfoRow({label, value}: {label: string; value: string}) {
  const colors = useThemeColors();
  return (
    <View style={styles.row}>
      <Text style={[typography.caption, {color: colors.textSecondary}]}>{label}</Text>
      <Text style={[typography.body, styles.rowValue, {color: colors.textPrimary}]}>{value}</Text>
    </View>
  );
}

/**
 * Acts as the diner "home" screen. Renders instantly from the cached session
 * account (always available - a diner session always carries one), then
 * quietly revalidates via GET /me in the background rather than blocking on
 * a spinner for data we already have.
 */
export function ProfileScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {dinerSession, signOutDiner, updateDinerAccount} = useSession();
  const handleAuthError = useDinerAuthErrorHandler();
  const [account, setAccount] = useState<DinerAccount | null>(dinerSession?.account ?? null);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setError(null);
    setRefreshing(true);
    try {
      const fresh = await identityApi.getMe();
      setAccount(fresh);
      await updateDinerAccount(fresh);
    } catch (err) {
      setError(handleAuthError(err));
    } finally {
      setRefreshing(false);
    }
  }, [handleAuthError, updateDinerAccount]);

  useEffect(() => {
    refresh();
    // Only on mount - EditProfile updates the session directly on save,
    // there is no need to refetch every time this screen regains focus.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // React Navigation keeps this screen mounted when navigating to EditProfile
  // and back, so the useState initializer above never re-runs. Resync local
  // display state whenever the session's account changes (e.g. after saving
  // an edit), otherwise the screen keeps showing pre-edit data until the next
  // full mount.
  useEffect(() => {
    if (dinerSession?.account) {
      setAccount(dinerSession.account);
    }
  }, [dinerSession?.account]);

  if (!account) {
    return (
      <ScreenContainer style={styles.centered}>
        <ActivityIndicator color={colors.primary} />
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={[typography.h2, {color: colors.textPrimary}]}>
          {account.name || 'Votre profil'}
        </Text>
        {refreshing ? (
          <Text style={[typography.caption, {color: colors.textSecondary, marginTop: spacing.xs}]}>
            Actualisation...
          </Text>
        ) : null}
        {error ? (
          <Text style={[typography.caption, {color: colors.error, marginTop: spacing.xs}]}>{error}</Text>
        ) : null}

        <View style={[styles.card, {backgroundColor: colors.surfaceAlt, borderColor: colors.border}]}>
          <InfoRow label="E-mail" value={account.email || '—'} />
          <InfoRow label="Téléphone" value={account.phone || '—'} />
          <InfoRow label="Ville" value={account.city || '—'} />
          <InfoRow label="Date de naissance" value={account.birthday || '—'} />
          <InfoRow label="Allergies" value={account.allergies.length ? account.allergies.join(', ') : '—'} />
          <InfoRow label="Régimes alimentaires" value={account.diets.length ? account.diets.join(', ') : '—'} />
          <InfoRow label="Préférences" value={account.preferences || '—'} />
        </View>

        <Button
          label="Modifier le profil"
          onPress={() => navigation.navigate('EditProfile')}
          style={styles.action}
        />
        <Button
          label="Préférences de notification"
          onPress={() => navigation.navigate('Notifications')}
          variant="secondary"
          style={styles.action}
        />
        <Button label="Se déconnecter" onPress={signOutDiner} variant="secondary" style={styles.action} />
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
    marginTop: spacing.lg,
  },
  row: {
    marginBottom: spacing.sm,
  },
  rowValue: {
    marginTop: 2,
  },
  action: {
    marginTop: spacing.md,
  },
});
