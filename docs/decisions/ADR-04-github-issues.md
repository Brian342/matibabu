# GitHub issue drafts for did:web authentication

Copy and paste each block into a new GitHub issue.

---

## Issue 1: Add Spring Security and JWT infrastructure to backend

**Title:** Add Spring Security and JWT infrastructure to backend

**Body:**

We are implementing decentralised authentication using `did:web` and Cardano wallet signatures (see `docs/decisions/ADR-02-did-web-authentication.md`).

This issue covers the security foundation:

- Add Spring Security to `backend/pom.xml`.
- Configure a security filter chain that:
  - Permits public access to `/api/auth/**`.
  - Requires a valid JWT for all other `/api/**` endpoints.
  - Disables default form login and HTTP basic auth.
- Add a JWT utility class for issuing and verifying access tokens.
- Store the JWT signing key outside source control (e.g., via environment variables or Spring configuration).

**Acceptance criteria:**

- `GET /api/patients/{id}` without a token returns `401 Unauthorized`.
- A request with a valid JWT in the `Authorization: Bearer <token>` header succeeds.
- `/api/auth/challenge` and `/api/auth/verify` remain publicly accessible.

**Related:** ADR-02

---

## Issue 2: Create DID resolver service

**Title:** Create DID resolver service for did:web

**Body:**

The backend needs to resolve `did:web` identifiers to DID documents over HTTPS.

Example:

```text
did:web:matibabu.co:patients:alice
```

resolves to:

```text
https://matibabu.co/.well-known/did.json
```

or, for paths:

```text
https://matibabu.co/patients/alice/did.json
```

**Requirements:**

- Create a `DidWebResolver` service in the application or infrastructure layer.
- Fetch the DID document using a standard HTTP client.
- Parse the JSON to extract public keys.
- Cache results briefly to avoid repeated network calls.
- Handle resolution failures with a clear exception.

**Acceptance criteria:**

- The service can resolve a `did:web` identifier and return a parsed DID document.
- Unit tests cover successful resolution and failure cases.

**Related:** ADR-02

---

## Issue 3: Create Cardano signature verifier

**Title:** Create Cardano signature verifier

**Body:**

The authentication flow requires the backend to verify a Cardano wallet signature against a challenge nonce.

The user signs the challenge with their utxos.dev wallet, so the signature follows Cardano message signing standards (CIP-8 / CIP-30). It is not a generic Ed25519 signature.

**Requirements:**

- Create a `SignatureVerifier` service or utility.
- Accept a signature, a message (the nonce), and a public key.
- Return a boolean indicating validity.
- Use a local Java library — no network calls to external APIs or microservices.

**Library options:**

| Option | Pros | Cons |
|---|---|---|
| `cardano-client-lib` (Java) | Native Java, no external service | Adds dependency, must keep updated |
| Bouncy Castle for raw Ed25519 | Lightweight | You must manually parse the CIP-8 structure |

**Recommendation:** start with `cardano-client-lib` if it supports CIP-8/CIP-30. Fall back to Bouncy Castle only if the Cardano library is too heavy or incomplete.

**Acceptance criteria:**

- The verifier returns `true` for a valid signature and `false` for an invalid one.
- Verification happens locally without network calls.
- Unit tests cover both valid and invalid signatures.
- Comment on the chosen library before opening the pull request.

**Acceptance criteria:**

- The verifier returns `true` for a valid signature and `false` for an invalid one.
- Unit tests cover both cases.

**Related:** ADR-02

---

## Issue 4: Create auth controller with challenge and verify endpoints

**Title:** Create auth controller with challenge and verify endpoints

**Body:**

Add the auth endpoints that the frontend will use to sign in.

**Endpoints:**

### POST /api/auth/challenge

Request:

```json
{
  "did": "did:web:matibabu.co:patients:alice"
}
```

Response:

```json
{
  "nonce": "random-string"
}
```

The backend must store the nonce temporarily, associated with the DID, with a short expiry (e.g., 5 minutes).

### POST /api/auth/verify

Request:

```json
{
  "did": "did:web:matibabu.co:patients:alice",
  "nonce": "random-string",
  "signature": "signature-bytes-in-hex-or-base64"
}
```

Response on success:

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Flow:**

1. Validate the nonce exists and has not expired.
2. Resolve the DID to a DID document.
3. Extract the public key.
4. Verify the signature.
5. Find or create the user identity record.
6. Issue a JWT.

