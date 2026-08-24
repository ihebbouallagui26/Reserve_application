module.exports = {
  preset: '@react-native/jest-preset',
  // React Navigation and its native peers ship ESM syntax that Jest's default
  // transformIgnorePatterns (which excludes all of node_modules) would skip.
  transformIgnorePatterns: [
    'node_modules/(?!(react-native|@react-native|@react-navigation|react-native-screens|react-native-safe-area-context)/)',
  ],
};
