import {apiRequest} from './client';
import {getPartnerToken} from '../../storage/session';
import type {ApiError} from '../../models/errors';
import type {PartnerLoginResponse, PartnerMeResponse, PartnerUser} from '../../models/partner';
import type {CreateStaffRequest, PartnerLoginRequest} from '../../models/requests';

async function requirePartnerToken(): Promise<string> {
  const token = await getPartnerToken();
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

/** All calls relative to identity-service, via the Gateway (/api/identity/partner/**). */
export const partnerApi = {
  login: (payload: PartnerLoginRequest) =>
    apiRequest<PartnerLoginResponse>('/api/identity/partner/login', {
      method: 'POST',
      body: payload,
    }),

  getMe: async () =>
    apiRequest<PartnerMeResponse>('/api/identity/partner/me', {
      token: await requirePartnerToken(),
    }),

  getStaff: async () =>
    apiRequest<PartnerUser[]>('/api/identity/partner/staff', {
      token: await requirePartnerToken(),
    }),

  createStaff: async (payload: CreateStaffRequest) =>
    apiRequest<PartnerUser>('/api/identity/partner/staff', {
      method: 'POST',
      body: payload,
      token: await requirePartnerToken(),
    }),

  deleteStaff: async (id: string) =>
    apiRequest<string>(`/api/identity/partner/staff/${encodeURIComponent(id)}`, {
      method: 'DELETE',
      token: await requirePartnerToken(),
    }),
};
