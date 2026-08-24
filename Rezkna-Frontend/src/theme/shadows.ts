import {Platform} from 'react-native';
import type {ViewStyle} from 'react-native';

/** Light, modern elevation - matches the soft-shadow cards visible in the REZKNA references. */
export const shadows: Record<'sm' | 'md', ViewStyle> = {
  sm: Platform.select({
    ios: {
      shadowColor: '#000000',
      shadowOpacity: 0.08,
      shadowRadius: 4,
      shadowOffset: {width: 0, height: 1},
    },
    android: {elevation: 2},
    default: {},
  }) as ViewStyle,
  md: Platform.select({
    ios: {
      shadowColor: '#000000',
      shadowOpacity: 0.12,
      shadowRadius: 8,
      shadowOffset: {width: 0, height: 2},
    },
    android: {elevation: 4},
    default: {},
  }) as ViewStyle,
};
