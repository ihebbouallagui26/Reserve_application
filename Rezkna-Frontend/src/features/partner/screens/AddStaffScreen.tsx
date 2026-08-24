import React, {useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {layout, spacing, typography, useThemeColors} from '../../../theme';
import {partnerApi} from '../../../services/api/partnerApi';
import {usePartnerAuthErrorHandler} from '../../../session/usePartnerAuthErrorHandler';
import {isValidEmail, isValidPassword} from '../../../utils/validation';
import type {PartnerRole} from '../../../models/partner';
import type {PartnerStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<PartnerStackParamList, 'AddStaff'>;

interface FieldErrors {
  name?: string;
  email?: string;
  password?: string;
}

/** Reachable only from StaffScreen's OWNER-only "Add staff member" button. */
export function AddStaffScreen({navigation}: Props) {
  const colors = useThemeColors();
  const handleAuthError = usePartnerAuthErrorHandler();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<PartnerRole>('HOST');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    const errors: FieldErrors = {};
    if (name.trim().length === 0) {
      errors.name = 'Le nom est requis.';
    }
    if (!isValidEmail(email)) {
      errors.email = 'Saisissez une adresse e-mail valide.';
    }
    if (!isValidPassword(password)) {
      errors.password = 'Le mot de passe doit contenir au moins 8 caractères.';
    }
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    setFormError(null);
    setLoading(true);
    try {
      await partnerApi.createStaff({
        name: name.trim(),
        email: email.trim().toLowerCase(),
        password,
        role,
      });
      navigation.goBack();
    } catch (err) {
      setFormError(handleAuthError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScreenContainer>
      <View style={styles.content}>
        <TextField label="Nom" value={name} onChangeText={setName} error={fieldErrors.name} editable={!loading} />
        <TextField
          label="E-mail"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
          error={fieldErrors.email}
          editable={!loading}
        />
        <TextField
          label="Mot de passe"
          value={password}
          onChangeText={setPassword}
          placeholder="Au moins 8 caractères"
          secureTextEntry
          autoCapitalize="none"
          error={fieldErrors.password}
          editable={!loading}
        />

        <Text style={[typography.caption, {color: colors.textSecondary, marginBottom: spacing.xs}]}>
          Rôle
        </Text>
        <View style={styles.roleRow}>
          <Button
            label="Hôte"
            variant={role === 'HOST' ? 'primary' : 'secondary'}
            onPress={() => setRole('HOST')}
            disabled={loading}
            style={styles.roleButton}
          />
          <Button
            label="Propriétaire"
            variant={role === 'OWNER' ? 'primary' : 'secondary'}
            onPress={() => setRole('OWNER')}
            disabled={loading}
            style={styles.roleButton}
          />
        </View>

        {formError ? (
          <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{formError}</Text>
        ) : null}

        <Button label="Ajouter le membre" onPress={handleSubmit} loading={loading} style={styles.submit} />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  content: {
    width: '100%',
    maxWidth: layout.contentMaxWidth,
    alignSelf: 'center',
    padding: spacing.lg,
  },
  roleRow: {
    flexDirection: 'row',
    marginBottom: spacing.md,
  },
  roleButton: {
    flex: 1,
    marginRight: spacing.sm,
  },
  submit: {
    marginTop: spacing.sm,
  },
});
