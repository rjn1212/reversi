package com.gitgames.reversio.controller.dto;

import com.gitgames.reversio.model.AiDifficulty;
import com.gitgames.reversio.model.GameVariant;
import com.gitgames.reversio.model.Player;

public record CreateGameRequest(
    AiDifficulty aiDifficulty,
    GameVariant variant,
    Player playerSide
) {}
