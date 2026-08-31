/**
 * Root-level navigation. Session-gated (see RootNavigator): a partner
 * session reaches "Partner" only; a diner session and no session at all
 * both reach "Main" (MainNavigator), since Explore must be reachable
 * without authentication - AccountEntryScreen, nested inside MainNavigator,
 * is what actually distinguishes guest from diner (see its own doc comment).
 */
export type RootStackParamList = {
  Main: undefined;
  Partner: undefined;
};
