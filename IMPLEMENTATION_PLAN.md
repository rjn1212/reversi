# Othello/Reversi -- Full Implementation Plan

## Context

Build a Player vs Computer Othello/Reversi game following the exact architecture pattern established by Stratego and Checkers. A single codebase supports both game variants via a `GameVariant` enum -- the only difference is initial board setup (Othello has a fixed center diagonal; Reversi starts empty with free placement of the first 4 discs in center squares). The API is platform-agnostic so the same backend serves web, future iPhone (React Native), and Android clients. All game logic lives on the server -- clients are pure renderers.

**Stack:** Java 21 / Spring Boot 3.3.5 / Maven multi-module backend, React 18 / TypeScript / Vite frontend
**Ports:** Backend 8084, Frontend 5177
**AI:** 3 levels -- Easy (random), Medium (positional heuristics), Hard (minimax + alpha-beta)
**Multiplayer:** Single-player vs AI only for v1 (architecture supports future multiplayer)

---

## Architecture Overview

```
+-----------------+     REST API (JSON)     +----------------------+
|  React Web App  | <---------------------> |  Spring Boot Backend |
|  (Vite + TS)    |    /api/v1/*            |                      |
+-----------------+                         |  +----------------+  |
                                            |  | GameController  |  |
+-----------------+                         |  +-------+--------+  |
|  React Native   | <--- same API --------> |  +-------v--------+  |
|  (future)       |                         |  | GameService     |  |
+-----------------+                         |  +-------+--------+  |
                                            |  +-------v--------+  |
                                            |  | GameEngine      |  |
                                            |  | MoveValidator   |  |
                                            |  | FlipCalculator  |  |
                                            |  | WinCondChecker  |  |
                                            |  +-------+--------+  |
                                            |  +-------v--------+  |
                                            |  | AI Engine       |  |
                                            |  | Easy/Medium/Hard|  |
                                            |  +----------------+  |
                                            +----------------------+
```

**Key decisions (matching Stratego/Checkers):**
- Server-authoritative: server validates every move, computes all flips
- Synchronous AI: AI move returned in same HTTP response as human's move
- In-memory storage: `ConcurrentHashMap<String, Game>` -- no database for v1
- No fog of war (both players see all pieces)
- Platform-aware: optional user identity per player slot, configurable CORS, extensible auth headers

---

## Project Structure

