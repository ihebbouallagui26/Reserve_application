export interface SocialCredential {
  /** The provider's own token/credential - identity-service validates it server-side. */
  token: string;
  name?: string;
}

export interface SocialSignInProvider {
  readonly isConfigured: boolean;
  signIn(): Promise<SocialCredential>;
}
