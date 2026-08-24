import React, {useState} from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {Button} from '../../../components/Button';
import {EmailMethodIcon, FacebookIcon, GoogleIcon, PhoneMethodIcon} from '../../../components/icons/AuthMethodIcons';
import {spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {googleSignInProvider} from '../../../services/social/googleSignIn';
import {facebookSignInProvider} from '../../../services/social/facebookSignIn';
import {getErrorMessage} from '../../../models/errors';
import {useSession} from '../../../session/SessionProvider';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Landing'>;

type SocialProvider = 'google' | 'facebook';

const METHOD_ICON_SIZE = 22;

/**
 * Reference: the REZKNA diner auth screenshot (logo, tagline, Google,
 * Facebook, phone, email). Apple Sign-In is deliberately omitted -
 * identity-service has no Apple support in Sprint 1.
 */
export function LandingScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {signInDiner} = useSession();
  const [loadingProvider, setLoadingProvider] = useState<SocialProvider | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSocial = async (provider: SocialProvider) => {
    setError(null);
    setLoadingProvider(provider);
    try {
      const client = provider === 'google' ? googleSignInProvider : facebookSignInProvider;
      const credential = await client.signIn();
      const response = await identityApi.socialLogin({
        provider,
        token: credential.token,
        name: credential.name,
      });
      await signInDiner({token: response.token, account: response.account});
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoadingProvider(null);
    }
  };

  return (
    <AuthContainer>
      <Text style={[typography.h2, styles.centerText, styles.brandMark, {color: colors.primary}]}>
        REZKNA
      </Text>

      {error ? (
        <Text
          style={[
            typography.caption,
            styles.centerText,
            {color: colors.error, marginBottom: spacing.sm},
          ]}>
          {error}
        </Text>
      ) : null}

      <Button
        label="Continuer avec Google"
        onPress={() => handleSocial('google')}
        variant="secondary"
        icon={<GoogleIcon size={METHOD_ICON_SIZE} color={colors.primary} />}
        loading={loadingProvider === 'google'}
        disabled={loadingProvider !== null}
      />
      <Button
        label="Continuer avec Facebook"
        onPress={() => handleSocial('facebook')}
        variant="secondary"
        icon={<FacebookIcon size={METHOD_ICON_SIZE} color={colors.primary} />}
        loading={loadingProvider === 'facebook'}
        disabled={loadingProvider !== null}
        style={styles.spacedButton}
      />

      <View style={styles.divider}>
        <View style={[styles.dividerLine, {backgroundColor: colors.border}]} />
        <Text style={[typography.caption, {color: colors.textSecondary, marginHorizontal: spacing.sm}]}>ou</Text>
        <View style={[styles.dividerLine, {backgroundColor: colors.border}]} />
      </View>

      <Button
        label="Continuer avec le téléphone"
        onPress={() => navigation.navigate('Phone')}
        icon={<PhoneMethodIcon size={METHOD_ICON_SIZE} color={colors.onPrimary} />}
        style={styles.spacedButton}
      />
      <Button
        label="Continuer avec l'email"
        onPress={() => navigation.navigate('Login')}
        variant="secondary"
        icon={<EmailMethodIcon size={METHOD_ICON_SIZE} color={colors.primary} />}
        style={styles.spacedButton}
      />

      <Pressable onPress={() => navigation.navigate('PartnerLogin')} style={styles.partnerLink}>
        <Text style={[typography.caption, styles.centerText, {color: colors.textSecondary}]}>
          Restaurant partenaire ? Connectez-vous
        </Text>
      </Pressable>
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  brandMark: {
    marginTop: spacing.md,
    marginBottom: spacing.lg,
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: spacing.md,
  },
  dividerLine: {
    flex: 1,
    height: StyleSheet.hairlineWidth,
  },
  spacedButton: {
    marginTop: spacing.sm,
  },
  centerText: {
    textAlign: 'center',
  },
  partnerLink: {
    marginTop: spacing.xl,
    alignItems: 'center',
  },
});
