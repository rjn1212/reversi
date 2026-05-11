package com.gitgames.othello.model;

public record Position(int row, int col) {

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean isCenterSquare() {
        return (row == 3 || row == 4) && (col == 3 || col == 4);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
