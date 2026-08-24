/**
 * Mirrors identity-service's DinerAccountView exactly (see
 * Rezkna-Backend/identity-service/.../account/DinerAccountView.java).
 * Never add a field here that the backend does not actually return.
 */
export interface DinerAccount {
  id: string;
  email: string | null;
  name: string | null;
  phone: string | null;
  city: string | null;
  preferences: string | null;
  provider: string | null;
  avatarUrl: string | null;
  notifySms: boolean;
  notifyEmail: boolean;
  marketingOptIn: boolean;
  allergies: string[];
  diets: string[];
  birthday: string | null;
  createdAt: string;
  lastLogin: string | null;
}

/** POST /register, /login, /social response data. */
export interface AuthResponse {
  token: string;
  account: DinerAccount;
}

/** POST /phone/start response data. */
export interface PhoneStartResponse {
  phone: string;
  expiresInMinutes: number;
}

/** POST /phone/verify response data. */
export interface PhoneVerifyResponse {
  token: string;
  account: DinerAccount;
  isNew: boolean;
}

/** GET/POST /preferences response data. */
export interface DinerPreferences {
  notifySms: boolean;
  notifyEmail: boolean;
  marketingOptIn: boolean;
}
