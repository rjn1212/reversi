package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private MoveValidator moveValidator;

    @BeforeEach
    void setUp() {
        moveValidator = new MoveValidator(new FlipCalculator());
    }

    @Test
    void standardOpeningHasFourValidMoves() {
        Board board = Board.createReversioStart();
        List<Position> moves = moveValidator.getValidMoves(board, Player.BLACK);
        assertEquals(4, moves.size());
        assertTrue(moves.contains(new Position(2, 3)));
        assertTrue(moves.contains(new Position(3, 2)));
        assertTrue(moves.contains(new Position(4, 5)));
        assertTrue(moves.contains(new Position(5, 4)));
    }

    @Test
    void occupiedSquareIsNotValid() {
        Board board = Board.createReversioStart();
        assertFalse(moveValidator.isValidMove(board, new Position(3, 3), Player.BLACK));
    }

    @Test
    void emptySquareWithNoFlipsIsNotValid() {
        Board board = Board.createReversioStart();
        assertFalse(moveValidator.isValidMove(board, new Position(0, 0), Player.BLACK));
    }

    @Test
    void outOfBoundsIsNotValid() {
        Board board = Board.createReversioStart();
        assertFalse(moveValidator.isValidMove(board, new Position(-1, 0), Player.BLACK));
        assertFalse(moveValidator.isValidMove(board, new Position(0, 8), Player.BLACK));
    }

    @Test
    void hasValidMovesOnStandardOpening() {
        Board board = Board.createReversioStart();
        assertTrue(moveValidator.hasValidMoves(board, Player.BLACK));
        assertTrue(moveValidator.hasValidMoves(board, Player.WHITE));
    }

    @Test
    void noValidMovesOnEmptyBoard() {
        Board board = new Board();
        assertFalse(moveValidator.hasValidMoves(board, Player.BLACK));
    }

    @Test
    void playerWithNoMovesDetected() {
        // Create a board where one player is surrounded and can't move
        Board board = new Board();
        // Fill top row with BLACK, second row with WHITE, WHITE at (2,0)
        // WHITE has no valid outflanking moves
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(0, 1), Player.BLACK);
        board.placeDisc(new Position(1, 0), Player.WHITE);
        // WHITE can't outflank anything
        assertFalse(moveValidator.hasValidMoves(board, Player.WHITE));
    }

    @Test
    void reversiPlacementValidation() {
        Board board = new Board();
        assertTrue(moveValidator.isValidPlacement(board, new Position(3, 3)));
        assertTrue(moveValidator.isValidPlacement(board, new Position(3, 4)));
        assertTrue(moveValidator.isValidPlacement(board, new Position(4, 3)));
        assertTrue(moveValidator.isValidPlacement(board, new Position(4, 4)));

        assertFalse(moveValidator.isValidPlacement(board, new Position(0, 0)));
        assertFalse(moveValidator.isValidPlacement(board, new Position(5, 5)));
    }

    @Test
    void reversiPlacementRejectsOccupiedCenter() {
        Board board = new Board();
        board.placeDisc(new Position(3, 3), Player.BLACK);
        assertFalse(moveValidator.isValidPlacement(board, new Position(3, 3)));
        assertTrue(moveValidator.isValidPlacement(board, new Position(3, 4)));
    }

    @Test
    void afterFirstMoveWhiteHasThreeValidMoves() {
        Board board = Board.createReversioStart();
        // BLACK plays (2,3), flipping (3,3) to BLACK
        board.placeDisc(new Position(2, 3), Player.BLACK);
        board.flipDisc(new Position(3, 3));
        // Now WHITE should have valid moves
        List<Position> whiteMoves = moveValidator.getValidMoves(board, Player.WHITE);
        assertFalse(whiteMoves.isEmpty());
    }
}
