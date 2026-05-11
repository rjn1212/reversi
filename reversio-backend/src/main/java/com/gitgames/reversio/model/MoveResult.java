package com.gitgames.reversio.model;

import java.util.List;

public record MoveResult(Position position, List<Position> flipped, boolean pass) {

    public static MoveResult ofMove(Position position, List<Position> flipped) {
        return new MoveResult(position, flipped, false);
    }

    public static MoveResult ofPass() {
        return new MoveResult(null, List.of(), true);
    }
}
