package com.gitgames.othello.controller.dto;

public record MakeMoveRequest(
    PositionDto position,
    Boolean pass
) {
    public boolean isPass() {
        return pass != null && pass;
    }
}
