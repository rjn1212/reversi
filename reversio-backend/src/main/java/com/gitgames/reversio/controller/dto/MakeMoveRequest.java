package com.gitgames.reversio.controller.dto;

public record MakeMoveRequest(
    PositionDto position,
    Boolean pass
) {
    public boolean isPass() {
        return pass != null && pass;
    }
}