```
othello/
+-- pom.xml                              (parent POM, modules: backend + frontend)
+-- IMPLEMENTATION_PLAN.md
+-- Othello_Rules.md
+-- CLAUDE.md
+-- start.sh
+-- .gitignore
+-- othello-backend/
|   +-- pom.xml
|   +-- src/main/java/com/gitgames/othello/
|   |   +-- OthelloApplication.java
|   |   +-- config/
|   |   |   +-- WebConfig.java           (CORS, SPA forwarding)
|   |   |   +-- JacksonConfig.java
|   |   +-- controller/
|   |   |   +-- GameController.java
|   |   |   +-- GlobalExceptionHandler.java
|   |   |   +-- dto/
|   |   |       +-- CreateGameRequest.java
|   |   |       +-- MakeMoveRequest.java
|   |   |       +-- GameStateResponse.java
|   |   |       +-- MoveResponse.java
|   |   |       +-- ValidMovesResponse.java
|   |   |       +-- PositionDto.java
|   |   |       +-- ErrorResponse.java
|   |   +-- model/
|   |   |   +-- Game.java                (aggregate root)
|   |   |   +-- Board.java               (8x8 Player grid)
|   |   |   +-- Player.java              (enum: BLACK, WHITE)
|   |   |   +-- GamePhase.java           (enum: PLACEMENT, PLAYING, GAME_OVER)
|   |   |   +-- GameVariant.java         (enum: OTHELLO, REVERSI)
|   |   |   +-- Position.java            (record: row, col)
|   |   |   +-- MoveResult.java          (record: position, flipped list)
|   |   |   +-- MoveRecord.java          (record: player, position, flippedCount, wasPass)
|   |   |   +-- AiDifficulty.java        (enum: EASY, MEDIUM, HARD)
|   |   +-- engine/
|   |   |   +-- GameEngine.java          (orchestrates turns)
|   |   |   +-- MoveValidator.java       (valid move detection)
|   |   |   +-- FlipCalculator.java      (8-direction flip computation)
|   |   |   +-- WinConditionChecker.java
|   |   +-- ai/
|   |   |   +-- AiStrategy.java          (interface)
|   |   |   +-- EasyAiStrategy.java      (random valid move)
|   |   |   +-- MediumAiStrategy.java    (positional heuristics)
|   |   |   +-- HardAiStrategy.java      (minimax + alpha-beta)
|   |   +-- service/
|   |       +-- GameService.java
|   |       +-- GameRepository.java      (ConcurrentHashMap wrapper)
|   |       +-- GameNotFoundException.java
|   |       +-- InvalidMoveException.java
|   +-- src/main/resources/
|   |   +-- application.properties
|   +-- src/test/java/com/gitgames/othello/
|       +-- model/PositionTest.java, BoardTest.java
|       +-- engine/MoveValidatorTest.java, FlipCalculatorTest.java, GameEngineTest.java
|       +-- ai/AiStrategyTest.java
|       +-- controller/GameControllerTest.java
+-- othello-frontend/
    +-- pom.xml                          (frontend-maven-plugin)
    +-- package.json
    +-- vite.config.ts                   (proxy /api to :8084, port 5177)
    +-- src/
        +-- main.tsx
        +-- App.tsx
        +-- api/gameApi.ts               (fetch wrapper with injectable auth)
        +-- types/game.ts                (mirrors API types)
        +-- hooks/useGame.ts             (useReducer state management)
        +-- styles/global.css
        +-- components/
            +-- Board/Board.tsx + .css    (CSS Grid 8x8, green Othello board)
            +-- Disc/Disc.tsx + .css      (circle with flip animation)
            +-- Game/GameView.tsx + GameInfo.tsx + ScoreBar.tsx
            +-- Common/NewGameDialog.tsx + GameOverDialog.tsx
```

---

## Domain Model

### Board
- 8x8 grid of `Player` values (BLACK/WHITE) or null for empty
- No `Piece` objects needed -- Othello discs have no identity beyond color
- `Board.createOthelloStart()`: places WHITE at (3,3)/(4,4), BLACK at (3,4)/(4,3) (diagonal)
- `Board.createReversiStart()`: returns empty board
- Methods: `placeDisc`, `flipDisc`, `getDiscAt`, `isEmpty`, `countDiscs`, `countEmpty`, `deepCopy`

### Game (Aggregate Root)
- `id`, `board`, `currentTurn` (starts BLACK), `phase`, `winner`, `winReason`
- `aiDifficulty`, `variant` (OTHELLO/REVERSI), `humanSide`
- `moveHistory` (List<MoveRecord>), `lastMoveWasPass` (consecutive-pass tracking)
- `platformUserIds` (Map<Player, String>) for future platform auth
- For OTHELLO: starts in PLAYING phase. For REVERSI: starts in PLACEMENT phase

### GamePhase
- `PLACEMENT` -- Reversi only: first 4 discs placed freely in center squares (d4/d5/e4/e5)
- `PLAYING` -- standard play with outflanking/flipping
- `GAME_OVER` -- board full or both players have no valid moves

---

## REST API Contract

### Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/games` | Create game (body: `{aiDifficulty, variant, playerSide}`) |
| GET | `/api/v1/games/{id}` | Get game state |
| DELETE | `/api/v1/games/{id}` | Abandon game |
| POST | `/api/v1/games/{id}/surrender` | Surrender |
| GET | `/api/v1/games/{id}/moves` | Get valid moves for current player |
| POST | `/api/v1/games/{id}/moves` | Place disc (body: `{position}` or `{pass: true}`) |

### Move API -- key Othello differences from Stratego/Checkers
- Move request uses `position` (where to place), not `from/to` (no piece movement)
- Response includes `flipped` list (positions that changed color) for animation
- Pass support: when no valid moves exist, client sends `{pass: true}`
- AI may also pass: response `aiMove` can have `pass: true`

