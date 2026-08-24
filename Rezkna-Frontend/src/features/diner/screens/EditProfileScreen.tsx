import React, {useState} from 'react';
import {ScrollView, StyleSheet, Text} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {ScreenContainer} from '../../../components/ScreenContainer';
import {Button} from '../../../components/Button';
import {TextField} from '../../../components/TextField';
import {layout, spacing, typography, useThemeColors} from '../../../theme';
import {identityApi} from '../../../services/api/identityApi';
import {useSession} from '../../../session/SessionProvider';
import {useDinerAuthErrorHandler} from '../../../session/useDinerAuthErrorHandler';
import type {DinerStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<DinerStackParamList, 'EditProfile'>;

/** "Peanuts, Shellfish" <-> ["Peanuts", "Shellfish"]. An empty field clears the list. */
function splitCsv(value: string): string[] {
  return value
    .split(',')
    .map(item => item.trim())
    .filter(item => item.length > 0);
}

/**
 * Every field here is what the form currently holds is what gets saved -
 * clearing a field and submitting clears it server-side too (identity-service
 * treats an empty string as a real value, not "leave unchanged", which only
 * applies to an omitted/null field).
 */
export function EditProfileScreen({navigation}: Props) {
  const colors = useThemeColors();
  const {dinerSession, updateDinerAccount} = useSession();
  const handleAuthError = useDinerAuthErrorHandler();
  const account = dinerSession?.account;

  const [name, setName] = useState(account?.name ?? '');
  const [phone, setPhone] = useState(account?.phone ?? '');
  const [city, setCity] = useState(account?.city ?? '');
  const [preferences, setPreferences] = useState(account?.preferences ?? '');
  const [birthday, setBirthday] = useState(account?.birthday ?? '');
  const [allergies, setAllergies] = useState(account?.allergies.join(', ') ?? '');
  const [diets, setDiets] = useState(account?.diets.join(', ') ?? '');

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setError(null);
    setLoading(true);
    try {
      const updated = await identityApi.updateMe({
        name: name.trim(),
        phone: phone.trim(),
        city: city.trim(),
        preferences: preferences.trim(),
        birthday: birthday.trim(),
        allergies: splitCsv(allergies),
        diets: splitCsv(diets),
      });
      await updateDinerAccount(updated);
      navigation.goBack();
    } catch (err) {
      setError(handleAuthError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScreenContainer>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <TextField label="Nom" value={name} onChangeText={setName} editable={!loading} />
        <TextField
          label="Téléphone"
          value={phone}
          onChangeText={setPhone}
          keyboardType="phone-pad"
          editable={!loading}
        />
        <TextField label="Ville" value={city} onChangeText={setCity} editable={!loading} />
        <TextField
          label="Date de naissance"
          value={birthday}
          onChangeText={setBirthday}
          placeholder="AAAA-MM-JJ"
          editable={!loading}
        />
        <TextField
          label="Allergies"
          value={allergies}
          onChangeText={setAllergies}
          placeholder="Arachides, Crustacés"
          autoCapitalize="none"
          editable={!loading}
        />
        <TextField
          label="Régimes alimentaires"
          value={diets}
          onChangeText={setDiets}
          placeholder="Végétarien, Halal"
          autoCapitalize="none"
          editable={!loading}
        />
        <TextField
          label="Préférences"
          value={preferences}
          onChangeText={setPreferences}
          placeholder="Table près de la fenêtre, coin calme..."
          editable={!loading}
        />

        {error ? (
          <Text style={[typography.caption, {color: colors.error, marginBottom: spacing.sm}]}>{error}</Text>
        ) : null}

        <Button label="Enregistrer les modifications" onPress={handleSubmit} loading={loading} style={styles.submit} />
      </ScrollView>
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
  submit: {
    marginTop: spacing.sm,
  },
});
