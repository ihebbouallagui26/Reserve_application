import {useCallback} from 'react';
import {useSession} from './SessionProvider';
import {getErrorMessage, isApiError} from '../models/errors';

/**
 * Every diner screen's catch block should route its error through this. On
 * 401 (expired/invalid token - identity-service never distinguishes the
 * reason), it signs the diner out immediately: RootNavigator reacts to the
 * session status change and returns to Auth on its own, so the screen never
 * needs to navigate manually or show a confusing error before disappearing.
 */
export function useDinerAuthErrorHandler() {
  const {signOutDiner} = useSession();

  return useCallback(
    (error: unknown): string => {
      if (isApiError(error) && error.kind === 'unauthorized') {
        signOutDiner().catch(() => {});
      }
      return getErrorMessage(error);
    },
    [signOutDiner],
  );
}
