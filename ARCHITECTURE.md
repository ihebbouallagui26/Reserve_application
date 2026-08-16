# Rezkna — Architecture

Technical reference for the Rezkna microservices rebuild. Written after Sprint 1
(identity-service) against the real, deployed code — not a design aspiration.

## Technology versions

- Java: 21
- Spring Boot: 3.5.16
- jjwt: 0.13.0

## 1. System overview

Rezkna is being rebuilt from a Spring Boot monolith (`com.lightbanana.reserve`) into
independent microservices, one per business capability, behind a single API Gateway.
The mobile app (React Native) talks to the Gateway only — it never calls a
microservice port directly.

```
Mobile app (React Native)
        │
        ▼
 gateway-service  :4000   (Spring Cloud Gateway, WebFlux)
        │  routes /api/<service>/** → <service>:<port>, StripPrefix=2
        ├──► identity-service     :4001   (Sprint 1 — implemented)
        ├──► restaurant-service   :4002   (skeleton — Sprint 2/3)
        ├──► reservation-service  :4003   (skeleton — Sprint 4/5)
        ├──► loyalty-service      :4004   (skeleton — Sprint 7)
        ├──► engagement-service   :4005   (skeleton — Sprint 8/9/10)
        └──► media-service        :4006   (skeleton — Sprint 11)

 Redis (shared infra, not yet used by any service's business logic)
```

Each service owns its own MongoDB Atlas database (`identity-db`, `restaurant-db`, ...).
No service reads or writes another service's database. Inter-service communication
(when it starts, from Sprint 4 onward) is synchronous REST, per the sprint plan.

## 2. Services

| Service | Port | Status | Responsibility |
|---|---|---|---|
| gateway-service | 4000 | Sprint 0 | Single entry point, routing, JWT pass-through (no validation) |
| identity-service | 4001 | **Sprint 1** | Diner accounts, phone OTP, diner/partner JWT, partner staff management |
| restaurant-service | 4002 | skeleton | Property, menu, floor plan, team, public search |
| reservation-service | 4003 | skeleton | Bookings, availability, check-in, waitlist |
| loyalty-service | 4004 | skeleton | Points, tiers, gift cards, wallet |
| engagement-service | 4005 | skeleton | CRM, journeys, messaging, SMS/email, reviews |
| media-service | 4006 | skeleton | Image upload and processing |

## 3. common-lib

Shared module every service depends on. Because Spring Boot's component scan only
covers a service's own base package (`com.rezkna.<service>`), `common-lib`'s beans
(living in `com.rezkna.common`) are registered via Spring Boot **auto-configuration**
(`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`),
not `@Component` scanning — a `@Component` there would silently never be picked up by
a consuming service.

- `ApiResponse<T>` — the response envelope every endpoint returns: `{ok, message, data}`.
- `GlobalExceptionHandler` — maps `ResourceNotFoundException`→404, `BadRequestException`
  /`IllegalArgumentException`/validation failures→400, anything else→500.
- `JwtService` — validates a JWT's signature, structure and expiration, and can read its
  claims. Deliberately generic: it does not know about token *types*. Issuing tokens is
  each service's own job (see §5).
- `JwtAuthenticationFilter` — reads `Authorization: Bearer <token>`, and if
  `JwtService` accepts it, populates `SecurityContext` with the token's `sub` as
  principal (no authorities). An invalid/missing token simply leaves the request
  unauthenticated; whether that matters is up to each route's own security rules.
- `JwtAuthenticationEntryPoint` / `RestAccessDeniedHandler` — produce 401/403 in the
  standard `ApiResponse` envelope for authentication/authorization failures raised
  through Spring Security's filter chain.

## 4. identity-service

### 4.1 Responsibility

Everything about *who is making a request*: diner accounts (email/password, phone OTP,
Google/Facebook social login) and owner/host accounts (login only — account creation is
out of scope, see §4.7). It issues and validates the two JWT types the rest of the
system will eventually rely on. It owns exactly one database, `identity-db`, and never
reads or writes another service's data — in particular, it has no notion of `Property`
beyond an opaque `propertyId` string.

### 4.2 Two-layer JWT validation

- **Layer 1 — common-lib's `JwtService`**: "is this JWT cryptographically valid?"
  (signature, structure, expiration). Shared by every service.
- **Layer 2 — `DinerTokenService` / `PartnerTokenService`** (identity-service only):
  "is this JWT the *right type* for this endpoint?" A structurally valid partner JWT
  presented on a diner-only route (or vice versa) is rejected. This second layer
  re-reads the raw `Authorization` header directly in the controller (via
  `requireAccountId(header)` / `requireClaims(header)`) rather than relying on Spring
  Security roles/authorities, since `common-lib`'s filter does not expose the `typ`
  claim as an authority.

