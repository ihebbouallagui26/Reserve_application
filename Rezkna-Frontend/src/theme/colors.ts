/**
 * REZKNA palette. Blue tone matches the Sprint 0 connectivity screen and the
 * existing REZKNA product screenshots - not copied from any third-party product.
 */
const palette = {
  blue600: '#4a90e2',
  blue700: '#2f74c9',
  blue50: '#eef5fd',
  white: '#ffffff',
  black: '#000000',
  gray50: '#f5f7fa',
  gray100: '#eef0f3',
  gray200: '#e2e5ea',
  gray400: '#9aa0a6',
  gray600: '#666666',
  gray900: '#111318',
  dark: '#121212',
  darkSurface: '#1c1c1e',
  darkBorder: '#2c2c2e',
  green600: '#1e8e3e',
  red600: '#d93025',
  amber600: '#b06000',
} as const;

export interface ThemeColors {
  background: string;
  surface: string;
  surfaceAlt: string;
  border: string;
  textPrimary: string;
  textSecondary: string;
  primary: string;
  primaryPressed: string;
  onPrimary: string;
  success: string;
  error: string;
  warning: string;
  disabled: string;
}

export const lightColors: ThemeColors = {
  background: palette.white,
  surface: palette.white,
  surfaceAlt: palette.gray50,
  border: palette.gray200,
  textPrimary: palette.gray900,
  textSecondary: palette.gray600,
  primary: palette.blue600,
  primaryPressed: palette.blue700,
  onPrimary: palette.white,
  success: palette.green600,
  error: palette.red600,
  warning: palette.amber600,
  disabled: palette.gray400,
};

export const darkColors: ThemeColors = {
  background: palette.dark,
  surface: palette.darkSurface,
  surfaceAlt: palette.darkSurface,
  border: palette.darkBorder,
  textPrimary: palette.white,
  textSecondary: palette.gray400,
  primary: palette.blue600,
  primaryPressed: palette.blue50,
  onPrimary: palette.white,
  success: palette.green600,
  error: '#ff6b60',
  warning: '#f2b94d',
  disabled: palette.gray600,
};
