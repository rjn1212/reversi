# Reversio

Player vs Computer Reversio/Reversi game. Java 21 / Spring Boot 3.3.5 backend, React 19 / TypeScript 6 / Vite 8 frontend. Maven multi-module build. Supports both Reversio (an advanced version of Reversi) and Reversi (classic) variants. Mobile responsive.

See `IMPLEMENTATION_PLAN.md` for the full build plan and completion status.

## Status

All phases (1-7) complete. The game is fully playable in a browser on desktop and mobile, with full platform integration.

- 66 backend tests (all passing)
- 3 AI difficulty levels: Easy (random), Medium (positional heuristics), Hard (minimax + alpha-beta)
- Both Reversio and Reversi variants
- Mobile responsive (fits iPhone SE 375px through desktop)
- Platform integration: health endpoint, JWT auth, score reporting, Dockerfile
- Frontend uses `@gitgames/platform-sdk` for token handling

## Build & test

```bash
# Start both servers (kills existing, streams logs to terminal)
./start.sh

# Or start individually:
cd reversio-backend && mvn spring-boot:run   # Backend on :8084
cd reversio-frontend && npm run dev          # Frontend on :5177

# Build everything (backend + frontend)
mvn clean package

# Run tests (66 tests)
mvn test

# Type-check frontend
cd reversio-frontend && npx tsc --noEmit

# Build frontend only
cd reversio-frontend && npx vite build
```

- Backend: http://localhost:8084
- Frontend dev: http://localhost:5177 (proxied to backend)
- Ctrl+C stops both when using start.sh

## Project structure

- `reversio-backend/` — Spring Boot REST API, all game logic, AI (3 levels)
  - `model/` — Game, Board, Player, Position, GamePhase, GameVariant, AiDifficulty, MoveResult, MoveRecord
  - `engine/` — FlipCalculator, MoveValidator, WinConditionChecker, GameEngine
  - `ai/` — AiStrategy interface, EasyAiStrategy, MediumAiStrategy, HardAiStrategy
  - `service/` — GameService, GameRepository (ConcurrentHashMap), PlatformScoreReporter, exceptions
  - `controller/` — GameController (input validation), GlobalExceptionHandler, PlatformController (health), DTOs
  - `config/` — WebConfig (CORS, SPA forwarding), JacksonConfig, PlatformAuthFilter (JWT)
- `reversio-frontend/` — React SPA, bundled into backend's `static/` by Maven
  - `api/` — gameApi.ts (fetch wrapper using @gitgames/platform-sdk)
  - `hooks/` — useGame.ts (useReducer state management)
  - `components/Board/` — 8x8 CSS Grid board with responsive sizing
  - `components/Disc/` — Disc with flip animation, last-placed glow
  - `components/Game/` — GameView, GameInfo, ScoreBar
  - `components/Common/` — NewGameDialog (with How to Play rules, error display), GameOverDialog

## API endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/games` | Create game (`{aiDifficulty, variant, playerSide}`, all required) |
| GET | `/api/v1/games/{id}` | Get game state |
| DELETE | `/api/v1/games/{id}` | Abandon game |
| POST | `/api/v1/games/{id}/surrender` | Surrender |
| GET | `/api/v1/games/{id}/moves` | Get valid moves |
| POST | `/api/v1/games/{id}/moves` | Place disc (`{position}` or `{pass: true}`) |
| GET | `/platform/health` | Health check (status, game name, version, uptime) |

## Architecture decisions

- **Server-authoritative:** all game logic on the backend, client is a pure renderer
- **Synchronous AI:** AI move returned in same HTTP response as human's move
- **In-memory storage:** `ConcurrentHashMap<String, Game>` — no database for v1
- **Engine is player-agnostic:** `Player.BLACK`/`WHITE` are game roles, not identity types. The "which side is AI" decision lives in `GameService`, not the engine
- **Platform-aware:** configurable CORS, injectable auth headers, optional `platformUserIds` per player

## Platform integration

This game is hosted on a game platform — a separate Next.js web shell that provides auth, a game catalog, and score tracking. Games integrate via a contract and are NOT part of the platform monorepo.

### Implemented features

- **Health endpoint:** `GET /platform/health` returns status, game name, version, uptime
- **JWT auth filter:** `PlatformAuthFilter` verifies HMAC256 tokens with issuer `game-platform`, extracts `platformUserId`, `platformUsername`, `platformUserEmail`, `platformUserRole` as request attributes. Skips `/platform/` routes. Continues anonymously on invalid/missing tokens.
- **Score reporting:** `PlatformScoreReporter` sends async POST to platform `/api/scores` with `Authorization: Bearer` header, payload includes `gameSlug`, `userId`, `score`, `metadata` (result, opponentScore, difficulty, variant), and `completedAt` (ISO-8601)
- **Frontend SDK:** `@gitgames/platform-sdk` (`PlatformClient`) handles URL param extraction, sessionStorage, and auth headers
- **Dockerfile:** Multi-stage build with eclipse-temurin:21, uses Maven wrapper
- **CORS:** Origins configurable via `cors.allowed-origins` property, covers both `/api/` and `/platform/` paths
- **User identity:** `Game` model has `Map<Player, String> platformUserIds`

### Configuration (environment variables)

- `PLATFORM_JWT_SECRET` — HMAC256 secret for JWT verification (blank = auth disabled)
- `PLATFORM_API_URL` — Base URL for score reporting (blank = reporting disabled)
- `PLATFORM_API_KEY` — API key sent as Bearer token for score reporting
