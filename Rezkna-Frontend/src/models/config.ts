/**
 * Mirrors identity-service's PublicConfigResponse exactly (see
 * Rezkna-Backend/identity-service/.../account/PublicConfigResponse.java).
 */
export interface PublicConfigResponse {
  googleClientId: string;
  facebookAppId: string;
}
