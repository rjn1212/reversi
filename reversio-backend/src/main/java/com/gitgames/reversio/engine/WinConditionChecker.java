package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import org.springframework.stereotype.Component;

@Component
public class WinConditionChecker {

    private final MoveValidator moveValidator;

    public WinConditionChecker(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    public boolean isGameOver(Board board) {
        if (board.countEmpty() == 0) return true;
        return !moveValidator.hasValidMoves(board, Player.BLACK)
            && !moveValidator.hasValidMoves(board, Player.WHITE);
    }

    public Player getWinner(Board board) {
        int black = board.countDiscs(Player.BLACK);
        int white = board.countDiscs(Player.WHITE);
        if (black > white) return Player.BLACK;
        if (white > black) return Player.WHITE;
        return null;
    }

    public String getWinReason(Board board) {
        if (board.countEmpty() == 0) return "Board is full";
        return "No moves remaining for either player";
    }
}
