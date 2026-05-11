package com.gitgames.reversio.ai;

import com.gitgames.reversio.engine.MoveValidator;
import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;

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
