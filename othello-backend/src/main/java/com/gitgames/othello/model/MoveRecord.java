package com.gitgames.othello.model;

public record MoveRecord(Player player, Position position, int flippedCount, boolean wasPass) {
}
