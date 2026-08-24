import React from 'react';
import {
  Image,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  View,
  useWindowDimensions,
} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';
import {AuthBackground} from './AuthBackground';
import {AuthWave} from './AuthWave';
import {BackButton} from './BackButton';
import {spacing, useThemeColors} from '../theme';

interface AuthContainerProps {
  children: React.ReactNode;
  /** Landing uses a taller header (welcome moment); form screens use a shorter one
   * so the fields stay above the fold on small phones. */
  compact?: boolean;
  /** Optional decoration (e.g. FloatingIcon glyphs) rendered inside the blue
   * header, behind the logo badge. */
  headerExtra?: React.ReactNode;
}

const FORM_MAX_WIDTH = 480;
const LOGO_ASPECT_RATIO = 890 / 607;

/**
 * Shared shell for every Diner auth screen: blue header with the REZKNA logo,
 * a wave into the white content area, and a keyboard-safe scroll view. Sizing
 * is percentage/window-based (useWindowDimensions), not fixed pixel values,
 * so the same layout holds up from a small phone to a tablet - the content
 * itself is additionally capped at FORM_MAX_WIDTH and centered so it never
 * stretches edge-to-edge on a tablet.
 */
export function AuthContainer({children, compact = false, headerExtra}: AuthContainerProps) {
  const colors = useThemeColors();
  const {height, width} = useWindowDimensions();
  const headerHeight = compact
    ? Math.min(Math.max(height * 0.28, 190), 300)
    : Math.min(Math.max(height * 0.4, 260), 440);
  const logoWidth = Math.min(width * 0.24, 92);
  const badgeSize = logoWidth + spacing.lg * 2;

  return (
    <View style={[styles.root, {backgroundColor: colors.primary}]}>
      <SafeAreaView edges={['top']} style={styles.safeTop}>
        <AuthBackground height={headerHeight}>
          {headerExtra}
          <View
            style={[
              styles.logoBadge,
              {width: badgeSize, height: badgeSize, borderRadius: badgeSize / 2, backgroundColor: colors.background},
            ]}>
            <Image
              source={require('../../assets/rezkna-logo.png')}
              resizeMode="contain"
              style={{width: logoWidth, height: logoWidth / LOGO_ASPECT_RATIO}}
            />
          </View>
        </AuthBackground>
        <BackButton />
      </SafeAreaView>
      <AuthWave />
      <KeyboardAvoidingView
        style={[styles.flex, {backgroundColor: colors.background}]}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}>
          <SafeAreaView edges={['bottom']} style={styles.formWrap}>
            {children}
          </SafeAreaView>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  safeTop: {
    backgroundColor: 'transparent',
  },
  flex: {
    flex: 1,
  },
  logoBadge: {
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000000',
    shadowOpacity: 0.15,
    shadowRadius: 10,
    shadowOffset: {width: 0, height: 4},
    elevation: 6,
  },
  scrollContent: {
    flexGrow: 1,
  },
  formWrap: {
    flex: 1,
    width: '100%',
    maxWidth: FORM_MAX_WIDTH,
    alignSelf: 'center',
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.xl,
    paddingBottom: spacing.xl,
  },
});
