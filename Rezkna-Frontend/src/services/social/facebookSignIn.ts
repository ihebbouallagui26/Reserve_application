import type {SocialSignInProvider} from './types';

/**
 * Same reasoning as googleSignIn.ts: FACEBOOK_APP_ID/FACEBOOK_APP_SECRET are
 * still placeholders, so react-native-fbsdk-next is deliberately not
 * installed yet. The backend's /debug_token + app_id verification is fully
 * implemented and tested server-side; only the native credential-acquisition
 * step is pending a real Facebook App.
 */
export const facebookSignInProvider: SocialSignInProvider = {
  isConfigured: false,
  async signIn() {
    throw new Error('La connexion Facebook n’est pas encore configurée pour cette version.');
  },
};
