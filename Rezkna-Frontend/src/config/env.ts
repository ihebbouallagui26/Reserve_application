import { Platform } from 'react-native';

/**
 * REZKNA API Gateway base URL. The mobile app talks to the Gateway only -
 * it must never call a microservice port (4001-4006) directly.
 *
 * Sprint 0 only wires up local development; staging/production base URLs
 * belong here once those environments actually exist.
 *
 * Local development notes:
 * - Android emulator: 10.0.2.2 is a special alias the emulator maps to the
 *   host machine's localhost - this is what is used below by default.
 * - iOS simulator: localhost works directly, since the simulator shares the
 *   host machine's network namespace.
 * - Physical device (Android or iOS): neither of the above reaches your dev
 *   machine. Replace the value below with your machine's LAN IP, e.g.
 *   http://192.168.1.42:4000 (find it via `ipconfig` on Windows or
 *   `ifconfig`/`ip addr` on macOS/Linux), and make sure the device is on the
 *   same network as the machine running docker compose.
 */
export const GATEWAY_BASE_URL: string = Platform.select({
  android: 'http://10.0.2.2:4000',
  default: 'http://localhost:4000',
});
