package com.gitgames.othello.controller.dto;

import com.gitgames.othello.model.AiDifficulty;
import com.gitgames.othello.model.GameVariant;
import com.gitgames.othello.model.Player;

public record CreateGameRequest(
    AiDifficulty aiDifficulty,
    GameVariant variant,
    Player playerSide
) {}
