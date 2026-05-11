package com.gitgames.reversio.controller.dto;

import java.util.List;

public record ValidMovesResponse(
    List<PositionDto> validMoves,
    boolean mustPass
) {}