### Move Response Shape
```json
{
  "humanMove": { "position": {"row":2,"col":3}, "flipped": [...], "pass": false },
  "aiMove": { "position": {"row":5,"col":4}, "flipped": [...], "pass": false },
  "gameState": { "gameId": "...", "phase": "PLAYING", "board": {...}, "discCount": {...}, ... }
}
```

If AI goes first (human chose WHITE), the first AI move is made during game creation and reflected in the initial game state.

---

## Engine Design

### FlipCalculator
Core algorithm. Scans 8 directions from a placed position. For each direction:
1. Walk collecting opponent discs
2. If we hit our own disc (anchor): return all collected -- they flip
3. If we hit empty or board edge: return nothing for this direction
4. Union of all 8 directions = complete flip set

### MoveValidator
- `isValidMove(board, pos, player)`: square is empty AND `flipCalculator.calculateFlips()` is non-empty
- `getValidMoves(board, player)`: scan all 64 squares
- `hasValidMoves(board, player)`: early-exit version of above
- `isValidPlacement(board, pos)`: Reversi placement -- must be center square and empty

### WinConditionChecker
Game over when:
1. Board is full (`countEmpty() == 0`), OR
2. Neither player has valid moves

Winner = player with more discs. Draw if equal (32-32).

### GameEngine
Orchestrates: `executeMove` (place + flip all), `executePass`, `executePlacement` (Reversi, no flipping)

### Pass Logic (in GameService)
- Track `lastMoveWasPass` on Game. When a player passes, if `lastMoveWasPass` is already true => game over (both passed consecutively). Otherwise set it to true. Reset to false on any real move.

---

## AI Engine

### Easy -- Random valid move

### Medium -- Positional heuristic scoring per move
**Position weight table:**
```
 100  -20   10    5    5   10  -20  100
 -20  -50   -2   -2   -2   -2  -50  -20
  10   -2    5    1    1    5   -2   10
   5   -2    1    0    0    1   -2    5
   5   -2    1    0    0    1   -2    5
  10   -2    5    1    1    5   -2   10
 -20  -50   -2   -2   -2   -2  -50  -20
 100  -20   10    5    5   10  -20  100
```
- Corners (100): permanently stable once captured
- X-squares (-50): give opponent corner access
- C-squares (-20): risky near corners
- Also factors: flip count, opponent mobility reduction, corner ownership adjustment

### Hard -- Minimax with alpha-beta pruning
- Depth 6 normal, depth 10+ in endgame (<=12 empty squares)
- Evaluation function: corner occupancy (100x), stable discs (15x), mobility (5x), frontier disc penalty (-3x), positional weight map, game-phase-dependent disc parity (1x early, 10x endgame)
- Pass handling in minimax tree (pass gives turn without reducing depth)
- Random tiebreaker among equally-scored moves

---

## Frontend Design

### Board: CSS Grid 8x8 with classic Othello green (`#2d7a2d`)
### Disc: Circular with radial gradient (black/white), CSS flip animation (`scaleX` transform)
### Valid moves: Semi-transparent dots on empty valid squares
### ScoreBar: Proportional horizontal bar showing disc counts
### NewGameDialog: Variant (Othello/Reversi) + Difficulty (Easy/Medium/Hard) + Side (Black/White)
### Pass button: Shown when `mustPass` is true
### AI move highlight: Brief indicator showing where AI placed
### Responsive: `clamp(40px, 8vw, 64px)` square sizing

---

## Implementation Phases

### Phase 1: Project Skeleton + Domain Model
- Maven multi-module setup (parent, backend, frontend POMs)
- All model classes: Player, Position, Board, Game, GamePhase, GameVariant, AiDifficulty, MoveResult, MoveRecord
- Spring Boot app, WebConfig (CORS for localhost:5177), JacksonConfig, application.properties (port 8084)
- React Vite project, vite.config.ts (port 5177, proxy to 8084)
- Unit tests for Position and Board
- **Deliverable:** Both projects build, Spring Boot starts

