/**
 * Mirrors identity-service's PartnerUserView / PartnerLoginResponse /
 * PartnerMeResponse exactly. Deliberately does NOT include a `Property`
 * object or `restaurants[].name` - identity-service never returns either.
 */
export type PartnerRole = 'OWNER' | 'HOST';

export interface PartnerUser {
  id: string;
  email: string;
  propertyId: string;
  name: string;
  role: PartnerRole;
  active: boolean;
  createdAt: string;
  lastLogin: string | null;
}

export interface PartnerRestaurantRef {
  propertyId: string;
  role: PartnerRole;
}

/** POST /partner/login response data. */
export interface PartnerLoginResponse {
  token: string;
  user: PartnerUser;
  propertyId: string;
  restaurants: PartnerRestaurantRef[];
}

/** GET /partner/me response data. */
export interface PartnerMeResponse {
  user: PartnerUser;
  propertyId: string;
}
