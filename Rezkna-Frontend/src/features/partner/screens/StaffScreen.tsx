import React, {useCallback, useState} from 'react';
import {ActivityIndicator, Alert, FlatList, Pressable, StyleSheet, Text, View} from 'react-native';
import {useFocusEffect} from '@react-navigation/native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {Button} from '../../../components/Button';
import {Badge} from '../../../components/Badge';
import {layout, radius, spacing, typography, useThemeColors} from '../../../theme';
import {partnerApi} from '../../../services/api/partnerApi';
import {useSession} from '../../../session/SessionProvider';
import {usePartnerAuthErrorHandler} from '../../../session/usePartnerAuthErrorHandler';
import type {PartnerUser} from '../../../models/partner';
import type {PartnerStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<PartnerStackParamList, 'Staff'>;

/** Display-only French label for a partner role - the underlying value (used
 * for permission checks) is never translated, only what the user reads. */
function roleLabel(role: string): string {
  return role === 'OWNER' ? 'Propriétaire' : role === 'HOST' ? 'Hôte' : role;
}

/**
 * OWNER sees "Add staff member" and a "Remove" action per row. HOST sees
 * neither - the actions are hidden, not just disabled, per the UX
 * requirement. The backend still enforces this independently (403 if
 * bypassed); the frontend only improves the experience.
 */
export function StaffScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {partnerSession} = useSession();
  const handleAuthError = usePartnerAuthErrorHandler();
  const isOwner = partnerSession?.user.role === 'OWNER';
  const currentUserId = partnerSession?.user.id;

  const [staff, setStaff] = useState<PartnerUser[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const list = await partnerApi.getStaff();
      setStaff(list);
    } catch (err) {
      setError(handleAuthError(err));
    } finally {
      setLoading(false);
    }
  }, [handleAuthError]);

  // Refetch every time this screen regains focus, so returning from
  // "Add staff member" shows the new row without a manual refresh.
  useFocusEffect(
    useCallback(() => {
      load();
    }, [load]),
  );

  const handleDelete = useCallback(
    async (id: string) => {
      setDeletingId(id);
      setError(null);
      try {
        await partnerApi.deleteStaff(id);
        setStaff(prev => (prev ? prev.filter(member => member.id !== id) : prev));
      } catch (err) {
        setError(handleAuthError(err));
      } finally {
        setDeletingId(null);
      }
    },
    [handleAuthError],
  );

  const confirmDelete = useCallback(
    (member: PartnerUser) => {
      Alert.alert(
        'Retirer un membre de l’équipe',
        `Retirer ${member.name} (${member.email}) de l’équipe ?`,
        [
          {text: 'Annuler', style: 'cancel'},
          {text: 'Retirer', style: 'destructive', onPress: () => handleDelete(member.id)},
        ],
      );
    },
    [handleDelete],
  );

  return (
    <ScreenContainer>
      <View style={styles.header}>
        {isOwner ? (
          <Button label="Ajouter un membre" onPress={() => navigation.navigate('AddStaff')} />
        ) : null}
        {error ? (
          <Text style={[typography.caption, {color: colors.error, marginTop: spacing.sm}]}>{error}</Text>
        ) : null}
      </View>

      {loading && !staff ? (
        <View style={styles.centered}>
          <ActivityIndicator color={colors.primary} />
        </View>
      ) : (
        <FlatList
          data={staff ?? []}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={
            <Text style={[typography.body, styles.emptyText, {color: colors.textSecondary}]}>
              Aucun membre pour le moment.
            </Text>
          }
          renderItem={({item}) => {
            const isSelf = item.id === currentUserId;
            return (
              <View style={[styles.row, {backgroundColor: colors.surfaceAlt, borderColor: colors.border}]}>
                <View style={styles.rowInfo}>
                  <Text style={[typography.bodyBold, {color: colors.textPrimary}]}>{item.name}</Text>
                  <Text style={[typography.caption, styles.rowEmail, {color: colors.textSecondary}]}>
                    {item.email}
                  </Text>
                  <View style={styles.badgeRow}>
                    <Badge label={roleLabel(item.role)} tone={item.role === 'OWNER' ? 'primary' : 'neutral'} />
                    {!item.active ? (
                      <View style={styles.inactiveBadge}>
                        <Badge label="Inactif" />
                      </View>
                    ) : null}
                  </View>
                </View>
                {isOwner && !isSelf ? (
                  <Pressable
                    onPress={() => confirmDelete(item)}
                    disabled={deletingId === item.id}
                    style={styles.removeButton}>
                    {deletingId === item.id ? (
                      <ActivityIndicator color={colors.error} size="small" />
                    ) : (
                      <Text style={[typography.bodyBold, {color: colors.error}]}>Retirer</Text>
                    )}
                  </Pressable>
                ) : null}
              </View>
            );
          }}
        />
      )}
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  header: {
    width: '100%',
    maxWidth: layout.contentMaxWidth,
    alignSelf: 'center',
    padding: spacing.lg,
    paddingBottom: 0,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  listContent: {
    width: '100%',
    maxWidth: layout.contentMaxWidth,
    alignSelf: 'center',
    padding: spacing.lg,
    flexGrow: 1,
  },
  emptyText: {
    textAlign: 'center',
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
  rowInfo: {
    flex: 1,
    marginRight: spacing.md,
  },
  rowEmail: {
    marginTop: 2,
    marginBottom: spacing.xs,
  },
  badgeRow: {
    flexDirection: 'row',
  },
  inactiveBadge: {
    marginLeft: spacing.xs,
  },
  removeButton: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
  },
});
