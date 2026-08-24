import React, {useState} from 'react';
import {Alert, Pressable, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {FloatingIcon} from '../../../components/FloatingIcon';
import {ChefHatIcon, CoffeeCupIcon} from '../../../components/icons/RestaurantIcons';
import {spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {getErrorMessage} from '../../../models/errors';
import {isValidEmail} from '../../../utils/validation';
import {useSession} from '../../../session/SessionProvider';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Login'>;

interface FieldErrors {
  email?: string;
  password?: string;
}

const DECORATION_COLOR = 'rgba(255,255,255,0.75)';

export function LoginScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {signInDiner} = useSession();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
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
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    setFormError(null);
    setLoading(true);
    try {
      const response = await identityApi.login({email: email.trim().toLowerCase(), password});
      await signInDiner({token: response.token, account: response.account});
    } catch (error) {
      // Deliberately generic on 401, matching the backend: never reveal
      // whether the email exists.
      setFormError(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = () => {
    // No forgot-password endpoint exists in Sprint 1 - be honest about that
    // rather than pretending to send a reset email.
    Alert.alert(
      'Indisponible pour le moment',
      'La réinitialisation du mot de passe n’est pas encore disponible dans cette version.',
    );
  };

  return (
    <AuthContainer
      compact
      headerExtra={
        <View style={StyleSheet.absoluteFill} pointerEvents="none">
          <FloatingIcon
            Icon={ChefHatIcon}
            size={24}
            color={DECORATION_COLOR}
            top="32%"
            left="12%"
            direction="ltr"
            travel={34}
            durationMs={8200}
          />
          <FloatingIcon
            Icon={CoffeeCupIcon}
            size={22}
            color={DECORATION_COLOR}
            top="16%"
            left="76%"
            direction="rtl"
            travel={40}
            durationMs={9200}
            delayMs={400}
          />
        </View>
      }>
      <Text style={[typography.h2, {color: colors.textPrimary, marginBottom: spacing.lg}]}>Connexion</Text>

      <TextField
        label="E-mail"
        value={email}
        onChangeText={setEmail}
        placeholder="vous@exemple.com"
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

      <View style={styles.metaRow}>
        <Pressable onPress={handleForgotPassword} disabled={loading} hitSlop={8}>
          <Text style={[typography.caption, {color: colors.primary}]}>Mot de passe oublié ?</Text>
        </Pressable>
      </View>

      {formError ? (
        <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{formError}</Text>
      ) : null}

      <Button label="Se connecter" onPress={handleSubmit} loading={loading} style={styles.submit} />

      <Pressable onPress={() => navigation.navigate('Register')} disabled={loading}>
        <Text
          style={[
            typography.body,
            styles.centerText,
            {color: colors.primary, marginTop: spacing.lg},
          ]}>
          Nouveau ici ? Créer un compte
        </Text>
      </Pressable>
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  metaRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginBottom: spacing.md,
  },
  submit: {
    marginTop: spacing.sm,
  },
  centerText: {
    textAlign: 'center',
  },
});
