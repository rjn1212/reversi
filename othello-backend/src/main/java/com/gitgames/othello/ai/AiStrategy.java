package com.gitgames.othello.ai;

import com.gitgames.othello.model.Board;
import com.gitgames.othello.model.Player;
import com.gitgames.othello.model.Position;

public interface AiStrategy {
    Position selectMove(Board board, Player player);
}
