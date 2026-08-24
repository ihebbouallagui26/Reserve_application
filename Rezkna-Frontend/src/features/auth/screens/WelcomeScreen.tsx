import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import {AuthContainer} from '../../../components/AuthContainer';
import {FloatingIcon} from '../../../components/FloatingIcon';
import {ChefHatIcon, ClocheIcon, CoffeeCupIcon, ForkSpoonIcon, ShopIcon} from '../../../components/icons/RestaurantIcons';
import {Button} from '../../../components/Button';
import {spacing, typography, useThemeColors} from '../../../theme';
import type {AuthStackParamList} from '../navigation/types';

type Props = NativeStackScreenProps<AuthStackParamList, 'Welcome'>;

const DECORATION_COLOR = 'rgba(255,255,255,0.75)';

/**
 * First screen a user without a session lands on (see AuthNavigator's
 * initialRouteName). Purely an entry moment - "Continuer" hands off to the
 * existing LandingScreen, which still carries every real sign-in path.
 * Nothing here talks to the backend.
 */
export function WelcomeScreen({navigation}: Props) {
  const colors = useThemeColors();

  return (
    <AuthContainer
      headerExtra={
        <View style={StyleSheet.absoluteFill} pointerEvents="none">
          <FloatingIcon
            Icon={ChefHatIcon}
            size={30}
            color={DECORATION_COLOR}
            top="12%"
            left="14%"
            direction="ltr"
            travel={38}
            durationMs={8600}
          />
          <FloatingIcon
            Icon={ShopIcon}
            size={28}
            color={DECORATION_COLOR}
            top="14%"
            left="74%"
            direction="rtl"
            travel={45}
            durationMs={9600}
            delayMs={300}
          />
          <FloatingIcon
            Icon={ClocheIcon}
            size={30}
            color={DECORATION_COLOR}
            top="64%"
            left="10%"
            direction="ltr"
            travel={28}
            durationMs={10200}
            delayMs={600}
          />
          <FloatingIcon
            Icon={CoffeeCupIcon}
            size={26}
            color={DECORATION_COLOR}
            top="58%"
            left="80%"
            direction="rtl"
            travel={36}
            durationMs={9000}
            delayMs={900}
          />
          <FloatingIcon
            Icon={ForkSpoonIcon}
            size={22}
            color={DECORATION_COLOR}
            top="42%"
            left="7%"
            direction="rtl"
            travel={20}
            durationMs={11000}
            delayMs={200}
          />
        </View>
      }>
      <View>
        <Text style={[typography.h2, styles.lead, {color: colors.textSecondary}]}>
          Bienvenue sur
        </Text>
        <Text style={[typography.h1, styles.brand, {color: colors.primary}]}>REZKNA</Text>
        <Text style={[typography.body, styles.description, {color: colors.textSecondary}]}>
          Découvrez les restaurants près de chez vous, consultez leurs menus et réservez une
          table en quelques instants.
        </Text>
      </View>

      <Button
        label="Continuer  →"
        onPress={() => navigation.replace('Landing')}
        style={styles.continueButton}
      />
    </AuthContainer>
  );
}

const styles = StyleSheet.create({
  lead: {
    fontWeight: '400',
  },
  brand: {
    fontSize: 38,
    lineHeight: 44,
    letterSpacing: 0.3,
  },
  description: {
    marginTop: spacing.md,
  },
  continueButton: {
    alignSelf: 'flex-end',
    marginTop: 'auto',
    paddingHorizontal: spacing.xl,
  },
});
