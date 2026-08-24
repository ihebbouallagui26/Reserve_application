/**
 * Shared width caps so content screens don't stretch edge-to-edge on a
 * tablet - mirrors the cap AuthContainer already applies to auth forms.
 */
export const layout = {
  /** Profile/settings/list-style screens (Profile, Notifications, Staff...). */
  contentMaxWidth: 640,
  /** Narrow single-column forms outside the auth flow (Partner login). */
  formMaxWidth: 480,
} as const;
