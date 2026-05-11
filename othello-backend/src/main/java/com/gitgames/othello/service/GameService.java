package com.gitgames.othello.service;

import com.gitgames.othello.ai.AiStrategy;
import com.gitgames.othello.ai.EasyAiStrategy;
import com.gitgames.othello.ai.HardAiStrategy;
import com.gitgames.othello.ai.MediumAiStrategy;
import com.gitgames.othello.engine.FlipCalculator;
import com.gitgames.othello.engine.GameEngine;
import com.gitgames.othello.engine.MoveValidator;
import com.gitgames.othello.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameEngine engine;
    private final MoveValidator moveValidator;
    private final FlipCalculator flipCalculator;
    private final GameRepository repository;

    public GameService(GameEngine engine, MoveValidator moveValidator,
                       FlipCalculator flipCalculator, GameRepository repository) {
        this.engine = engine;
        this.moveValidator = moveValidator;
        this.flipCalculator = flipCalculator;
        this.repository = repository;
    }

    public Game createGame(AiDifficulty difficulty, GameVariant variant, Player humanSide) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Game game = new Game(id, difficulty, variant, humanSide);
        repository.save(game);

        if (game.getPhase() == GamePhase.PLAYING && game.isAiTurn()) {
            makeAiMove(game);
        }

        return game;
    }

    public Game getGame(String gameId) {
        return repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public List<Position> getValidMoves(String gameId) {
        Game game = getGame(gameId);
        if (game.getPhase() == GamePhase.GAME_OVER) return List.of();
        if (game.getPhase() == GamePhase.PLACEMENT) {
            return getValidPlacements(game);
        }
        return engine.getValidMoves(game.getBoard(), game.getCurrentTurn());
    }

    public boolean mustPass(String gameId) {
        Game game = getGame(gameId);
        if (game.getPhase() != GamePhase.PLAYING) return false;
        return !engine.hasValidMoves(game.getBoard(), game.getCurrentTurn());
    }

    public MoveResponse makeMove(String gameId, Position position) {
        Game game = getGame(gameId);
        validateGameActive(game);

        if (game.getPhase() == GamePhase.PLACEMENT) {
            return handlePlacement(game, position);
        }

        if (game.isAiTurn()) {
            throw new InvalidMoveException("It is not your turn");
        }

        if (!engine.isValidMove(game.getBoard(), position, game.getCurrentTurn())) {
            throw new InvalidMoveException("Position " + position + " is not a valid move");
        }

        MoveResult humanMove = engine.executeMove(game.getBoard(), position, game.getCurrentTurn());
        game.recordMove(new MoveRecord(game.getCurrentTurn(), position, humanMove.flipped().size(), false));
        game.setLastMoveWasPass(false);
        game.switchTurn();

        MoveResult aiMove = handlePostHumanMove(game);
        return new MoveResponse(humanMove, aiMove, game);
    }

    public MoveResponse pass(String gameId) {
        Game game = getGame(gameId);
        validateGameActive(game);

        if (game.getPhase() != GamePhase.PLAYING) {
            throw new InvalidMoveException("Cannot pass during " + game.getPhase() + " phase");
        }

        if (game.isAiTurn()) {
            throw new InvalidMoveException("It is not your turn");
        }

        if (engine.hasValidMoves(game.getBoard(), game.getCurrentTurn())) {
            throw new InvalidMoveException("You have valid moves and cannot pass");
        }

        MoveResult humanPass = engine.executePass();
        game.recordMove(new MoveRecord(game.getCurrentTurn(), null, 0, true));

        if (game.isLastMoveWasPass()) {
            endGame(game);
            return new MoveResponse(humanPass, null, game);
        }

        game.setLastMoveWasPass(true);
        game.switchTurn();

        MoveResult aiMove = handlePostHumanMove(game);
        return new MoveResponse(humanPass, aiMove, game);
    }

    public Game surrender(String gameId) {
        Game game = getGame(gameId);
        validateGameActive(game);
        game.setPhase(GamePhase.GAME_OVER);
        game.setWinner(game.getAiSide());
        game.setWinReason("Surrender");
        game.setLastActivityAt(Instant.now());
        return game;
    }

    public void abandonGame(String gameId) {
        repository.deleteById(gameId);
    }

    private MoveResult handlePostHumanMove(Game game) {
        if (engine.isGameOver(game.getBoard())) {
            endGame(game);
            return null;
        }

        if (!engine.hasValidMoves(game.getBoard(), game.getCurrentTurn())) {
            MoveResult aiPass = engine.executePass();
            game.recordMove(new MoveRecord(game.getCurrentTurn(), null, 0, true));

            if (game.isLastMoveWasPass()) {
                endGame(game);
                return aiPass;
            }

            game.setLastMoveWasPass(true);
            game.switchTurn();
            return aiPass;
        }

        return makeAiMove(game);
    }

    private MoveResult makeAiMove(Game game) {
        AiStrategy ai = createAiStrategy(game.getAiDifficulty());
        Position aiPosition = ai.selectMove(game.getBoard(), game.getCurrentTurn());

        if (aiPosition == null) {
            MoveResult aiPass = engine.executePass();
            game.recordMove(new MoveRecord(game.getCurrentTurn(), null, 0, true));
            if (game.isLastMoveWasPass()) {
                endGame(game);
            } else {
                game.setLastMoveWasPass(true);
                game.switchTurn();
            }
            return aiPass;
        }

        MoveResult aiMove = engine.executeMove(game.getBoard(), aiPosition, game.getCurrentTurn());
        game.recordMove(new MoveRecord(game.getCurrentTurn(), aiPosition, aiMove.flipped().size(), false));
        game.setLastMoveWasPass(false);
        game.switchTurn();

        if (engine.isGameOver(game.getBoard())) {
            endGame(game);
        }

        return aiMove;
    }

    private MoveResponse handlePlacement(Game game, Position position) {
        if (!engine.isValidPlacement(game.getBoard(), position)) {
            throw new InvalidMoveException("Position " + position + " is not a valid center placement");
        }

        MoveResult humanMove = engine.executePlacement(game.getBoard(), position, game.getCurrentTurn());
        game.recordMove(new MoveRecord(game.getCurrentTurn(), position, 0, false));
        game.switchTurn();

        int discsPlaced = game.getBoard().countDiscs(Player.BLACK) + game.getBoard().countDiscs(Player.WHITE);

        if (discsPlaced < 4 && game.isAiTurn()) {
            List<Position> placements = getValidPlacements(game);
            if (!placements.isEmpty()) {
                Position aiPos = placements.get(new java.util.Random().nextInt(placements.size()));
                MoveResult aiMove = engine.executePlacement(game.getBoard(), aiPos, game.getCurrentTurn());
                game.recordMove(new MoveRecord(game.getCurrentTurn(), aiPos, 0, false));
                game.switchTurn();

                discsPlaced = game.getBoard().countDiscs(Player.BLACK) + game.getBoard().countDiscs(Player.WHITE);
                if (discsPlaced >= 4) {
                    game.setPhase(GamePhase.PLAYING);
                }
                return new MoveResponse(humanMove, aiMove, game);
            }
        }

        if (discsPlaced >= 4) {
            game.setPhase(GamePhase.PLAYING);
            if (game.isAiTurn()) {
                MoveResult aiMove = makeAiMove(game);
                return new MoveResponse(humanMove, aiMove, game);
            }
        }

        return new MoveResponse(humanMove, null, game);
    }

    private List<Position> getValidPlacements(Game game) {
        return List.of(
            new Position(3, 3), new Position(3, 4),
            new Position(4, 3), new Position(4, 4)
        ).stream().filter(p -> game.getBoard().isEmpty(p)).toList();
    }

    private void endGame(Game game) {
        game.setPhase(GamePhase.GAME_OVER);
        game.setWinner(engine.getWinner(game.getBoard()));
        game.setWinReason(engine.getWinReason(game.getBoard()));
        game.setLastActivityAt(Instant.now());
    }

    private void validateGameActive(Game game) {
        if (game.getPhase() == GamePhase.GAME_OVER) {
            throw new InvalidMoveException("Game is already over");
        }
    }

    private AiStrategy createAiStrategy(AiDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> new EasyAiStrategy(moveValidator);
            case MEDIUM -> new MediumAiStrategy(moveValidator, flipCalculator);
            case HARD -> new HardAiStrategy(moveValidator, flipCalculator);
        };
    }

    public record MoveResponse(MoveResult humanMove, MoveResult aiMove, Game game) {}
}
