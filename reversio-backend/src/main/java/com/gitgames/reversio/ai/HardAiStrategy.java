package com.gitgames.reversio.ai;

import com.gitgames.reversio.engine.FlipCalculator;
import com.gitgames.reversio.engine.MoveValidator;
import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HardAiStrategy implements AiStrategy {

    private static final int MAX_DEPTH = 6;
    private static final int ENDGAME_DEPTH = 10;

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

    private static final Position[] CORNERS = {
        new Position(0, 0), new Position(0, 7),
        new Position(7, 0), new Position(7, 7)
    };

    private final MoveValidator moveValidator;
    private final FlipCalculator flipCalculator;
    private final Random random = new Random();

    public HardAiStrategy(MoveValidator moveValidator, FlipCalculator flipCalculator) {
        this.moveValidator = moveValidator;
        this.flipCalculator = flipCalculator;
    }

    @Override
    public Position selectMove(Board board, Player player) {
        List<Position> moves = moveValidator.getValidMoves(board, player);
        if (moves.isEmpty()) return null;

        int depth = board.countEmpty() <= 12 ? ENDGAME_DEPTH : MAX_DEPTH;

        int bestScore = Integer.MIN_VALUE;
        List<Position> bestMoves = new ArrayList<>();

        for (Position move : moves) {
            Board copy = board.deepCopy();
            applyMove(copy, move, player);
            int score = minimax(copy, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, player);
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

    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizing, Player aiPlayer) {
        if (depth == 0 || isTerminal(board)) {
            return evaluate(board, aiPlayer);
        }

        Player current = maximizing ? aiPlayer : aiPlayer.opponent();
        List<Position> moves = moveValidator.getValidMoves(board, current);

        if (moves.isEmpty()) {
            Player other = current.opponent();
            if (!moveValidator.hasValidMoves(board, other)) {
                return evaluate(board, aiPlayer);
            }
            return minimax(board, depth, alpha, beta, !maximizing, aiPlayer);
        }

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Position move : moves) {
                Board copy = board.deepCopy();
                applyMove(copy, move, current);
                int eval = minimax(copy, depth - 1, alpha, beta, false, aiPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Position move : moves) {
                Board copy = board.deepCopy();
                applyMove(copy, move, current);
                int eval = minimax(copy, depth - 1, alpha, beta, true, aiPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluate(Board board, Player aiPlayer) {
        Player opponent = aiPlayer.opponent();
        int score = 0;

        int myDiscs = board.countDiscs(aiPlayer);
        int oppDiscs = board.countDiscs(opponent);
        int emptyCount = board.countEmpty();

        if (emptyCount <= 10) {
            score += (myDiscs - oppDiscs) * 10;
        } else {
            score += (myDiscs - oppDiscs);
        }

        int myCorners = countCorners(board, aiPlayer);
        int oppCorners = countCorners(board, opponent);
        score += (myCorners - oppCorners) * 100;

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Player disc = board.getDiscAt(new Position(row, col));
                if (disc == aiPlayer) {
                    score += POSITION_WEIGHTS[row][col];
                } else if (disc == opponent) {
                    score -= POSITION_WEIGHTS[row][col];
                }
            }
        }

        int myMobility = moveValidator.getValidMoves(board, aiPlayer).size();
        int oppMobility = moveValidator.getValidMoves(board, opponent).size();
        score += (myMobility - oppMobility) * 5;

        int myStable = countStableDiscs(board, aiPlayer);
        int oppStable = countStableDiscs(board, opponent);
        score += (myStable - oppStable) * 15;

        int myFrontier = countFrontierDiscs(board, aiPlayer);
        int oppFrontier = countFrontierDiscs(board, opponent);
        score -= (myFrontier - oppFrontier) * 3;

        return score;
    }

    private int countCorners(Board board, Player player) {
        int count = 0;
        for (Position corner : CORNERS) {
            if (board.getDiscAt(corner) == player) count++;
        }
        return count;
    }

    private int countStableDiscs(Board board, Player player) {
        boolean[][] stable = new boolean[Board.SIZE][Board.SIZE];
        int count = 0;

        for (Position corner : CORNERS) {
            if (board.getDiscAt(corner) == player) {
                count += floodFillStable(board, stable, corner.row(), corner.col(), player);
            }
        }
        return count;
    }

    private int floodFillStable(Board board, boolean[][] stable, int startRow, int startCol, Player player) {
        int dRow = startRow == 0 ? 1 : -1;
        int dCol = startCol == 0 ? 1 : -1;
        int count = 0;

        for (int r = startRow; r >= 0 && r < Board.SIZE; r += dRow) {
            boolean rowStable = true;
            for (int c = startCol; c >= 0 && c < Board.SIZE; c += dCol) {
                if (board.getDiscAt(new Position(r, c)) == player && rowStable && isColumnStable(board, stable, r, c, startCol, dCol, player)) {
                    if (!stable[r][c]) {
                        stable[r][c] = true;
                        count++;
                    }
                } else {
                    rowStable = false;
                }
            }
            if (!stable[r][startCol]) break;
        }
        return count;
    }

    private boolean isColumnStable(Board board, boolean[][] stable, int row, int col, int startCol, int dCol, Player player) {
        if (col == startCol) return true;
        int prevCol = col - dCol;
        return stable[row][prevCol];
    }

    private int countFrontierDiscs(Board board, Player player) {
        int count = 0;
        int[][] dirs = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.getDiscAt(new Position(r, c)) != player) continue;
                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < Board.SIZE && nc >= 0 && nc < Board.SIZE
                            && board.isEmpty(new Position(nr, nc))) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    private boolean isTerminal(Board board) {
        if (board.countEmpty() == 0) return true;
        return !moveValidator.hasValidMoves(board, Player.BLACK)
            && !moveValidator.hasValidMoves(board, Player.WHITE);
    }

    private void applyMove(Board board, Position pos, Player player) {
        List<Position> flipped = flipCalculator.calculateFlips(board, pos, player);
        board.placeDisc(pos, player);
        for (Position f : flipped) {
            board.flipDisc(f);
        }
    }
}
