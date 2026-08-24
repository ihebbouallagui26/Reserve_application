import React, {useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {BackButton} from '../../../components/BackButton';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {layout, spacing, typography, useThemeColors} from '../../../theme';
import {partnerApi} from '../../../services/api/partnerApi';
import {getErrorMessage, isApiError} from '../../../models/errors';
import {isValidEmail} from '../../../utils/validation';
import {useSession} from '../../../session/SessionProvider';
import type {AuthStackParamList} from '../../auth/navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'PartnerLogin'>;

interface FieldErrors {
  email?: string;
  password?: string;
  propertyId?: string;
}

/**
 * identity-service's POST /partner/login returns 409 (no candidate list in
 * the body - the backend deliberately keeps that response message-only) when
 * an account matches more than one property and no propertyId was given.
 * There is no way to show a real picker without inventing data, so this
 * screen honestly asks the user to type their Property ID once that
 * happens, rather than pretending to offer a selection it doesn't have.
 */
export function PartnerLoginScreen(_props: Props) {
  const colors = useThemeColors();
  const {signInPartner} = useSession();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [propertyId, setPropertyId] = useState('');
  const [needsPropertyId, setNeedsPropertyId] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    const errors: FieldErrors = {};
    if (!isValidEmail(email)) {
      errors.email = 'Saisissez une adresse e-mail valide.';
    }
    if (password.length === 0) {
      errors.password = 'Le mot de passe est requis.';
    }
    if (needsPropertyId && propertyId.trim().length === 0) {
      errors.propertyId = 'L’identifiant de l’établissement est requis.';
    }
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    setFormError(null);
    setLoading(true);
    try {
      const response = await partnerApi.login({
        email: email.trim().toLowerCase(),
        password,
        propertyId: needsPropertyId ? propertyId.trim() : undefined,
      });
      await signInPartner({
        token: response.token,
        user: response.user,
        propertyId: response.propertyId,
      });
    } catch (error) {
      if (isApiError(error) && error.kind === 'conflict') {
        setNeedsPropertyId(true);
        setFormError(
          'Plusieurs établissements sont associés à ce compte. Saisissez l’identifiant de l’établissement pour continuer.',
        );
      } else {
        setFormError(getErrorMessage(error));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScreenContainer style={styles.screen}>
      <BackButton color={colors.primary} background="transparent" />
      <View style={styles.container}>
        <Text style={[typography.h2, {color: colors.textPrimary, marginBottom: spacing.xs}]}>
          Connexion partenaire
        </Text>
        <Text style={[typography.body, {color: colors.textSecondary, marginBottom: spacing.lg}]}>
          Gérez votre équipe.
        </Text>

        <TextField
          label="E-mail"
          value={email}
          onChangeText={setEmail}
          placeholder="proprietaire@exemple.com"
          keyboardType="email-address"
          autoCapitalize="none"
          error={fieldErrors.email}
          editable={!loading}
        />
        <TextField
          label="Mot de passe"
          value={password}
          onChangeText={setPassword}
          placeholder="••••••••"
          secureTextEntry
          autoCapitalize="none"
          error={fieldErrors.password}
          editable={!loading}
        />
        {needsPropertyId ? (
          <TextField
            label="Identifiant de l'établissement"
            value={propertyId}
            onChangeText={setPropertyId}
            autoCapitalize="none"
            error={fieldErrors.propertyId}
            editable={!loading}
          />
        ) : null}

        {formError ? (
          <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{formError}</Text>
        ) : null}

        <Button label="Se connecter" onPress={handleSubmit} loading={loading} style={styles.submit} />
      </View>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  screen: {
    justifyContent: 'center',
  },
  container: {
    width: '100%',
    maxWidth: layout.formMaxWidth,
    alignSelf: 'center',
    paddingHorizontal: spacing.lg,
  },
  submit: {
    marginTop: spacing.sm,
  },
});
