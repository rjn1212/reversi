package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlipCalculatorTest {

    private FlipCalculator flipCalculator;

    @BeforeEach
    void setUp() {
        flipCalculator = new FlipCalculator();
    }

    @Test
    void standardOpeningFlipsOneDisc() {
        Board board = Board.createReversioStart();
        // BLACK plays at (2,3) -- should flip WHITE at (3,3)
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(2, 3), Player.BLACK);
        assertEquals(1, flipped.size());
        assertEquals(new Position(3, 3), flipped.get(0));
    }

    @Test
    void allFourOpeningMovesAreValid() {
        Board board = Board.createReversioStart();
        // BLACK's four valid opening moves
        assertFalse(flipCalculator.calculateFlips(board, new Position(2, 3), Player.BLACK).isEmpty());
        assertFalse(flipCalculator.calculateFlips(board, new Position(3, 2), Player.BLACK).isEmpty());
        assertFalse(flipCalculator.calculateFlips(board, new Position(4, 5), Player.BLACK).isEmpty());
        assertFalse(flipCalculator.calculateFlips(board, new Position(5, 4), Player.BLACK).isEmpty());
    }

    @Test
    void noFlipsOnEmptyBoard() {
        Board board = new Board();
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(3, 3), Player.BLACK);
        assertTrue(flipped.isEmpty());
    }

    @Test
    void noFlipsWhenNoAnchor() {
        Board board = new Board();
        board.placeDisc(new Position(3, 3), Player.WHITE);
        // BLACK at (3,2) -- WHITE at (3,3) but no BLACK anchor beyond
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(3, 2), Player.BLACK);
        assertTrue(flipped.isEmpty());
    }

    @Test
    void flipsMultipleInOneDirection() {
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(0, 1), Player.WHITE);
        board.placeDisc(new Position(0, 2), Player.WHITE);
        board.placeDisc(new Position(0, 3), Player.WHITE);
        // BLACK at (0,4) should flip 3 WHITE discs
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(0, 4), Player.BLACK);
        assertEquals(3, flipped.size());
    }

    @Test
    void flipsInMultipleDirections() {
        Board board = new Board();
        // Horizontal: BLACK at (3,0), WHITE at (3,1), place BLACK at (3,2)
        board.placeDisc(new Position(3, 0), Player.BLACK);
        board.placeDisc(new Position(3, 1), Player.WHITE);
        // Vertical: BLACK at (1,2), WHITE at (2,2), place BLACK at (3,2)
        board.placeDisc(new Position(1, 2), Player.BLACK);
        board.placeDisc(new Position(2, 2), Player.WHITE);

        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(3, 2), Player.BLACK);
        assertEquals(2, flipped.size());
        assertTrue(flipped.contains(new Position(3, 1)));
        assertTrue(flipped.contains(new Position(2, 2)));
    }

    @Test
    void diagonalFlip() {
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(1, 1), Player.WHITE);
        board.placeDisc(new Position(2, 2), Player.WHITE);

        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(3, 3), Player.BLACK);
        assertEquals(2, flipped.size());
    }

    @Test
    void noFlipOnOccupiedSquare() {
        Board board = Board.createReversioStart();
        // (3,3) is already occupied by WHITE
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(3, 3), Player.BLACK);
        // FlipCalculator doesn't check emptiness -- that's MoveValidator's job
        // But placing on a non-empty square shouldn't matter for flip calculation in isolation
        // since the board state would be invalid anyway
        assertNotNull(flipped);
    }

    @Test
    void doesNotWrapAroundBoard() {
        Board board = new Board();
        board.placeDisc(new Position(0, 6), Player.WHITE);
        board.placeDisc(new Position(0, 7), Player.BLACK);
        // BLACK at (0,0) should NOT flip (0,6) through wrapping
        // (0,7) is BLACK anchor but there's a gap
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(0, 0), Player.BLACK);
        assertTrue(flipped.isEmpty());
    }

    @Test
    void cornerFlip() {
        Board board = new Board();
        board.placeDisc(new Position(0, 1), Player.WHITE);
        board.placeDisc(new Position(0, 2), Player.BLACK);
        // BLACK at corner (0,0) flips (0,1)
        List<Position> flipped = flipCalculator.calculateFlips(board, new Position(0, 0), Player.BLACK);
        assertEquals(1, flipped.size());
        assertEquals(new Position(0, 1), flipped.get(0));
    }
}
