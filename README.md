# Rezkna

Monorepo: 7 Spring Boot microservices behind an API Gateway, plus a React Native
mobile app that talks to the Gateway only.

## Project structure

```
Rezkna/
├── Rezkna-Backend/
│   ├── common-lib/           shared API response format, error handling, JWT validation
│   ├── gateway-service/      :4000 - single entry point, routes to the 6 services below
│   ├── identity-service/     :4001
│   ├── restaurant-service/   :4002
│   ├── reservation-service/  :4003
│   ├── loyalty-service/      :4004
│   ├── engagement-service/   :4005
│   └── media-service/        :4006
├── Rezkna-Frontend/          React Native + TypeScript mobile app
├── docker-compose.yml
└── .env.example
```

Each service owns its own MongoDB Atlas database (`identity-db`, `restaurant-db`, ...);
no service accesses another's database directly. Redis is shared infrastructure.

## Backend

### Requirements

- Java 21, Maven
- Docker + Docker Compose
- A MongoDB Atlas cluster (this project does not use a local MongoDB container)

### Setup

```
cp .env.example .env
# fill in MONGODB_ATLAS_USER, MONGODB_ATLAS_PASSWORD, MONGODB_ATLAS_HOST, JWT_SECRET
```

`JWT_SECRET` must be at least 32 bytes (HS256 requirement) - e.g. `openssl rand -base64 48`.
Make sure your current IP is allow-listed in Atlas's Network Access settings, or
services will fail to connect.

### Run

```
docker compose up --build -d
docker compose ps          # all 7 services + redis should be healthy
curl http://localhost:4000/api/identity/ping
```

Expected response: `{"ok":true,"message":"Request successful","data":"pong"}`

### Ports

| Service | Port |
|---|---|
| gateway-service | 4000 |
| identity-service | 4001 |
| restaurant-service | 4002 |
| reservation-service | 4003 |
| loyalty-service | 4004 |
| engagement-service | 4005 |
| media-service | 4006 |

The mobile app, and any external client, talks to **4000 only**. The other 6 ports
are for direct service testing during development.

## Mobile (Rezkna-Frontend)

### Requirements

- Node >= 22.11
- Java 21 (Android builds)
- Android Studio + Android SDK, for Android development
- macOS + Xcode, for iOS development (not available on Windows)

### Setup

```
cd Rezkna-Frontend
npm install
```

### Run

```
npm start                 # Metro bundler
npm run android            # separate terminal, needs an emulator running or a device connected
```

### Connecting to the backend

The app's Gateway base URL is configured in `Rezkna-Frontend/src/config/env.ts`.
Local development URLs differ by target:

| Target | Gateway URL |
|---|---|
| Android emulator | `http://10.0.2.2:4000` (emulator's alias for the host machine) |
| iOS simulator | `http://localhost:4000` (simulator shares the host's network) |
| Physical device | `http://<host machine LAN IP>:4000` - find it via `ipconfig` (Windows) or `ifconfig`/`ip addr` (macOS/Linux); the device must be on the same network |

`env.ts` defaults to the emulator/simulator cases automatically based on platform; edit
it directly for physical device testing.

### Connectivity check

The app's home screen is a Sprint 0 technical verification screen (not real UI yet):
pressing **Test Connection** calls `GET /api/identity/ping` through the Gateway and
shows the result. This proves Mobile → Gateway → backend → `ApiResponse<T>` end-to-end
without needing any authentication (Sprint 0 has no login yet).

## Known Issues / Environment Notes

### Android native build (Windows)

The React Native Android native build currently fails on this Windows development
machine. This has been investigated and is classified as an environment/toolchain
compatibility issue, not an identified REZKNA application-code defect:

- The failure is related to the NDK 27.1.12297006 / Windows LLD native toolchain.
- It was reproduced after cleaning all native build artifacts.
- Removing `react-native-safe-area-context` did not resolve it.
- Setting `newArchEnabled=false` did not resolve it.
- The failure moved into React Native's own core native code once that dependency
  was removed, confirming it is not specific to any one dependency.
- The NDK's `libc++` static archives were inspected directly and are intact, not
  corrupted.

**Verified working independently of the above:**

- TypeScript compilation passes (`npx tsc --noEmit`)
- ESLint passes
- Metro bundling passes
- The Gateway-only API architecture is verified

**Not currently testable on this machine:**

- Android app launch - no emulator or physical Android device is currently available
- iOS - not testable on Windows; iOS builds require macOS and Xcode

### Disk space

The `C:` drive on this development machine is currently nearly full. Freeing disk
space may be required before attempting further native Android builds.
