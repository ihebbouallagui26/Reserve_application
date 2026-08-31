import {Platform, PermissionsAndroid} from 'react-native';
import Geolocation from '@react-native-community/geolocation';

export type LocationResult =
  | {status: 'success'; latitude: number; longitude: number}
  | {status: 'denied'}
  | {status: 'unavailable'}
  | {status: 'timeout'}
  | {status: 'error'};

const TIMEOUT_MS = 10000;

/**
 * Android requires an explicit runtime request; iOS shows its own system
 * prompt the first time Geolocation.getCurrentPosition is actually called,
 * so nothing extra is needed there beyond the Info.plist usage description
 * (already set - see Checkpoint 8/9 notes on Info.plist).
 */
async function requestAndroidPermission(): Promise<boolean> {
  if (Platform.OS !== 'android') {
    return true;
  }
  try {
    const result = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION, {
      title: 'Localisation',
      message: 'REZKNA utilise votre position pour vous montrer les restaurants à proximité.',
      buttonPositive: 'Autoriser',
      buttonNegative: 'Refuser',
    });
    return result === PermissionsAndroid.RESULTS.GRANTED;
  } catch {
    return false;
  }
}

/**
 * One-shot position lookup - never watchPosition, no continuous tracking,
 * no listener to leak or clean up. Never throws: every outcome (denied,
 * GPS unavailable, timeout, unexpected error) resolves to a LocationResult
 * a caller can branch on, so a screen can always fall back to the
 * unfiltered restaurant list rather than getting stuck.
 */
export function getCurrentPosition(): Promise<LocationResult> {
  return requestAndroidPermission().then(granted => {
    if (!granted) {
      return {status: 'denied'};
    }
    return new Promise<LocationResult>(resolve => {
      Geolocation.getCurrentPosition(
        position => {
          resolve({
            status: 'success',
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          });
        },
        error => {
          // Standard Geolocation error codes: 1 permission denied (can still
          // happen on iOS even after our own Android-only pre-check above),
          // 2 position unavailable (GPS off/no fix), 3 timeout.
          if (error.code === 1) {
            resolve({status: 'denied'});
          } else if (error.code === 2) {
            resolve({status: 'unavailable'});
          } else if (error.code === 3) {
            resolve({status: 'timeout'});
          } else {
            resolve({status: 'error'});
          }
        },
        {enableHighAccuracy: false, timeout: TIMEOUT_MS, maximumAge: 60000},
      );
    });
  });
}
