import React, {useState} from 'react';
import {StyleSheet, Text} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {getErrorMessage} from '../../../models/errors';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Phone'>;

export function PhoneScreen({navigation}: Props) {
  const colors = useThemeColors();
  const [phone, setPhone] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (phone.trim().length === 0) {
      setError('Saisissez votre numéro de téléphone.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      // The backend normalizes the number and never returns the OTP code -
      // only the normalized phone and how long it stays valid.
      const response = await identityApi.startPhoneVerification({phone: phone.trim()});
      navigation.navigate('Otp', {phone: response.phone});
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContainer compact>
      <Text style={[typography.h2, {color: colors.textPrimary, marginBottom: spacing.sm}]}>
        Quel est votre numéro de téléphone ?
      </Text>
      <Text style={[typography.body, {color: colors.textSecondary, marginBottom: spacing.lg}]}>
        Nous vous enverrons un code à 6 chiffres par SMS pour confirmer votre identité.
      </Text>

      <TextField
        label="Téléphone"
        value={phone}
        onChangeText={setPhone}
        placeholder="20123456"
        keyboardType="phone-pad"
        error={error ?? undefined}
        editable={!loading}
      />

      <Button label="Envoyer le code" onPress={handleSubmit} loading={loading} style={styles.submit} />
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  submit: {
    marginTop: spacing.sm,
  },
});
