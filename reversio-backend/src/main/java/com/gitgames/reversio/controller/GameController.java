package com.gitgames.reversio.controller;

import com.gitgames.reversio.controller.dto.*;
import com.gitgames.reversio.model.Game;
import com.gitgames.reversio.model.Position;
import com.gitgames.reversio.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public ResponseEntity<GameStateResponse> createGame(@RequestBody CreateGameRequest request) {
        if (request.aiDifficulty() == null || request.variant() == null || request.playerSide() == null) {
            throw new IllegalArgumentException("aiDifficulty, variant, and playerSide are required");
        }
        Game game = gameService.createGame(request.aiDifficulty(), request.variant(), request.playerSide());
        return ResponseEntity.status(HttpStatus.CREATED).body(GameStateResponse.fromGame(game));
    }

    @GetMapping("/games/{id}")
    public GameStateResponse getGame(@PathVariable String id) {
        return GameStateResponse.fromGame(gameService.getGame(id));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> abandonGame(@PathVariable String id) {
        gameService.abandonGame(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/games/{id}/surrender")
    public GameStateResponse surrender(@PathVariable String id) {
        return GameStateResponse.fromGame(gameService.surrender(id));
    }

    @GetMapping("/games/{id}/moves")
    public ValidMovesResponse getValidMoves(@PathVariable String id) {
        List<Position> moves = gameService.getValidMoves(id);
        boolean mustPass = gameService.mustPass(id);
        return new ValidMovesResponse(
            moves.stream().map(PositionDto::fromModel).toList(),
            mustPass
        );
    }

    @PostMapping("/games/{id}/moves")
    public MoveResponseDto makeMove(@PathVariable String id, @RequestBody MakeMoveRequest request) {
        GameService.MoveResponse response;
        if (request.isPass()) {
            response = gameService.pass(id);
        } else {
            response = gameService.makeMove(id, request.position().toModel());
        }
        return MoveResponseDto.fromServiceResponse(response);
    }
}
