package com.gitgames.reversio.model;

public record MoveRecord(Player player, Position position, int flippedCount, boolean wasPass) {
}