Both failure modes — no token at all (rejected by Spring Security's filter chain before
reaching a controller) and a wrong-type token (rejected explicitly inside a controller)
— return the identical `401 {"ok":false,"message":"Missing or invalid authentication
token","data":null}`, by design: a client cannot distinguish "you're not logged in" from
"you're logged in as the wrong kind of user" from the response alone.

### 4.3 Diner JWT

Claims: `sub` = accountId, `typ` = `"diner"`, `email`. Signed HS256/384/512 (algorithm
auto-selected by key length) with the shared `JWT_SECRET`. Expiration configurable via
`identity.jwt.diner-token-days` (default 60).

### 4.4 Partner JWT

Claims: `sub` = userId, `typ` = `"partner"`, `pid` = propertyId, `role` (`OWNER` or
`HOST`), `email`. Same secret. Expiration via `identity.jwt.partner-token-days`
(default 14).

### 4.5 API

All paths below are relative to identity-service; the Gateway exposes them at
`/api/identity/**` (`StripPrefix=2`).

**Diner — public:**
- `POST /register`, `POST /login`, `POST /social` — return `{token, account}`
- `POST /phone/start` — request a 6-digit OTP (never returns the code)
- `POST /phone/verify` — returns `{token, account, isNew}`

**Diner — requires a diner JWT:**
- `GET /me`, `POST /me` — read/update profile
- `GET /preferences`, `POST /preferences` — `notifySms`/`notifyEmail`/`marketingOptIn`

**Partner — public:**
- `POST /partner/login` — `{token, user, propertyId, restaurants: [{propertyId, role}]}`.
  Never returns a `Property` object — restaurant-service data is out of reach by design.
  If an account matches more than one property and `propertyId` is not supplied, the
  request is rejected with `409` rather than guessing which property was meant.

**Partner — requires a partner JWT:**
- `GET /partner/me` — `{user, propertyId}`, no `Property` object
- `GET /partner/staff` — staff accounts for the caller's property
- `POST /partner/staff`, `DELETE /partner/staff/{id}` — **OWNER only** (`HOST` gets 403);
  scoped strictly to the caller's own `propertyId` — a staff id belonging to another
  property returns 404, not 403, to avoid confirming it exists elsewhere. `DELETE`
  returns the deleted staff's `id` as `data` on success (real deletion, not a soft
  deactivation - `active` exists on the model for the login check, but no endpoint
  toggles it in Sprint 1)

### 4.6 MongoDB — `identity-db`

| Collection | Document | Notable indexes |
|---|---|---|
| `accounts` | `DinerAccount` | `email` unique + **sparse** (many diners have no email — phone-only or social accounts without an email scope — so uniqueness must not apply to absent values), `phoneTail` indexed |
| `phone_verification_codes` | `PhoneVerificationCode` | `phone` indexed, `expiresAt` **TTL** index (`expireAfter = "0s"` — expires exactly at the stored deadline) |
| `partner_users` | `PartnerUser` | compound unique index on `(email, propertyId)` — the same email can manage several properties, but not the same property twice |

`spring.data.mongodb.auto-index-creation: true` is required for any `@Indexed`
annotation to actually take effect — without it the annotations are inert and no
index is created.

### 4.7 OTP

`SecureRandom`-generated 6-digit code (`000000`–`999999`, leading zeros preserved),
stored only as a BCrypt hash — never in clear text, never logged. 10-minute expiry
(enforced both by the Mongo TTL index and by an explicit application-level check on
verify, so an over-limit clock skew can't extend a code's real life). Maximum 5 wrong
attempts, after which the code is deleted and the endpoint returns `429`. Every new
`/phone/start` call deletes any previous code for that phone before creating a new one
— only one code can be active per phone at a time.

`SmsSender` is a generic `send(phone, message)` interface (not OTP-specific, so it can
serve other message types later without changing its shape); `LoggingSmsSender` is the
only implementation in Sprint 1 (development/test stand-in — logs a masked-phone
confirmation, e.g. `OTP sent to *******8776`, and never the `message` content it was
given, regardless of what that content is). A real provider (TunisieSMS or similar) is
engagement-service's concern in a later sprint and can implement `SmsSender` without
any caller needing to change.

### 4.8 Social login security

The old monolith's `SocialVerifier` checked that a social token was *authentic* but not
that it was issued *for this application* — a token-confusion gap. identity-service
closes it for both providers:

