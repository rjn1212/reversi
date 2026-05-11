package com.gitgames.reversio.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final int SIZE = 8;

    private final Player[][] grid = new Player[SIZE][SIZE];

    public Player getDiscAt(Position pos) {
        return grid[pos.row()][pos.col()];
    }

    public void placeDisc(Position pos, Player color) {
        grid[pos.row()][pos.col()] = color;
    }

    public void flipDisc(Position pos) {
        Player current = grid[pos.row()][pos.col()];
        if (current != null) {
            grid[pos.row()][pos.col()] = current.opponent();
        }
    }

    public boolean isEmpty(Position pos) {
        return grid[pos.row()][pos.col()] == null;
    }

    public int countDiscs(Player color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == color) count++;
            }
        }
        return count;
    }

    public int countEmpty() {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == null) count++;
            }
        }
        return count;
    }

    public List<Position> getOccupiedPositions() {
        List<Position> positions = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != null) {
                    positions.add(new Position(r, c));
                }
            }
        }
        return positions;
    }

    public Board deepCopy() {
        Board copy = new Board();
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(this.grid[r], 0, copy.grid[r], 0, SIZE);
        }
        return copy;
    }

    public static Board createReversioStart() {
        Board board = new Board();
        board.placeDisc(new Position(3, 3), Player.WHITE);
        board.placeDisc(new Position(3, 4), Player.BLACK);
        board.placeDisc(new Position(4, 3), Player.BLACK);
        board.placeDisc(new Position(4, 4), Player.WHITE);
        return board;
    }

    public static Board createReversiStart() {
        return new Board();
    }
}
