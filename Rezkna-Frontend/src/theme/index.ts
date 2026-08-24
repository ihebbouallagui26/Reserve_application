import {useColorScheme} from 'react-native';
import {darkColors, lightColors} from './colors';
import type {ThemeColors} from './colors';

export {spacing} from './spacing';
export {radius} from './radius';
export {typography} from './typography';
export {shadows} from './shadows';
export {layout} from './layout';
export type {ThemeColors} from './colors';

/** Resolves the REZKNA color tokens for the device's current light/dark setting. */
export function useThemeColors(): ThemeColors {
  const scheme = useColorScheme();
  return scheme === 'dark' ? darkColors : lightColors;
}
