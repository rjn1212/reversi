package com.gitgames.othello.engine;

import com.gitgames.othello.model.Board;
import com.gitgames.othello.model.Player;
import com.gitgames.othello.model.Position;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlipCalculator {

    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        { 0, -1},          { 0, 1},
        { 1, -1}, { 1, 0}, { 1, 1}
    };

    public List<Position> calculateFlips(Board board, Position pos, Player player) {
        List<Position> allFlipped = new ArrayList<>();
        for (int[] dir : DIRECTIONS) {
            allFlipped.addAll(scanDirection(board, pos, dir[0], dir[1], player));
        }
        return allFlipped;
    }

    private List<Position> scanDirection(Board board, Position pos, int dRow, int dCol, Player player) {
        List<Position> candidates = new ArrayList<>();
        int r = pos.row() + dRow;
        int c = pos.col() + dCol;

        while (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE) {
            Player disc = board.getDiscAt(new Position(r, c));
            if (disc == null) {
                return List.of();
            }
            if (disc == player) {
                return candidates;
            }
            candidates.add(new Position(r, c));
            r += dRow;
            c += dCol;
        }
        return List.of();
    }
}
