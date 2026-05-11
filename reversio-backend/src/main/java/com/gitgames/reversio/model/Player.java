package com.gitgames.reversio.model;

public enum Player {
    BLACK,
    WHITE;

    public Player opponent() {
        return this == BLACK ? WHITE : BLACK;
    }
}
