package com.gitgames.othello.controller.dto;

import com.gitgames.othello.model.MoveResult;
import com.gitgames.othello.service.GameService;

import java.util.List;

public record MoveResponseDto(
    MoveDataDto humanMove,
    MoveDataDto aiMove,
    GameStateResponse gameState
) {
    public static MoveResponseDto fromServiceResponse(GameService.MoveResponse response) {
        return new MoveResponseDto(
            MoveDataDto.fromMoveResult(response.humanMove()),
            response.aiMove() != null ? MoveDataDto.fromMoveResult(response.aiMove()) : null,
            GameStateResponse.fromGame(response.game())
        );
    }

    public record MoveDataDto(
        PositionDto position,
        List<PositionDto> flipped,
        boolean pass
    ) {
        public static MoveDataDto fromMoveResult(MoveResult result) {
            return new MoveDataDto(
                result.position() != null ? PositionDto.fromModel(result.position()) : null,
                result.flipped().stream().map(PositionDto::fromModel).toList(),
                result.pass()
            );
        }
    }
}
