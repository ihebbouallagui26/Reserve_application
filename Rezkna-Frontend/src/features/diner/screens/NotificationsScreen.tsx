import React, {useCallback, useEffect, useState} from 'react';
import {ActivityIndicator, StyleSheet, Switch, Text, View} from 'react-native';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {layout, radius, spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {useDinerAuthErrorHandler} from '../../../session/useDinerAuthErrorHandler';
import type {DinerPreferences} from '../../../models/diner';

type PreferenceKey = keyof DinerPreferences;

const ROWS: {key: PreferenceKey; label: string; description: string}[] = [
  {
    key: 'notifySms',
    label: 'Notifications par SMS',
    description: 'Confirmations et rappels de réservation par SMS.',
  },
  {
    key: 'notifyEmail',
    label: 'Notifications par e-mail',
    description: 'Confirmations et rappels de réservation par e-mail.',
  },
  {
    key: 'marketingOptIn',
    label: 'Marketing',
    description: 'Offres et actualités des restaurants que vous visitez.',
  },
];

export function NotificationsScreen() {
  const colors = useThemeColors();
  const handleAuthError = useDinerAuthErrorHandler();
  const [prefs, setPrefs] = useState<DinerPreferences | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingKey, setSavingKey] = useState<PreferenceKey | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const fresh = await identityApi.getPreferences();
      setPrefs(fresh);
    } catch (err) {
      setError(handleAuthError(err));
    } finally {
      setLoading(false);
    }
  }, [handleAuthError]);

  useEffect(() => {
    load();
  }, [load]);

  const toggle = async (key: PreferenceKey) => {
    if (!prefs || savingKey) {
      return;
    }
    const previous = prefs;
    const optimistic: DinerPreferences = {...prefs, [key]: !prefs[key]};
    setPrefs(optimistic);
    setSavingKey(key);
    setError(null);
    try {
      const saved = await identityApi.updatePreferences(optimistic);
      setPrefs(saved);
    } catch (err) {
      setPrefs(previous);
      setError(handleAuthError(err));
    } finally {
      setSavingKey(null);
    }
  };

  if (loading || !prefs) {
    return (
      <ScreenContainer style={styles.centered}>
        <ActivityIndicator color={colors.primary} />
      </ScreenContainer>
    );
  }

  return (
    <ScreenContainer>
      <View style={styles.content}>
        {error ? (
          <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.md}]}>{error}</Text>
        ) : null}

        {ROWS.map(row => (
          <View
            key={row.key}
            style={[styles.row, {backgroundColor: colors.surfaceAlt, borderColor: colors.border}]}>
            <View style={styles.rowText}>
              <Text style={[typography.bodyBold, {color: colors.textPrimary}]}>{row.label}</Text>
              <Text style={[typography.caption, styles.rowDescription, {color: colors.textSecondary}]}>
                {row.description}
              </Text>
            </View>
            <Switch
              value={prefs[row.key]}
              onValueChange={() => toggle(row.key)}
              disabled={savingKey !== null}
              trackColor={{false: colors.border, true: colors.primary}}
              thumbColor={colors.surface}
            />
          </View>
        ))}
      </View>
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
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderRadius: radius.lg,
    padding: spacing.md,
    marginBottom: spacing.sm,
  },
  rowText: {
    flex: 1,
    marginRight: spacing.md,
  },
  rowDescription: {
    marginTop: 2,
  },
});