- **Google**: the ID token is verified against Google's `tokeninfo` endpoint, then the
  response's `aud` claim is compared to `GOOGLE_CLIENT_ID`. A token that is valid but
  was issued for a different Google client is rejected.
- **Facebook**: the user token is checked via `/debug_token` using an App Access Token
  built from `FACEBOOK_APP_ID`/`FACEBOOK_APP_SECRET`. `data.is_valid` and
  `data.app_id == FACEBOOK_APP_ID` must both hold before `/me` is ever called for the
  profile. A token that merely resolves a profile is not treated as proof of anything.

Both verifiers split the outbound HTTP call from the accept/reject decision
(`fetchTokenInfo`/`decide` for Google, `fetchDebugToken`/`validate`/`fetchProfile` for
Facebook) specifically so the decision logic has unit tests that don't depend on the
network. `GOOGLE_CLIENT_ID`, `FACEBOOK_APP_ID`, `FACEBOOK_APP_SECRET` are read from
environment variables only, never logged, never hardcoded; an unconfigured value fails
closed (rejects) rather than skipping the check.

### 4.9 Security summary

- Passwords and OTP codes: BCrypt, never stored or logged in clear text.
- No endpoint ever returns `passwordHash` or `codeHash` — every response goes through
  an explicit view/DTO (`DinerAccountView`, `PartnerUserView`), never the raw document.
- Stateless (`SessionCreationPolicy.STATELESS`), CSRF disabled — consistent with Sprint
  0's rationale (no cookie-based session exists for CSRF to protect).
- `IdentityExceptionHandler` (local to identity-service) explicitly handles
  `AuthenticationException`→401 and `AccessDeniedException`→403. This is necessary, not
  cosmetic: Spring MVC's exception resolution runs *inside* the servlet filter chain,
  so `common-lib`'s catch-all `GlobalExceptionHandler` would otherwise intercept a
  security exception thrown from a controller (e.g. a wrong-typed JWT) before Spring
  Security's own `JwtAuthenticationEntryPoint`/`RestAccessDeniedHandler` ever saw it,
  turning what should be a 401/403 into a 500. The two local handlers reproduce the
  exact same response bodies as the filter-level handlers so the two paths are
  indistinguishable to a client. `ConflictException`(409)/`TooManyAttemptsException`
  (429) are also handled locally since `common-lib` doesn't cover those codes.

### 4.10 What Sprint 1 deliberately excludes

- `POST /partner/signup` and any `Property` creation/mutation — a restaurant belongs to
  restaurant-service, which doesn't exist yet; identity-service must never write into
  another service's data.
- Door tokens/sessions/invites (`/api/door/**`) — depends on restaurant-service too;
  explicitly deferred to Sprint 6 per the sprint plan.
- A real SMS provider — behind `SmsSender`, to be supplied by engagement-service later.
- Apple Sign-In, `X-Staff-Key`, the legacy `/api/reserve/**` JWT system (tied to the old
  monolith's external "LightBanana auth service") — none of these are part of the
  documented Rezkna auth model.

## 5. Testing

Sprint 1 is the first place the project has real automated tests.

- **Unit tests**: `DinerTokenService`/`PartnerTokenService` (claims, expiration,
  cross-type rejection, bad signature), `PhoneNormalizer`, `OtpGenerator` (format,
  leading zeros preserved), `GoogleTokenVerifier`/`FacebookTokenVerifier` decision logic
  (audience/app-id acceptance and rejection, provider failures), `LoggingSmsSender`
  (message content, including the OTP code, never reaches the log line - only the
  masked phone does) — all without touching the network or a database.
- **Controller tests** (`@WebMvcTest` + `MockMvc` + Mockito): status codes, validation,
  the response envelope, public vs. protected routes, missing/wrong-type tokens,
  OWNER-vs-HOST authorization. Each test class explicitly `@Import`s `SecurityConfig`
  plus `common-lib`'s `JwtAutoConfiguration`/`CommonLibAutoConfiguration`, since
  `@WebMvcTest`'s slice does not load either by default.
- **Integration tests** (Testcontainers `mongo:7`, real MongoDB — test-only, the
  running application always connects to Atlas): unique/sparse email index, the
  composite `(email, propertyId)` unique index, the OTP TTL index configuration,
  repository lookups. Named `*IntegrationTest` (not `*IT`) so plain `mvn test` runs
  them — no `maven-failsafe-plugin`/separate `verify` phase was introduced.

## 6. Git workflow

Monorepo, `main` (stable) / `develop` (integration) / `feature/<service>-<topic>`
branches, `sprint-N` tags at the close of each sprint, `type(service): description`
commit messages. See the sprint plan for the full 14-sprint roadmap.
