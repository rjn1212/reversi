# Reversio/Reversi -- Full Implementation Plan

## Context

Build a Player vs Computer Reversio/Reversi game following the exact architecture pattern established by Stratego and Checkers. A single codebase supports both game variants via a `GameVariant` enum -- the only difference is initial board setup (Reversio has a fixed center diagonal; Reversi starts empty with free placement of the first 4 discs in center squares). The API is platform-agnostic so the same backend serves web, future iPhone (React Native), and Android clients. All game logic lives on the server -- clients are pure renderers.

**Stack:** Java 21 / Spring Boot 3.3.5 / Maven multi-module backend, React 19 / TypeScript 6 / Vite 8 frontend
**Ports:** Backend 8084, Frontend 5177
**AI:** 3 levels -- Easy (random), Medium (positional heuristics), Hard (minimax + alpha-beta)
**Multiplayer:** Single-player vs AI only for v1 (architecture supports future multiplayer)
**Tests:** 66 unit/integration tests (all passing)

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
reversio/
+-- pom.xml                              (parent POM, modules: backend + frontend)
+-- IMPLEMENTATION_PLAN.md
+-- CLAUDE.md
+-- start.sh
+-- .gitignore
+-- reversio-backend/
|   +-- pom.xml
|   +-- src/main/java/com/gitgames/reversio/
|   |   +-- ReversioApplication.java
|   |   +-- config/
|   |   |   +-- WebConfig.java           (CORS, SPA forwarding)
|   |   |   +-- JacksonConfig.java
|   |   +-- controller/
|   |   |   +-- GameController.java
|   |   |   +-- GlobalExceptionHandler.java
|   |   |   +-- dto/
|   |   |       +-- CreateGameRequest.java
|   |   |       +-- MakeMoveRequest.java
|   |   |       +-- GameStateResponse.java (with BoardDto, DiscDto inner classes)
|   |   |       +-- MoveResponseDto.java   (with MoveDataDto inner class)
|   |   |       +-- ValidMovesResponse.java
|   |   |       +-- PositionDto.java
|   |   |       +-- ErrorResponse.java
|   |   +-- model/
|   |   |   +-- Game.java                (aggregate root)
|   |   |   +-- Board.java               (8x8 Player grid)
|   |   |   +-- Player.java              (enum: BLACK, WHITE)
|   |   |   +-- GamePhase.java           (enum: PLACEMENT, PLAYING, GAME_OVER)
|   |   |   +-- GameVariant.java         (enum: REVERSIO, REVERSI)
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
|   +-- src/test/java/com/gitgames/reversio/
|       +-- model/PositionTest.java, BoardTest.java
|       +-- engine/MoveValidatorTest.java, FlipCalculatorTest.java, GameEngineTest.java
|       +-- ai/AiStrategyTest.java
|       +-- controller/GameControllerTest.java
+-- reversio-frontend/
    +-- pom.xml                          (frontend-maven-plugin)
    +-- package.json
    +-- vite.config.ts                   (proxy /api to :8084, port 5177)
    +-- index.html                       (viewport meta, entry point)
    +-- eslint.config.js
    +-- tsconfig.json, tsconfig.app.json, tsconfig.node.json
    +-- src/
        +-- main.tsx
        +-- App.tsx
        +-- vite-env.d.ts
        +-- api/gameApi.ts               (fetch wrapper with injectable auth)
        +-- types/game.ts                (mirrors API types)
        +-- hooks/useGame.ts             (useReducer state management)
        +-- styles/global.css            (dark theme, buttons, mobile breakpoints)
        +-- components/
            +-- Board/Board.tsx + .css    (CSS Grid 8x8, responsive square sizing)
            +-- Disc/Disc.tsx + .css      (circle with flip animation)
            +-- Game/GameView.tsx + .css  (board + info layout, stacks on mobile)
            +-- Game/GameInfo.tsx + .css  (turn, scores, actions panel)
            +-- Game/ScoreBar.tsx         (proportional black/white bar)
            +-- Common/NewGameDialog.tsx + .css (variant/difficulty/side selection)
            +-- Common/GameOverDialog.tsx + .css (win/lose/draw overlay)
```

---

## Domain Model

### Board
- 8x8 grid of `Player` values (BLACK/WHITE) or null for empty
- No `Piece` objects needed -- Reversio discs have no identity beyond color
- `Board.createReversioStart()`: places WHITE at (3,3)/(4,4), BLACK at (3,4)/(4,3) (diagonal)
- `Board.createReversiStart()`: returns empty board
- Methods: `placeDisc`, `flipDisc`, `getDiscAt`, `isEmpty`, `countDiscs`, `countEmpty`, `deepCopy`

### Game (Aggregate Root)
- `id`, `board`, `currentTurn` (starts BLACK), `phase`, `winner`, `winReason`
- `aiDifficulty`, `variant` (REVERSIO/REVERSI), `humanSide`
- `moveHistory` (List<MoveRecord>), `lastMoveWasPass` (consecutive-pass tracking)
- `platformUserIds` (Map<Player, String>) for future platform auth
- For REVERSIO: starts in PLAYING phase. For REVERSI: starts in PLACEMENT phase

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

### Move API -- key Reversio differences from Stratego/Checkers
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

### Board: CSS Grid 8x8 with classic Reversio green (`#2d7a2d`), row (1-8) and column (a-h) labels
### Disc: Circular with radial gradient (black/white), CSS flip animation (`scaleX` transform), last-placed glow
### Valid moves: Semi-transparent yellow dots on empty valid squares
### ScoreBar: Proportional horizontal bar showing black/white disc counts
### NewGameDialog: Variant (Reversio/Reversi) + Difficulty (Easy/Medium/Hard) + Side (Black/White)
### GameInfo: Turn indicator, disc counts, move number, difficulty, variant, pass/surrender buttons
### Pass button: Shown when `mustPass` is true
### AI move highlight: Yellow glow on last-placed disc
### Error display: Auto-dismiss after 4 seconds
### Dark theme: Background `#1a1a2e`, panels `#2a2a4a`, green board

