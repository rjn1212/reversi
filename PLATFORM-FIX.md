# Platform Integration Fixes — Othello

Stack: Java 21 / Spring Boot / React / Vite
Port: 8084

## 1. Health endpoint — missing (REQUIRED)

No health endpoint exists. No PlatformController or any `/platform/health` route.

**Fix:** Create a `PlatformController` class with `@GetMapping("/platform/health")` that tracks startup time and returns:
```json
{"status":"ok","game":"othello","version":"1.0.0","uptime":3600}
```

Files:
- Create: `othello-backend/src/main/java/com/gitgames/othello/controller/PlatformController.java`

## 2. Frontend — missing URL param extraction (REQUIRED)

The `gameApi.ts` already reads `platformToken` from sessionStorage and sends Bearer headers — this is correct. But nothing extracts the token from URL query params when the user arrives from the platform redirect.

**Fix:** Add a startup hook in `App.tsx` that:
1. Reads `platformToken` and `returnUrl` from `window.location.search`
2. Stores them in `sessionStorage`
3. Cleans the URL with `history.replaceState`

Files:
- Modify: `othello-frontend/src/App.tsx`

## 3. Dockerfile — missing (REQUIRED for Railway)

No Dockerfile exists.

**Fix:** Create a multi-stage Dockerfile in the project root:
```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/othello-backend/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Files:
- Create: `Dockerfile`

## 4. games.json registration — missing (REQUIRED)

Othello is not registered in the platform's `games.json`. An entry needs to be added to the game-platform repo.

**Fix:** Add to `games.json` in game-platform:
```json
{
  "slug": "othello",
  "name": "Othello",
  "description": "Classic reversi board game. Outflank your opponent to control the board.",
  "version": "1.0.0",
  "tags": ["strategy", "board-game", "ai", "classic"],
  "players": { "min": 1, "max": 2, "modes": ["single-player"] },
  "thumbnail": "/images/games/othello.png",
  "backend": { "url": "http://localhost:8084", "healthEndpoint": "/platform/health" },
  "frontend": { "launchUrl": "http://localhost:8084", "launchMode": "redirect" },
  "features": { "auth": false, "scoreReporting": false, "saveLoad": false },
  "access": { "minRole": "BASICPLUS", "gpCost": 20 }
}
```

## 5. JWT auth verification — not implemented (OPTIONAL)

No JWT verification filter exists.

**Fix:** Add `com.auth0:java-jwt:4.4.0` dependency, create a `PlatformAuthFilter` that verifies Bearer tokens with `PLATFORM_JWT_SECRET`, extracts claims, sets them as request attributes. Continue anonymously if no token.

Files:
- Modify: `pom.xml` or `othello-backend/pom.xml` — add java-jwt dependency
- Create: `othello-backend/src/main/java/com/gitgames/othello/config/PlatformAuthFilter.java`
- Modify: `othello-backend/src/main/resources/application.properties` — add `platform.jwt.secret`

## 6. Score reporting — not implemented (OPTIONAL)

No score reporting to the platform.

**Fix:** Create a `PlatformScoreReporter` service that POSTs game results to the platform's `/api/scores` when a game ends.

Files:
- Create: `othello-backend/src/main/java/com/gitgames/othello/service/PlatformScoreReporter.java`
- Modify: `othello-backend/src/main/resources/application.properties` — add `platform.api.url` and `platform.api.key`

## Already working

- **CORS**: Configurable via `CORS_ALLOWED_ORIGINS` env var (default `http://localhost:5177`). No changes needed.
- **Frontend Bearer header**: `gameApi.ts` already reads from sessionStorage and sends Authorization header.

## Summary

| Requirement | Status | Priority |
|---|---|---|
| Health endpoint | Missing | REQUIRED |
| Frontend token from URL | Missing | REQUIRED |
| Dockerfile | Missing | REQUIRED |
| games.json registration | Missing | REQUIRED |
| JWT verification | Missing | OPTIONAL |
| Score reporting | Missing | OPTIONAL |
| CORS config | Working | — |
| Frontend Bearer header | Working | — |
