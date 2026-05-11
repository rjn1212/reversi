package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.MoveResult;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameEngine {

    private final MoveValidator moveValidator;
    private final FlipCalculator flipCalculator;
    private final WinConditionChecker winConditionChecker;

    public GameEngine(MoveValidator moveValidator, FlipCalculator flipCalculator,
                      WinConditionChecker winConditionChecker) {
        this.moveValidator = moveValidator;
        this.flipCalculator = flipCalculator;
        this.winConditionChecker = winConditionChecker;
    }

    public MoveResult executeMove(Board board, Position pos, Player player) {
        List<Position> flipped = flipCalculator.calculateFlips(board, pos, player);
        board.placeDisc(pos, player);
        for (Position flip : flipped) {
            board.flipDisc(flip);
        }
        return MoveResult.ofMove(pos, flipped);
    }

    public MoveResult executePass() {
        return MoveResult.ofPass();
    }

    public MoveResult executePlacement(Board board, Position pos, Player player) {
        board.placeDisc(pos, player);
        return MoveResult.ofMove(pos, List.of());
    }

    public List<Position> getValidMoves(Board board, Player player) {
        return moveValidator.getValidMoves(board, player);
    }

    public boolean hasValidMoves(Board board, Player player) {
        return moveValidator.hasValidMoves(board, player);
    }

    public boolean isValidMove(Board board, Position pos, Player player) {
        return moveValidator.isValidMove(board, pos, player);
    }

    public boolean isValidPlacement(Board board, Position pos) {
        return moveValidator.isValidPlacement(board, pos);
    }

    public boolean isGameOver(Board board) {
        return winConditionChecker.isGameOver(board);
    }

    public Player getWinner(Board board) {
        return winConditionChecker.getWinner(board);
    }

    public String getWinReason(Board board) {
        return winConditionChecker.getWinReason(board);
    }
}
