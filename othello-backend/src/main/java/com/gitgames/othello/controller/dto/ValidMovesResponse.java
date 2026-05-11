package com.gitgames.othello.controller.dto;

import java.util.List;

public record ValidMovesResponse(
    List<PositionDto> validMoves,
    boolean mustPass
) {}
