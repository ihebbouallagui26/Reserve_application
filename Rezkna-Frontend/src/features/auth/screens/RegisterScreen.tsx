import React, {useState} from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {FloatingIcon} from '../../../components/FloatingIcon';
import {ForkSpoonIcon, ShopIcon} from '../../../components/icons/RestaurantIcons';
import {spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {getErrorMessage} from '../../../models/errors';
import {isValidEmail, isValidPassword} from '../../../utils/validation';
import {useSession} from '../../../session/SessionProvider';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Register'>;

interface FieldErrors {
  name?: string;
  email?: string;
  password?: string;
}

const DECORATION_COLOR = 'rgba(255,255,255,0.75)';

/**
 * Only the fields identity-service actually requires (name, email,
 * password). phone/city/preferences are also accepted by POST /register but
 * are left for the profile screen (Phase 3) - keeping this form focused.
 */
export function RegisterScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {signInDiner} = useSession();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
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
      const response = await identityApi.register({
        email: email.trim().toLowerCase(),
        password,
        name: name.trim(),
      });
      await signInDiner({token: response.token, account: response.account});
    } catch (error) {
      setFormError(getErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContainer
      compact
      headerExtra={
        <View style={StyleSheet.absoluteFill} pointerEvents="none">
          <FloatingIcon
            Icon={ForkSpoonIcon}
            size={22}
            color={DECORATION_COLOR}
            top="32%"
            left="12%"
            direction="ltr"
            travel={34}
            durationMs={8600}
          />
          <FloatingIcon
            Icon={ShopIcon}
            size={24}
            color={DECORATION_COLOR}
            top="16%"
            left="76%"
            direction="rtl"
            travel={40}
            durationMs={9600}
            delayMs={400}
          />
        </View>
      }>
      <Text style={[typography.h2, {color: colors.textPrimary, marginBottom: spacing.lg}]}>
        Créer votre compte
      </Text>

      <TextField label="Nom" value={name} onChangeText={setName} error={fieldErrors.name} editable={!loading} />
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
        placeholder="Au moins 8 caractères"
        secureTextEntry
        autoCapitalize="none"
        error={fieldErrors.password}
        editable={!loading}
      />

      {formError ? (
        <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{formError}</Text>
      ) : null}

      <Button label="Créer le compte" onPress={handleSubmit} loading={loading} style={styles.submit} />

      <Pressable onPress={() => navigation.navigate('Login')} disabled={loading}>
        <Text
          style={[
            typography.body,
            styles.centerText,
            {color: colors.primary, marginTop: spacing.lg},
          ]}>
          Vous avez déjà un compte ? Connectez-vous
        </Text>
      </Pressable>
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  submit: {
    marginTop: spacing.sm,
  },
  centerText: {
    textAlign: 'center',
  },
});
