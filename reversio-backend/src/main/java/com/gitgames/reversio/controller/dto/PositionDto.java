package com.gitgames.reversio.controller.dto;

import com.gitgames.reversio.model.Position;

public record PositionDto(int row, int col) {

    public Position toModel() {
        return new Position(row, col);
    }

    public static PositionDto fromModel(Position pos) {
        return new PositionDto(pos.row(), pos.col());
    }
}
