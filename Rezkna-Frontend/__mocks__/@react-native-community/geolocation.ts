/**
 * Manual mock for the native geolocation module - importing the real package
 * touches a native event emitter that Jest's environment has no native
 * module for (no device/emulator backs the test run), so any file that
 * transitively imports locationService.ts would otherwise crash the whole
 * suite with a "doesn't seem to be linked" invariant, even in tests that
 * never call getCurrentPosition themselves (e.g. the App smoke test).
 * Individual test files that care about specific location outcomes mock
 * locationService.ts directly instead of this lower-level module.
 */
const Geolocation = {
  getCurrentPosition: jest.fn(),
  watchPosition: jest.fn(),
  clearWatch: jest.fn(),
  stopObserving: jest.fn(),
  requestAuthorization: jest.fn(),
  setRNConfiguration: jest.fn(),
};

export default Geolocation;
