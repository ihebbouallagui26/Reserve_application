import React from 'react';
import {useSession} from '../../../session/SessionProvider';
import {AuthNavigator} from '../../auth/navigation/AuthNavigator';
import {DinerNavigator} from '../../diner/navigation/DinerNavigator';

/**
 * The "Account" destination inside MainNavigator: renders the guest sign-in
 * flow or the diner's own profile stack, chosen reactively from session
 * status. Only ever mounted when root status is 'none' or 'diner' (see
 * RootNavigator) - any other value here is therefore always 'none' in
 * practice, so the check only needs to single out 'diner'.
 *
 * Because MainNavigator itself never unmounts across a sign-in (both 'none'
 * and 'diner' route to it), completing sign-in here swaps this screen's
 * content from AuthNavigator to DinerNavigator in place - no navigation
 * reset required.
 */
export function AccountEntryScreen() {
  const {status} = useSession();
  return status === 'diner' ? <DinerNavigator /> : <AuthNavigator />;
}
