package com.gitgames.othello.ai;

import com.gitgames.othello.engine.MoveValidator;
import com.gitgames.othello.model.Board;
import com.gitgames.othello.model.Player;
import com.gitgames.othello.model.Position;

import java.util.List;
import java.util.Random;

public class EasyAiStrategy implements AiStrategy {

    private final MoveValidator moveValidator;
    private final Random random = new Random();

    public EasyAiStrategy(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

    @Override
    public Position selectMove(Board board, Player player) {
        List<Position> moves = moveValidator.getValidMoves(board, player);
        if (moves.isEmpty()) return null;
        return moves.get(random.nextInt(moves.size()));
    }
}
