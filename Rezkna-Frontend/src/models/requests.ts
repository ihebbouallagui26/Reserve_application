import type {PartnerRole} from './partner';

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
  city?: string;
  preferences?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SocialLoginRequest {
  provider: 'google' | 'facebook';
  token: string;
  name?: string;
}

export interface PhoneStartRequest {
  phone: string;
}

export interface PhoneVerifyRequest {
  phone: string;
  code: string;
  name?: string;
}

export interface UpdateProfileRequest {
  name?: string;
  phone?: string;
  city?: string;
  preferences?: string;
  birthday?: string;
  allergies?: string[];
  diets?: string[];
}

export interface UpdatePreferencesRequest {
  notifySms: boolean;
  notifyEmail: boolean;
  marketingOptIn: boolean;
}

export interface PartnerLoginRequest {
  email: string;
  password: string;
  propertyId?: string;
}

export interface CreateStaffRequest {
  name: string;
  email: string;
  password: string;
  role?: PartnerRole;
}
