export type AuthStackParamList = {
  Welcome: undefined;
  Landing: undefined;
  Login: undefined;
  Register: undefined;
  Phone: undefined;
  Otp: {phone: string};
  /**
   * Lives in features/partner/screens (domain-organized) but is registered
   * here: it is reachable pre-session, exactly like the diner auth screens,
   * from a single shared "no session yet" entry point.
   */
  PartnerLogin: undefined;
};
