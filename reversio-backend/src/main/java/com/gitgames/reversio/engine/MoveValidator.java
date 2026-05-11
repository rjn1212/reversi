package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MoveValidator {

    private final FlipCalculator flipCalculator;

    public MoveValidator(FlipCalculator flipCalculator) {
        this.flipCalculator = flipCalculator;
    }

    public boolean isValidMove(Board board, Position pos, Player player) {
        if (!pos.isValid() || !board.isEmpty(pos)) return false;
        return !flipCalculator.calculateFlips(board, pos, player).isEmpty();
    }

    public List<Position> getValidMoves(Board board, Player player) {
        List<Position> moves = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Position pos = new Position(row, col);
                if (isValidMove(board, pos, player)) {
                    moves.add(pos);
                }
            }
        }
        return moves;
    }

    public boolean hasValidMoves(Board board, Player player) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (isValidMove(board, new Position(row, col), player)) return true;
            }
        }
        return false;
    }

    public boolean isValidPlacement(Board board, Position pos) {
        return pos.isValid() && pos.isCenterSquare() && board.isEmpty(pos);
    }
}
