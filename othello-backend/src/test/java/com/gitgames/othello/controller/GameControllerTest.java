package com.gitgames.othello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitgames.othello.controller.dto.CreateGameRequest;
import com.gitgames.othello.controller.dto.MakeMoveRequest;
import com.gitgames.othello.controller.dto.PositionDto;
import com.gitgames.othello.model.AiDifficulty;
import com.gitgames.othello.model.GameVariant;
import com.gitgames.othello.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOthelloGame() throws Exception {
        var request = new CreateGameRequest(AiDifficulty.EASY, GameVariant.OTHELLO, Player.BLACK);
        mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").isNotEmpty())
                .andExpect(jsonPath("$.phase").value("PLAYING"))
                .andExpect(jsonPath("$.variant").value("OTHELLO"))
                .andExpect(jsonPath("$.currentTurn").value("BLACK"))
                .andExpect(jsonPath("$.humanSide").value("BLACK"))
                .andExpect(jsonPath("$.board.size").value(8))
                .andExpect(jsonPath("$.board.discs", hasSize(4)))
                .andExpect(jsonPath("$.discCount.BLACK").value(2))
                .andExpect(jsonPath("$.discCount.WHITE").value(2));
    }

    @Test
    void createOthelloGameAsWhiteAiMovesFirst() throws Exception {
        var request = new CreateGameRequest(AiDifficulty.EASY, GameVariant.OTHELLO, Player.WHITE);
        mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentTurn").value("WHITE"))
                .andExpect(jsonPath("$.board.discs", hasSize(5)))
                .andExpect(jsonPath("$.moveNumber").value(1));
    }

    @Test
    void createReversiGame() throws Exception {
        var request = new CreateGameRequest(AiDifficulty.EASY, GameVariant.REVERSI, Player.BLACK);
        mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phase").value("PLACEMENT"))
                .andExpect(jsonPath("$.variant").value("REVERSI"))
                .andExpect(jsonPath("$.board.discs", hasSize(0)));
    }

    @Test
    void getValidMovesFromOpening() throws Exception {
        String gameId = createDefaultGame();

        mockMvc.perform(get("/api/v1/games/" + gameId + "/moves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validMoves", hasSize(4)))
                .andExpect(jsonPath("$.mustPass").value(false));
    }

    @Test
    void makeMoveAndGetAiResponse() throws Exception {
        String gameId = createDefaultGame();

        var moveRequest = new MakeMoveRequest(new PositionDto(2, 3), null);
        mockMvc.perform(post("/api/v1/games/" + gameId + "/moves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.humanMove.position.row").value(2))
                .andExpect(jsonPath("$.humanMove.position.col").value(3))
                .andExpect(jsonPath("$.humanMove.pass").value(false))
                .andExpect(jsonPath("$.humanMove.flipped", hasSize(1)))
                .andExpect(jsonPath("$.aiMove").isNotEmpty())
                .andExpect(jsonPath("$.aiMove.pass").value(false))
                .andExpect(jsonPath("$.gameState.currentTurn").value("BLACK"));
    }

    @Test
    void invalidMoveReturns400() throws Exception {
        String gameId = createDefaultGame();

        var moveRequest = new MakeMoveRequest(new PositionDto(0, 0), null);
        mockMvc.perform(post("/api/v1/games/" + gameId + "/moves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MOVE"));
    }

    @Test
    void gameNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/games/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GAME_NOT_FOUND"));
    }

    @Test
    void surrenderEndsGame() throws Exception {
        String gameId = createDefaultGame();

        mockMvc.perform(post("/api/v1/games/" + gameId + "/surrender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("GAME_OVER"))
                .andExpect(jsonPath("$.winner").value("WHITE"))
                .andExpect(jsonPath("$.winReason").value("Surrender"));
    }

    @Test
    void abandonGameDeletesIt() throws Exception {
        String gameId = createDefaultGame();

        mockMvc.perform(delete("/api/v1/games/" + gameId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGameState() throws Exception {
        String gameId = createDefaultGame();

        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.phase").value("PLAYING"));
    }

    private String createDefaultGame() throws Exception {
        var request = new CreateGameRequest(AiDifficulty.EASY, GameVariant.OTHELLO, Player.BLACK);
        MvcResult result = mockMvc.perform(post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("gameId").asText();
    }
}
