/**
 * Shared width caps so content screens don't stretch edge-to-edge on a
 * tablet - mirrors the cap AuthContainer already applies to auth forms.
 */
export const layout = {
  /** Profile/settings/list-style screens (Profile, Notifications, Staff...). */
  contentMaxWidth: 640,
  /** Narrow single-column forms outside the auth flow (Partner login). */
  formMaxWidth: 480,
  /** Restaurant detail/menu (Phase 11): contentMaxWidth (640) reads as
   * cramped on tablet for a premium, SevenRooms-style detail page - wider
   * on purpose, distinct from the form/list caps above. */
  detailMaxWidth: 900,
} as const;
