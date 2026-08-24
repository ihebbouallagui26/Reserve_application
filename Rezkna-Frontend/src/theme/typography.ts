import type {TextStyle} from 'react-native';

type NamedTextStyle = Pick<TextStyle, 'fontSize' | 'fontWeight' | 'lineHeight'>;

export const typography: Record<
  'h1' | 'h2' | 'h3' | 'body' | 'bodyBold' | 'caption' | 'button',
  NamedTextStyle
> = {
  h1: {fontSize: 32, fontWeight: '700', lineHeight: 38},
  h2: {fontSize: 24, fontWeight: '700', lineHeight: 30},
  h3: {fontSize: 18, fontWeight: '600', lineHeight: 24},
  body: {fontSize: 16, fontWeight: '400', lineHeight: 22},
  bodyBold: {fontSize: 16, fontWeight: '600', lineHeight: 22},
  caption: {fontSize: 13, fontWeight: '400', lineHeight: 18},
  button: {fontSize: 16, fontWeight: '600', lineHeight: 20},
};
