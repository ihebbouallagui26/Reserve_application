import {apiRequest} from './client';
import {getDinerToken} from '../../storage/session';
import type {ApiError} from '../../models/errors';
import type {
  AuthResponse,
  DinerAccount,
  DinerPreferences,
  PhoneStartResponse,
  PhoneVerifyResponse,
} from '../../models/diner';
import type {
  LoginRequest,
  PhoneStartRequest,
  PhoneVerifyRequest,
  RegisterRequest,
  SocialLoginRequest,
  UpdatePreferencesRequest,
  UpdateProfileRequest,
} from '../../models/requests';

async function requireDinerToken(): Promise<string> {
  const token = await getDinerToken();
  if (!token) {
    const error: ApiError = {
      kind: 'unauthorized',
      status: null,
      message: 'Vous devez être connecté.',
    };
    throw error;
  }
  return token;
}

/** All calls relative to identity-service, via the Gateway (/api/identity/**). */
export const identityApi = {
  register: (payload: RegisterRequest) =>
    apiRequest<AuthResponse>('/api/identity/register', {method: 'POST', body: payload}),

  login: (payload: LoginRequest) =>
    apiRequest<AuthResponse>('/api/identity/login', {method: 'POST', body: payload}),

  socialLogin: (payload: SocialLoginRequest) =>
    apiRequest<AuthResponse>('/api/identity/social', {method: 'POST', body: payload}),

  startPhoneVerification: (payload: PhoneStartRequest) =>
    apiRequest<PhoneStartResponse>('/api/identity/phone/start', {
      method: 'POST',
      body: payload,
    }),

  verifyPhone: (payload: PhoneVerifyRequest) =>
    apiRequest<PhoneVerifyResponse>('/api/identity/phone/verify', {
      method: 'POST',
      body: payload,
    }),

  getMe: async () =>
    apiRequest<DinerAccount>('/api/identity/me', {token: await requireDinerToken()}),

  updateMe: async (payload: UpdateProfileRequest) =>
    apiRequest<DinerAccount>('/api/identity/me', {
      method: 'POST',
      body: payload,
      token: await requireDinerToken(),
    }),

  getPreferences: async () =>
    apiRequest<DinerPreferences>('/api/identity/preferences', {
      token: await requireDinerToken(),
    }),

  updatePreferences: async (payload: UpdatePreferencesRequest) =>
    apiRequest<DinerPreferences>('/api/identity/preferences', {
      method: 'POST',
      body: payload,
      token: await requireDinerToken(),
    }),
};
