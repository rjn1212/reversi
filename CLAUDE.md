# Othello

Player vs Computer Othello/Reversi game. Java 21 / Spring Boot 3.3.5 backend, React 19 / TypeScript 6 / Vite 8 frontend. Maven multi-module build. Supports both Othello (standard) and Reversi (classic) variants. Mobile responsive.

See `IMPLEMENTATION_PLAN.md` for the full build plan and completion status.

## Status

Phases 1-6 complete. The game is fully playable in a browser on desktop and mobile. Phase 7 (deployment + platform integration) is pending.

- 66 backend tests (all passing)
- 3 AI difficulty levels: Easy (random), Medium (positional heuristics), Hard (minimax + alpha-beta)
- Both Othello and Reversi variants
- Mobile responsive (fits iPhone SE 375px through desktop)

## Build & test

```bash
# Start both servers (kills existing, streams logs to terminal)
./start.sh

# Or start individually:
cd othello-backend && mvn spring-boot:run   # Backend on :8084
cd othello-frontend && npm run dev          # Frontend on :5177

# Build everything (backend + frontend)
mvn clean package

# Run tests (66 tests)
mvn test

# Type-check frontend
cd othello-frontend && npx tsc --noEmit

# Build frontend only
cd othello-frontend && npx vite build
```

- Backend: http://localhost:8084
- Frontend dev: http://localhost:5177 (proxied to backend)
- Ctrl+C stops both when using start.sh

## Project structure

- `othello-backend/` — Spring Boot REST API, all game logic, AI (3 levels)
  - `model/` — Game, Board, Player, Position, GamePhase, GameVariant, AiDifficulty, MoveResult, MoveRecord
  - `engine/` — FlipCalculator, MoveValidator, WinConditionChecker, GameEngine
  - `ai/` — AiStrategy interface, EasyAiStrategy, MediumAiStrategy, HardAiStrategy
  - `service/` — GameService, GameRepository (ConcurrentHashMap), exceptions
  - `controller/` — GameController, GlobalExceptionHandler, DTOs
  - `config/` — WebConfig (CORS, SPA forwarding), JacksonConfig
- `othello-frontend/` — React SPA, bundled into backend's `static/` by Maven
  - `api/` — gameApi.ts (fetch wrapper with injectable auth)
  - `hooks/` — useGame.ts (useReducer state management)
  - `components/Board/` — 8x8 CSS Grid board with responsive sizing
  - `components/Disc/` — Disc with flip animation, last-placed glow
  - `components/Game/` — GameView, GameInfo, ScoreBar
  - `components/Common/` — NewGameDialog, GameOverDialog

## API endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/games` | Create game (`{aiDifficulty, variant, playerSide}`) |
| GET | `/api/v1/games/{id}` | Get game state |
| DELETE | `/api/v1/games/{id}` | Abandon game |
| POST | `/api/v1/games/{id}/surrender` | Surrender |
| GET | `/api/v1/games/{id}/moves` | Get valid moves |
| POST | `/api/v1/games/{id}/moves` | Place disc (`{position}` or `{pass: true}`) |

## Architecture decisions

- **Server-authoritative:** all game logic on the backend, client is a pure renderer
- **Synchronous AI:** AI move returned in same HTTP response as human's move
- **In-memory storage:** `ConcurrentHashMap<String, Game>` — no database for v1
- **Engine is player-agnostic:** `Player.BLACK`/`WHITE` are game roles, not identity types. The "which side is AI" decision lives in `GameService`, not the engine
- **Platform-aware:** configurable CORS, injectable auth headers, optional `platformUserIds` per player

## Platform integration

This game will be hosted on a game platform. The platform is a separate Next.js web shell that provides auth, a game catalog, and score tracking. Games integrate via a contract — they are NOT part of the platform monorepo.

### Current platform-ready features

- `gameApi.ts` uses `getHeaders()` that injects platform JWT tokens from sessionStorage
- `Game` model has `Map<Player, String> platformUserIds` for user identity
- CORS origins configurable via `cors.allowed-origins` property
- `/api/v1/` namespace reserved for game API; `/platform/` reserved for integration endpoints

### What NOT to build yet

- No `/platform/health` endpoint (added during platform integration phase)
- No JWT verification filter (added during platform integration phase)
- No Dockerfile (added during deployment phase)
- No `@gitgames/platform-sdk` dependency in the frontend
- No `games.json` registration

Platform integration is additive — Phase 7.
