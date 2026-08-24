/**
 * "Notifications" here maps to GET/POST /api/identity/preferences
 * (notifySms/notifyEmail/marketingOptIn). It is named "Notifications" rather
 * than "Preferences" specifically to avoid confusion with DinerAccount's own
 * `preferences` free-text field, edited on the Profile/EditProfile screens -
 * two distinct backend concepts that happen to share a word.
 */
export type DinerStackParamList = {
  Profile: undefined;
  EditProfile: undefined;
  Notifications: undefined;
};
