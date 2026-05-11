# Platform Integration Plan — Reversio

The backend platform integration (health, JWT filter, score reporter, Dockerfile) is implemented. This plan covers the remaining fixes needed for full compatibility with the game-platform contract.

## Reference

- Platform repo: `/Users/randyn/projects/git/game-platform`
- Platform score API: `POST /api/scores` — expects `Authorization: Bearer <GAME_API_KEY>` header
- Platform score payload: `{ gameSlug, userId, score, metadata, completedAt }` (see `packages/platform-types/src/scores.ts`)
- Platform JWT claims: `{ sub, username, email, role, gpBalance, iss: "game-platform" }`
- Platform SDK: `@gitgames/platform-sdk` (see `packages/platform-sdk/src/index.ts`)

---

## 1. Adopt @gitgames/platform-sdk on the frontend

Replace the hand-rolled token handling in `App.tsx` and `gameApi.ts` with the platform SDK.

### Install

```bash
cd reversio-frontend
npm install @gitgames/platform-sdk
```

### Changes

**`reversio-frontend/src/App.tsx`**:
- Remove the code that manually reads `platformToken`/`returnUrl` from URL params, stores in sessionStorage, and cleans the URL
- Instantiate `PlatformClient` once at module scope or via context

**`reversio-frontend/src/api/gameApi.ts`**:
- Remove the manual `getHeaders()` function that reads from sessionStorage
- Import and use the shared `PlatformClient` instance
- Use `platform.getAuthHeaders()` merged with `Content-Type` header

Example pattern:
```typescript
import { PlatformClient } from '@gitgames/platform-sdk';

export const platform = new PlatformClient();

function getHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...platform.getAuthHeaders(),
  };
}
```

The `PlatformClient` constructor automatically handles URL param extraction, sessionStorage, and URL cleanup.

---

## 2. Fix score reporting auth header

The platform's `/api/scores` endpoint checks `Authorization: Bearer <key>`. The current `PlatformScoreReporter` uses `X-Api-Key` which won't pass validation.

**File:** `reversio-backend/src/main/java/com/gitgames/reversio/service/PlatformScoreReporter.java`

Change:
```java
.header("X-Api-Key", platformApiKey)
```
To:
```java
.header("Authorization", "Bearer " + platformApiKey)
```

---

## 3. Fix score reporting payload format

The current payload is close but needs adjustment to match the platform's `ScoreSubmission` type.

**File:** `reversio-backend/src/main/java/com/gitgames/reversio/service/PlatformScoreReporter.java`

Current payload:
```json
{"gameSlug":"reversio","userId":"...","outcome":"win","score":38,"opponentScore":26,"difficulty":"HARD","variant":"REVERSIO"}
```

Required payload:
```json
{
  "gameSlug": "reversio",
  "userId": "...",
  "score": 38,
  "metadata": {
    "result": "win",
    "opponentScore": 26,
    "difficulty": "HARD",
    "variant": "REVERSIO"
  },
  "completedAt": "2026-05-11T14:30:00Z"
}
```

Changes:
- Keep `gameSlug`, `userId`, `score` at top level
- Move `outcome` (renamed to `result`), `opponentScore`, `difficulty`, `variant` into `metadata` object
- Add `completedAt` as ISO-8601 timestamp
- Remove `outcome` from top level

---

## 4. Add JWT issuer validation

The `PlatformAuthFilter` verifies the signature but doesn't check the `iss` claim. The platform mints tokens with `iss: "game-platform"`.

**File:** `reversio-backend/src/main/java/com/gitgames/reversio/config/PlatformAuthFilter.java`

Change:
```java
JWT.require(algorithm).build()
```
To:
```java
JWT.require(algorithm).withIssuer("game-platform").build()
```

---

## 5. Fix JWT claim extraction — wrong claim name

The filter reads `name` but the platform JWT uses `username`.

**File:** `reversio-backend/src/main/java/com/gitgames/reversio/config/PlatformAuthFilter.java`

Change:
```java
request.setAttribute("platformUserName", jwt.getClaim("name").asString());
```
To:
```java
request.setAttribute("platformUsername", jwt.getClaim("username").asString());
```

This also standardizes the attribute name (`platformUsername` not `platformUserName`) to match the other games.

---

## 6. Update games.json in game-platform

**File (in game-platform repo):** `games.json`

Update the reversio entry:
- `features.auth`: `false` → `true`
- `features.scoreReporting`: `false` → `true`

---

## Verification

After all changes:
1. `mvn clean package` — builds without errors
2. Start the backend, confirm `GET /platform/health` returns `{"status":"ok","game":"reversio",...}`
3. Play a game with a platform token — verify JWT is accepted and username is correctly extracted
4. Complete a game — verify score POST uses `Authorization: Bearer` header and correct payload with `metadata` object
5. Confirm the frontend uses the SDK for token handling and sends Bearer headers on API calls
