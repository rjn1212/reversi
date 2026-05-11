package com.gitgames.othello.controller.dto;

import com.gitgames.othello.model.*;

import java.util.List;
import java.util.Map;

public record GameStateResponse(
    String gameId,
    GamePhase phase,
    GameVariant variant,
    Player currentTurn,
    Player humanSide,
    AiDifficulty aiDifficulty,
    BoardDto board,
    Map<Player, Integer> discCount,
    int moveNumber,
    Player winner,
    String winReason
) {
    public static GameStateResponse fromGame(Game game) {
        List<DiscDto> discs = game.getBoard().getOccupiedPositions().stream()
                .map(pos -> new DiscDto(PositionDto.fromModel(pos), game.getBoard().getDiscAt(pos)))
                .toList();

        return new GameStateResponse(
            game.getId(),
            game.getPhase(),
            game.getVariant(),
            game.getCurrentTurn(),
            game.getHumanSide(),
            game.getAiDifficulty(),
            new BoardDto(Board.SIZE, discs),
            Map.of(
                Player.BLACK, game.getBoard().countDiscs(Player.BLACK),
                Player.WHITE, game.getBoard().countDiscs(Player.WHITE)
            ),
            game.getMoveNumber(),
            game.getWinner(),
            game.getWinReason()
        );
    }

    public record BoardDto(int size, List<DiscDto> discs) {}
    public record DiscDto(PositionDto position, Player color) {}
}
