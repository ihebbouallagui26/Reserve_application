import {useCallback} from 'react';
import {useSession} from './SessionProvider';
import {getErrorMessage, isApiError} from '../models/errors';

/**
 * Partner equivalent of useDinerAuthErrorHandler. 403 (HOST attempting an
 * OWNER-only action) and 404 (staff not found / wrong property) are
 * deliberately NOT auto-signed-out for - they are authorization/data
 * outcomes, not session-validity outcomes, and the screen shows their
 * message inline instead.
 */
export function usePartnerAuthErrorHandler() {
  const {signOutPartner} = useSession();

  return useCallback(
    (error: unknown): string => {
      if (isApiError(error) && error.kind === 'unauthorized') {
        signOutPartner().catch(() => {});
      }
      return getErrorMessage(error);
    },
    [signOutPartner],
  );
}
