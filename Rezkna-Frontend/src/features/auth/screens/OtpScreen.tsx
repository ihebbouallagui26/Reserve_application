import React, {useState} from 'react';
import {StyleSheet, Text} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {getErrorMessage} from '../../../models/errors';
import {isValidOtpCode} from '../../../utils/validation';
import {useSession} from '../../../session/SessionProvider';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Otp'>;

/**
 * The code is never logged and never expected back from any API response -
 * the user is the only source of it (via SMS, outside this app).
 */
export function OtpScreen({route}: Props) {
  const {phone} = route.params;
  const colors = useThemeColors();
  const {signInDiner} = useSession();
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!isValidOtpCode(code)) {
      setError('Saisissez le code à 6 chiffres.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const response = await identityApi.verifyPhone({phone, code: code.trim()});
      await signInDiner({token: response.token, account: response.account});
    } catch (err) {
      // Covers 401 (wrong/expired code) and 429 (too many attempts) with the
      // same safe, generic-or-explicit message the client is given.
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContainer compact>
      <Text style={[typography.h2, {color: colors.textPrimary, marginBottom: spacing.sm}]}>
        Saisissez le code
      </Text>
      <Text style={[typography.body, {color: colors.textSecondary, marginBottom: spacing.lg}]}>
        Nous avons envoyé un code à 6 chiffres au {phone}.
      </Text>

      <TextField
        label="Code"
        value={code}
        onChangeText={setCode}
        placeholder="000000"
        keyboardType="number-pad"
        maxLength={6}
        error={error ?? undefined}
        editable={!loading}
      />

      <Button label="Vérifier" onPress={handleSubmit} loading={loading} style={styles.submit} />
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  submit: {
    marginTop: spacing.sm,
  },
});
