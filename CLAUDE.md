# Othello

Player vs Computer Othello/Reversi game. Java 21 / Spring Boot 3.3.5 backend, React 19 / TypeScript / Vite frontend. Maven multi-module build. Supports both Othello (standard) and Reversi (classic) variants.

See `IMPLEMENTATION_PLAN.md` for the full build plan.

## Build & test

```bash
# Start both servers (kills existing, streams logs to terminal)
./start.sh

# Or start individually:
cd othello-backend && mvn spring-boot:run   # Backend on :8084
cd othello-frontend && npm run dev          # Frontend on :5177

# Build everything (backend + frontend)
mvn clean package

# Run tests
mvn test
```

- Backend: http://localhost:8084
- Frontend dev: http://localhost:5177 (proxied to backend)
- Ctrl+C stops both when using start.sh

## Project structure

- `othello-backend/` — Spring Boot REST API, all game logic, AI (3 levels)
- `othello-frontend/` — React SPA, bundled into backend's `static/` by Maven

## Platform integration

This game will be hosted on a game platform. The platform is a separate Next.js web shell that provides auth, a game catalog, and score tracking. Games integrate via a contract — they are NOT part of the platform monorepo.

### What this means for development

**1. Don't hardcode which Player is human or AI in the engine layer.**

`Player.BLACK` and `Player.WHITE` are game roles, not identity types. The engine (`MoveValidator`, `FlipCalculator`, `WinConditionChecker`) should work with `Player` values without knowing or caring which is human. The decision about "which side is AI" belongs in `GameService`, not the engine.

**2. The Game model supports optional user identity.**

```java
private final Map<Player, String> platformUserIds = new EnumMap<>(Player.class);
```

**3. Make CORS origins configurable.**

```java
@Value("${cors.allowed-origins:http://localhost:5177}")
private String allowedOrigins;
```

**4. Structure the frontend API client to accept auth headers.**

`gameApi.ts` uses a `getHeaders()` function that injects platform JWT tokens from sessionStorage.

**5. The `/api/v1/` namespace is ours.**

Platform integration endpoints go under `/platform/` (e.g., `GET /platform/health`).

**6. The synchronous AI response is fine but conditional.**

The move endpoint returns the AI's move in the same response. The "should AI respond?" decision is in `GameService`, not the controller.

### What NOT to build yet

- No `/platform/health` endpoint (added during platform integration phase)
- No JWT verification filter (added during platform integration phase)
- No Dockerfile (added during deployment phase)
- No `@gitgames/platform-sdk` dependency in the frontend
- No `games.json` registration

Focus on building a complete, working single-player game first. Platform integration is additive.
