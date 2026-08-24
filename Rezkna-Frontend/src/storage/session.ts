import * as Keychain from 'react-native-keychain';
import type {DinerAccount} from '../models/diner';
import type {PartnerUser} from '../models/partner';

/**
 * Diner and partner sessions are stored under two entirely separate Keychain
 * services (native-level namespaces), not just separate JS objects - it is
 * structurally impossible for one to overwrite or leak into the other.
 */
const DINER_SERVICE = 'com.rezkna.frontend.session.diner';
const PARTNER_SERVICE = 'com.rezkna.frontend.session.partner';

export interface DinerSession {
  token: string;
  account: DinerAccount;
}

export interface PartnerSession {
  token: string;
  user: PartnerUser;
  propertyId: string;
}

function parseSession<T>(password: string): T | null {
  try {
    return JSON.parse(password) as T;
  } catch {
    return null;
  }
}

// --- Diner ---

export async function saveDinerSession(session: DinerSession): Promise<void> {
  await Keychain.setGenericPassword('diner', JSON.stringify(session), {
    service: DINER_SERVICE,
  });
}

export async function getDinerSession(): Promise<DinerSession | null> {
  const result = await Keychain.getGenericPassword({service: DINER_SERVICE});
  if (!result) {
    return null;
  }
  return parseSession<DinerSession>(result.password);
}

export async function getDinerToken(): Promise<string | null> {
  const session = await getDinerSession();
  return session?.token ?? null;
}

export async function clearDinerSession(): Promise<void> {
  await Keychain.resetGenericPassword({service: DINER_SERVICE});
}

// --- Partner ---

export async function savePartnerSession(session: PartnerSession): Promise<void> {
  await Keychain.setGenericPassword('partner', JSON.stringify(session), {
    service: PARTNER_SERVICE,
  });
}

export async function getPartnerSession(): Promise<PartnerSession | null> {
  const result = await Keychain.getGenericPassword({service: PARTNER_SERVICE});
  if (!result) {
    return null;
  }
  return parseSession<PartnerSession>(result.password);
}

export async function getPartnerToken(): Promise<string | null> {
  const session = await getPartnerSession();
  return session?.token ?? null;
}

export async function clearPartnerSession(): Promise<void> {
  await Keychain.resetGenericPassword({service: PARTNER_SERVICE});
}
