import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {
  clearDinerSession,
  clearPartnerSession,
  getDinerSession,
  getPartnerSession,
  saveDinerSession,
  savePartnerSession,
} from '../storage/session';
import type {DinerSession, PartnerSession} from '../storage/session';
import type {DinerAccount} from '../models/diner';

export type SessionStatus = 'loading' | 'none' | 'diner' | 'partner';

interface SessionContextValue {
  status: SessionStatus;
  dinerSession: DinerSession | null;
  partnerSession: PartnerSession | null;
  signInDiner: (session: DinerSession) => Promise<void>;
  signOutDiner: () => Promise<void>;
  signInPartner: (session: PartnerSession) => Promise<void>;
  signOutPartner: () => Promise<void>;
  /** Keeps the persisted+in-memory diner session's account in sync after GET/POST /me. */
  updateDinerAccount: (account: DinerAccount) => Promise<void>;
}

const SessionContext = createContext<SessionContextValue | undefined>(undefined);

/**
 * Reactive layer on top of storage/session.ts (Keychain). RootNavigator reads
 * `status` from here to decide which branch to render - signing in/out
 * anywhere in the app therefore navigates automatically, without screens
 * needing to call navigation.reset() themselves.
 *
 * Diner and partner are mutually exclusive by construction: signing into one
 * always clears the other, both in memory and in Keychain, so the two
 * sessions can never coexist even if a previous state was left inconsistent.
 */
export function SessionProvider({children}: {children: React.ReactNode}) {
  const [status, setStatus] = useState<SessionStatus>('loading');
  const [dinerSession, setDinerSessionState] = useState<DinerSession | null>(null);
  const [partnerSession, setPartnerSessionState] = useState<PartnerSession | null>(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      const [diner, partner] = await Promise.all([getDinerSession(), getPartnerSession()]);
      if (cancelled) {
        return;
      }
      if (diner) {
        setDinerSessionState(diner);
        setStatus('diner');
      } else if (partner) {
        setPartnerSessionState(partner);
        setStatus('partner');
      } else {
        setStatus('none');
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const signInDiner = useCallback(async (session: DinerSession) => {
    await Promise.all([saveDinerSession(session), clearPartnerSession()]);
    setPartnerSessionState(null);
    setDinerSessionState(session);
    setStatus('diner');
  }, []);

  const signOutDiner = useCallback(async () => {
    await clearDinerSession();
    setDinerSessionState(null);
    setStatus('none');
  }, []);

  const updateDinerAccount = useCallback(async (account: DinerAccount) => {
    setDinerSessionState(prev => {
      if (!prev) {
        return prev;
      }
      const next: DinerSession = {...prev, account};
      saveDinerSession(next).catch(() => {});
      return next;
    });
  }, []);

  const signInPartner = useCallback(async (session: PartnerSession) => {
    await Promise.all([savePartnerSession(session), clearDinerSession()]);
    setDinerSessionState(null);
    setPartnerSessionState(session);
    setStatus('partner');
  }, []);

  const signOutPartner = useCallback(async () => {
    await clearPartnerSession();
    setPartnerSessionState(null);
    setStatus('none');
  }, []);

  const value = useMemo<SessionContextValue>(
    () => ({
      status,
      dinerSession,
      partnerSession,
      signInDiner,
      signOutDiner,
      signInPartner,
      signOutPartner,
      updateDinerAccount,
    }),
    [
      status,
      dinerSession,
      partnerSession,
      signInDiner,
      signOutDiner,
      signInPartner,
      signOutPartner,
      updateDinerAccount,
    ],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionContextValue {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
}