### Phase 2: Core Game Engine
- FlipCalculator, MoveValidator, WinConditionChecker, GameEngine
- Comprehensive unit tests: flip calculation, valid moves, pass detection, game over, Reversi placement
- **Deliverable:** All game logic working and tested

### Phase 3: REST API + Service Layer
- GameRepository, GameService, DTOs, GameController, GlobalExceptionHandler
- Consecutive-pass tracking, AI integration (EasyAI only initially)
- Integration tests via MockMvc
- **Deliverable:** Complete game playable via curl

### Phase 4: AI Engine
- EasyAiStrategy (random), MediumAiStrategy (positional heuristics), HardAiStrategy (minimax + alpha-beta)
- AI tests: valid move generation, corner preference, trap avoidance
- **Deliverable:** All 3 AI levels functional

### Phase 5: Frontend
- TypeScript types, API client, useGame hook
- Board, Disc, GameView, GameInfo, ScoreBar, NewGameDialog, GameOverDialog
- Full game flow: new game -> play with valid move highlights -> pass handling -> game over
- Reversi placement phase UI
- **Deliverable:** Fully playable in browser

### Phase 6: Polish
- Flip animation, AI move highlight, last-move indicator
- Error handling with auto-dismiss, loading states
- Surrender button, responsive CSS, row/column labels
- start.sh script, CLAUDE.md
- **Deliverable:** Production-quality game

### Phase 7: Deployment + Platform Integration
- Dockerfile (multi-stage eclipse-temurin:21)
- `GET /platform/health` endpoint
- Update game-platform `games.json` with Othello entry (backend 8084)
- README.md, Othello_Rules.md
- **Deliverable:** Deployable, documented, platform-registered

---

## Verification Plan

1. **Unit tests:** `mvn test` -- FlipCalculator, MoveValidator, WinConditionChecker, GameEngine, all 3 AI strategies, controller integration
2. **API testing:** curl flows for Othello + Reversi, all difficulties, both sides, pass scenarios, surrender
3. **Browser testing:** Full games for each variant x difficulty x side combination. Force pass situations. Verify draw handling.
4. **Edge cases:** Opening moves, corner captures, consecutive passes, board full, 32-32 draw, AI as BLACK (first move on creation), Reversi center placement variations

---

## Rules Cross-Reference

| Rule | Implementation |
|------|---------------|
| 8x8 board | `Board.SIZE = 8` |
| Othello: fixed diagonal center start | `Board.createOthelloStart()` |
| Reversi: empty board, free center placement | `Board.createReversiStart()`, `GamePhase.PLACEMENT` |
| Black moves first | `Game` constructor sets `currentTurn = BLACK` |
| Place to outflank in 8 directions | `FlipCalculator.calculateFlips()` |
| All flips simultaneous and mandatory | `GameEngine.executeMove()` applies all at once |
| Valid only if >= 1 flip | `MoveValidator.isValidMove()` checks non-empty flip list |
| Must pass if no valid move | `hasValidMoves()` check, `mustPass` flag, pass endpoint |
| Game over: board full | `WinConditionChecker`: `countEmpty() == 0` |
| Game over: neither can move | `WinConditionChecker`: both `hasValidMoves()` false |
| Most discs wins | `getWinner()` compares disc counts |
| 32-32 is draw | `getWinner()` returns null on equal counts |

---

## Reference Files

Existing implementations to use as patterns:
- Stratego CLAUDE.md: `/Users/randyn/projects/git_games/Stratego/CLAUDE.md`
- Stratego plan: `/Users/randyn/projects/git_games/Stratego/IMPLEMENTATION_PLAN.md`
- Checkers CLAUDE.md: `/Users/randyn/projects/git_games/Checkers/CLAUDE.md`
- Checkers plan: `/Users/randyn/projects/git_games/Checkers/IMPLEMENTATION_PLAN.md`
- Game platform architecture: `/Users/randyn/projects/git/game-platform/ARCHITECTURE.md`
- Game platform setup: `/Users/randyn/projects/git/game-platform/SETUP.md`
- Game registry: `/Users/randyn/projects/git/game-platform/games.json`
