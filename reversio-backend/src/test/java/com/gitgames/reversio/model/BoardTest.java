package com.gitgames.reversio.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void reversioStartHasFourDiscs() {
        Board board = Board.createReversioStart();
        assertEquals(2, board.countDiscs(Player.BLACK));
        assertEquals(2, board.countDiscs(Player.WHITE));
        assertEquals(60, board.countEmpty());
    }

    @Test
    void reversioStartDiagonalPattern() {
        Board board = Board.createReversioStart();
        assertEquals(Player.WHITE, board.getDiscAt(new Position(3, 3)));
        assertEquals(Player.BLACK, board.getDiscAt(new Position(3, 4)));
        assertEquals(Player.BLACK, board.getDiscAt(new Position(4, 3)));
        assertEquals(Player.WHITE, board.getDiscAt(new Position(4, 4)));
    }

    @Test
    void reversiStartIsEmpty() {
        Board board = Board.createReversiStart();
        assertEquals(0, board.countDiscs(Player.BLACK));
        assertEquals(0, board.countDiscs(Player.WHITE));
        assertEquals(64, board.countEmpty());
    }

    @Test
    void placeAndRetrieveDisc() {
        Board board = new Board();
        Position pos = new Position(2, 3);
        assertTrue(board.isEmpty(pos));

        board.placeDisc(pos, Player.BLACK);
        assertFalse(board.isEmpty(pos));
        assertEquals(Player.BLACK, board.getDiscAt(pos));
    }

    @Test
    void flipDisc() {
        Board board = new Board();
        Position pos = new Position(2, 3);
        board.placeDisc(pos, Player.BLACK);
        assertEquals(Player.BLACK, board.getDiscAt(pos));

        board.flipDisc(pos);
        assertEquals(Player.WHITE, board.getDiscAt(pos));

        board.flipDisc(pos);
        assertEquals(Player.BLACK, board.getDiscAt(pos));
    }

    @Test
    void flipEmptySquareIsNoOp() {
        Board board = new Board();
        Position pos = new Position(0, 0);
        board.flipDisc(pos);
        assertNull(board.getDiscAt(pos));
    }

    @Test
    void countDiscs() {
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(0, 1), Player.BLACK);
        board.placeDisc(new Position(0, 2), Player.WHITE);
        assertEquals(2, board.countDiscs(Player.BLACK));
        assertEquals(1, board.countDiscs(Player.WHITE));
        assertEquals(61, board.countEmpty());
    }

    @Test
    void deepCopyIsIndependent() {
        Board original = Board.createReversioStart();
        Board copy = original.deepCopy();

        copy.placeDisc(new Position(0, 0), Player.BLACK);
        assertNull(original.getDiscAt(new Position(0, 0)));
        assertEquals(Player.BLACK, copy.getDiscAt(new Position(0, 0)));
    }

    @Test
    void deepCopyPreservesState() {
        Board original = Board.createReversioStart();
        Board copy = original.deepCopy();

        assertEquals(original.getDiscAt(new Position(3, 3)), copy.getDiscAt(new Position(3, 3)));
        assertEquals(original.getDiscAt(new Position(3, 4)), copy.getDiscAt(new Position(3, 4)));
        assertEquals(original.countDiscs(Player.BLACK), copy.countDiscs(Player.BLACK));
        assertEquals(original.countDiscs(Player.WHITE), copy.countDiscs(Player.WHITE));
    }

    @Test
    void getOccupiedPositions() {
        Board board = Board.createReversioStart();
        List<Position> occupied = board.getOccupiedPositions();
        assertEquals(4, occupied.size());
    }
}