**Acceptance criteria:**

- A valid challenge/verify flow returns a JWT.
- An invalid signature returns `401 Unauthorized`.
- A reused or expired nonce is rejected.

**Related:** ADR-02

---

## Issue 5: Create DID-to-user identity mapping

**Title:** Create DID-to-user identity mapping

**Body:**

We need to map a `did:web` identifier to an internal user record so that the rest of the backend can work with stable internal IDs.

**Proposed table:**

```text
user_identities
  id UUID PRIMARY KEY
  did VARCHAR UNIQUE NOT NULL
  user_type VARCHAR NOT NULL -- PATIENT, PROVIDER, ADMIN
  created_at TIMESTAMP NOT NULL
  updated_at TIMESTAMP
```

**Requirements:**

- Add the table via a Flyway migration.
- Create a `UserIdentity` domain entity and repository abstraction.
- Create an application service to find or create a user identity by DID.
- Keep the domain independent of Spring and HTTP.

**Acceptance criteria:**

- A new DID creates a new identity record.
- A returning DID reuses the existing identity record.
- The auth controller can look up the internal user ID from the DID during login.

**Related:** ADR-02

---

## Issue 6: Integrate utxos.dev wallet connection in frontend

**Title:** Integrate utxos.dev wallet connection in frontend

**Body:**

The frontend needs to connect to a Cardano wallet via utxos.dev before it can authenticate.

**Requirements:**

- Add the utxos.dev SDK to the frontend project.
- Create a wallet connection button/component.
- Handle connection state (disconnected, connecting, connected).
- Expose the connected wallet address and public key.

**Acceptance criteria:**

- A user can click "Connect wallet" and authenticate through utxos.dev.
- The frontend can access the wallet's public key.
- The wallet state is available to other components.

**Related:** ADR-02

---

## Issue 7: Implement did:web auth flow in frontend

**Title:** Implement did:web auth flow in frontend

**Body:**

Once the wallet is connected, the frontend must complete the challenge/response flow with the backend.

**Flow:**

1. Derive or register a `did:web` identifier for the connected wallet.
2. Send the DID to `POST /api/auth/challenge`.
3. Sign the returned nonce with the wallet.
4. Send the DID, nonce, and signature to `POST /api/auth/verify`.
5. Store the returned JWT.
6. Include the JWT in the `Authorization` header for all subsequent API calls.

**Requirements:**

- Create an auth client in `frontend/src/lib/api/auth.ts`.
- Store the access token securely (e.g., memory or httpOnly cookie if we add cookie support later).
- Handle auth errors and allow reconnection.

**Acceptance criteria:**

- A user can sign in via wallet and then call `/api/patients` with a valid token.
- Token expiry is handled gracefully.

**Related:** ADR-02

---

## Issue 8: Decide JWT expiry and refresh strategy

**Title:** Decide JWT expiry and refresh strategy

**Body:**

We need to decide how long access tokens should live and whether to issue refresh tokens.

**Questions:**

1. What should the access token expiry be? Options:
   - 15 minutes (more secure)
   - 1 hour (common for dashboards)
   - 24 hours (more convenient)

2. Should we issue refresh tokens?
   - Yes: better UX, users stay logged in.
   - No: users must re-sign with their wallet when the token expires.

3. If we use refresh tokens, how should they be stored and rotated?

**Context:**

This is a healthcare app. Requiring users to sign with their wallet every 15 minutes is likely unacceptable. At the same time, long-lived tokens increase risk.

Please comment with a recommendation.

**Related:** ADR-02

---

## Issue 9: Decide Cardano signature verification library

**Title:** Decide Cardano signature verification library

**Body:**

The backend needs to verify Cardano wallet signatures locally. The signature comes from a utxos.dev wallet and follows CIP-8 / CIP-30 message signing standards.

**Options:**

1. **cardano-client-lib** (Java)
   - Native Java.
   - No network calls.
   - Adds a dependency that must be maintained.

2. **Bouncy Castle** with manual CIP-8 parsing
   - Lightweight.
   - No network calls.
   - Requires understanding the CIP-8 message structure.

**Out of scope:**

- External APIs such as Blockfrost or Koios.
- Separate verification microservices.

Healthcare authentication should not depend on a third-party network call every time someone logs in.

**Recommendation:** start with `cardano-client-lib`. Use Bouncy Castle only as a fallback.

Please comment with the final choice and rationale.

**Related:** ADR-02
