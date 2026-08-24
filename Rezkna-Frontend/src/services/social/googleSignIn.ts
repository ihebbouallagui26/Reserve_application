import type {SocialSignInProvider} from './types';

/**
 * No native Google Sign-In SDK is wired up: GOOGLE_CLIENT_ID has never been
 * provisioned with a real value anywhere in this project (still a
 * placeholder in .env.example). Installing
 * @react-native-google-signin/google-signin without a real client ID would
 * mean shipping a native module that cannot actually authenticate - a fake
 * integration, which the Sprint 1 rules explicitly forbid. The backend
 * contract (POST /api/identity/social, provider=google, aud checked
 * server-side) is already fully implemented and tested; only this
 * client-side credential-acquisition step is pending real Google Cloud
 * credentials. Swapping this stub for the real SDK later requires no
 * change anywhere else - screens only depend on the SocialSignInProvider
 * interface.
 */
export const googleSignInProvider: SocialSignInProvider = {
  isConfigured: false,
  async signIn() {
    throw new Error('La connexion Google n’est pas encore configurée pour cette version.');
  },
};