### Mobile Responsive Design
- Board squares: `min(calc((100vw - 71px) / 8), 64px)` -- auto-fits any viewport width
- At `<400px`: smaller row/col labels (16px), thinner board border, recalculated square size
- At `<480px`: vertical stacking (board on top, info below), reduced app padding, smaller headings
- GameInfo: full-width on mobile, action buttons side-by-side
- GameOverDialog: reduced padding and margins on mobile
- NewGameDialog: wrapping option buttons on narrow screens
- Fits iPhone SE (375px) through desktop without horizontal scrolling

---

## Implementation Phases

### Phase 1: Project Skeleton + Domain Model -- COMPLETE
- Maven multi-module setup (parent, backend, frontend POMs)
- All model classes: Player, Position, Board, Game, GamePhase, GameVariant, AiDifficulty, MoveResult, MoveRecord
- Spring Boot app, WebConfig (CORS for localhost:5177), JacksonConfig, application.properties (port 8084)
- React Vite project, vite.config.ts (port 5177, proxy to 8084)
- Unit tests for Position (5 tests) and Board (10 tests)
- **Deliverable:** Both projects build, Spring Boot starts

### Phase 2: Core Game Engine -- COMPLETE
- FlipCalculator (10 tests), MoveValidator (10 tests), WinConditionChecker, GameEngine (11 tests)
- Comprehensive unit tests: flip calculation in all 8 directions, valid moves, pass detection, game over, Reversi placement
- **Deliverable:** All game logic working and tested

### Phase 3: REST API + Service Layer -- COMPLETE
- GameRepository (ConcurrentHashMap), GameService, DTOs (GameStateResponse with BoardDto/DiscDto, MoveResponseDto with MoveDataDto), GameController, GlobalExceptionHandler
- Consecutive-pass tracking, AI integration, surrender support
- Integration tests via MockMvc (10 tests)
- **Deliverable:** Complete game playable via curl

### Phase 4: AI Engine -- COMPLETE
- EasyAiStrategy (random valid move), MediumAiStrategy (positional weight table + flip count + mobility + corner adjustment), HardAiStrategy (minimax + alpha-beta, depth 6/10 endgame)
- AI tests (10 tests): valid move generation, corner preference, difficulty differentiation
- **Deliverable:** All 3 AI levels functional

### Phase 5: Frontend -- COMPLETE
- TypeScript types mirroring API, fetch-based API client with injectable auth headers
- useGame hook with useReducer: game state, valid moves auto-fetch, error auto-dismiss (4s), AI thinking state
- Board (CSS Grid 8x8), Disc (radial gradient, flip animation, last-placed glow), GameView, GameInfo (turn/score/actions), ScoreBar
- NewGameDialog (variant/difficulty/side selection), GameOverDialog (win/lose/draw overlay with scores)
- Full game flow: new game -> play with valid move highlights -> pass handling -> game over
- Reversi placement phase UI with hint text
- **Deliverable:** Fully playable in browser

### Phase 6: Polish -- COMPLETE
- Flip animation (scaleX CSS keyframes), AI move highlight (yellow glow), last-move indicator
- Error handling with auto-dismiss (4s timeout), loading states, AI thinking indicator (pulse animation)
- Surrender button, row/column labels (a-h, 1-8)
- Mobile responsive CSS: viewport-based board sizing, media queries at 400px and 480px breakpoints
- Dark theme (#1a1a2e background), green Reversio board (#2d7a2d)
- start.sh script (kills existing servers, starts both, cleanup on Ctrl+C), CLAUDE.md
- **Deliverable:** Production-quality game, works on desktop and mobile

### Phase 7: Deployment + Platform Integration -- PENDING
- Dockerfile (multi-stage eclipse-temurin:21)
- `GET /platform/health` endpoint
- JWT verification filter
- Update game-platform `games.json` with Reversio entry (backend 8084)
- README.md, Reversio_Rules.md
- **Deliverable:** Deployable, documented, platform-registered

---

## Verification Plan

1. **Unit tests:** `mvn test` -- 66 tests, all passing
   - PositionTest (5): validity, center squares, boundary
   - BoardTest (10): creation, placement, flipping, counting, deep copy, Reversio/Reversi starts
   - FlipCalculatorTest (10): all 8 directions, multi-direction, no flips, edge positions
   - MoveValidatorTest (10): valid/invalid moves, all valid moves, Reversi placement, has-valid-moves
   - GameEngineTest (11): execute move with flips, pass, placement, win conditions, consecutive passes
   - AiStrategyTest (10): all 3 difficulty levels return valid moves, differentiation
   - GameControllerTest (10): create game, get state, make move, pass, surrender, valid moves, error handling
2. **API testing:** curl flows for Reversio + Reversi, all difficulties, both sides, pass scenarios, surrender
3. **Browser testing:** Full games for each variant x difficulty x side combination. Force pass situations. Verify draw handling. Mobile viewport testing.
4. **Edge cases:** Opening moves, corner captures, consecutive passes, board full, 32-32 draw, AI as BLACK (first move on creation), Reversi center placement variations

---

## Rules Cross-Reference

| Rule | Implementation |
|------|---------------|
| 8x8 board | `Board.SIZE = 8` |
| Reversio: fixed diagonal center start | `Board.createReversioStart()` |
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
