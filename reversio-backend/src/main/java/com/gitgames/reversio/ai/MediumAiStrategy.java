package com.gitgames.reversio.ai;

import com.gitgames.reversio.engine.FlipCalculator;
import com.gitgames.reversio.engine.MoveValidator;
import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediumAiStrategy implements AiStrategy {

    private static final int[][] POSITION_WEIGHTS = {
        { 100, -20,  10,   5,   5,  10, -20,  100},
        { -20, -50,  -2,  -2,  -2,  -2, -50,  -20},
        {  10,  -2,   5,   1,   1,   5,  -2,   10},
        {   5,  -2,   1,   0,   0,   1,  -2,    5},
        {   5,  -2,   1,   0,   0,   1,  -2,    5},
        {  10,  -2,   5,   1,   1,   5,  -2,   10},
        { -20, -50,  -2,  -2,  -2,  -2, -50,  -20},
        { 100, -20,  10,   5,   5,  10, -20,  100}
    };

    private static final int[][] CORNERS = {{0, 0}, {0, 7}, {7, 0}, {7, 7}};
    private static final int[][][] ADJACENT_TO_CORNER = {
        {{0, 1}, {1, 0}, {1, 1}},
        {{0, 6}, {1, 7}, {1, 6}},
        {{6, 0}, {7, 1}, {6, 1}},
        {{6, 7}, {7, 6}, {6, 6}}
    };

    private final MoveValidator moveValidator;
    private final FlipCalculator flipCalculator;
    private final Random random = new Random();

    public MediumAiStrategy(MoveValidator moveValidator, FlipCalculator flipCalculator) {
        this.moveValidator = moveValidator;
        this.flipCalculator = flipCalculator;
    }

    @Override
    public Position selectMove(Board board, Player player) {
        List<Position> moves = moveValidator.getValidMoves(board, player);
        if (moves.isEmpty()) return null;

        int bestScore = Integer.MIN_VALUE;
        List<Position> bestMoves = new ArrayList<>();

        for (Position move : moves) {
            int score = scoreMove(board, move, player);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private int scoreMove(Board board, Position pos, Player player) {
        int score = 0;

        score += POSITION_WEIGHTS[pos.row()][pos.col()];
        score += adjustForCornerOwnership(board, pos, player);

        List<Position> flipped = flipCalculator.calculateFlips(board, pos, player);
        score += flipped.size() * 2;

        Board copy = board.deepCopy();
        copy.placeDisc(pos, player);
        for (Position f : flipped) copy.flipDisc(f);
        int opponentMoves = moveValidator.getValidMoves(copy, player.opponent()).size();
        score -= opponentMoves;

        return score;
    }

    private int adjustForCornerOwnership(Board board, Position pos, Player player) {
        int adjustment = 0;
        for (int i = 0; i < CORNERS.length; i++) {
            Position corner = new Position(CORNERS[i][0], CORNERS[i][1]);
            if (board.getDiscAt(corner) == player) {
                for (int[] adj : ADJACENT_TO_CORNER[i]) {
                    if (pos.row() == adj[0] && pos.col() == adj[1]) {
                        adjustment += 50;
                    }
                }
            }
        }
        return adjustment;
    }
}
