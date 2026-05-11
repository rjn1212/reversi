package com.gitgames.reversio.ai;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;

public interface AiStrategy {
    Position selectMove(Board board, Player player);
}
