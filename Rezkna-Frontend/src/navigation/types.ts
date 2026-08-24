/**
 * Root-level navigation. Each branch is session-gated (see RootNavigator) -
 * a diner session can only ever reach "Diner", a partner session only
 * "Partner", and no session only "Auth". Nested param lists for the real
 * screens inside each branch are added as those screens are built
 * (Phase 2: AuthStackParamList, Phase 3: DinerStackParamList, Phase 4-5:
 * PartnerStackParamList).
 */
export type RootStackParamList = {
  Auth: undefined;
  Diner: undefined;
  Partner: undefined;
};
