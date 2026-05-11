package com.gitgames.reversio.ai;

import com.gitgames.reversio.engine.FlipCalculator;
import com.gitgames.reversio.engine.MoveValidator;
import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiStrategyTest {

    private MoveValidator moveValidator;
    private FlipCalculator flipCalculator;

    @BeforeEach
    void setUp() {
        flipCalculator = new FlipCalculator();
        moveValidator = new MoveValidator(flipCalculator);
    }

    @Test
    void easyAiReturnsValidMove() {
        EasyAiStrategy easy = new EasyAiStrategy(moveValidator);
        Board board = Board.createReversioStart();
        Position move = easy.selectMove(board, Player.BLACK);

        assertNotNull(move);
        assertTrue(moveValidator.isValidMove(board, move, Player.BLACK));
    }

    @Test
    void easyAiReturnsNullWhenNoMoves() {
        EasyAiStrategy easy = new EasyAiStrategy(moveValidator);
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        Position move = easy.selectMove(board, Player.BLACK);
        assertNull(move);
    }

    @Test
    void mediumAiReturnsValidMove() {
        MediumAiStrategy medium = new MediumAiStrategy(moveValidator, flipCalculator);
        Board board = Board.createReversioStart();
        Position move = medium.selectMove(board, Player.BLACK);

        assertNotNull(move);
        assertTrue(moveValidator.isValidMove(board, move, Player.BLACK));
    }

    @Test
    void mediumAiPrefersCorners() {
        MediumAiStrategy medium = new MediumAiStrategy(moveValidator, flipCalculator);
        Board board = new Board();
        // Set up so corner (0,0) is a valid move for BLACK
        board.placeDisc(new Position(0, 1), Player.WHITE);
        board.placeDisc(new Position(0, 2), Player.BLACK);
        // Also make (1,0) valid (less desirable)
        board.placeDisc(new Position(2, 0), Player.BLACK);
        board.placeDisc(new Position(1, 0), Player.WHITE);
        // Place an anchor so (0,0) captures via column too
        // Actually just check if corner is available, medium should pick it
        List<Position> valid = moveValidator.getValidMoves(board, Player.BLACK);
        if (valid.contains(new Position(0, 0))) {
            Position move = medium.selectMove(board, Player.BLACK);
            assertEquals(new Position(0, 0), move);
        }
    }

    @Test
    void mediumAiReturnsNullWhenNoMoves() {
        MediumAiStrategy medium = new MediumAiStrategy(moveValidator, flipCalculator);
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        assertNull(medium.selectMove(board, Player.BLACK));
    }

    @Test
    void hardAiReturnsValidMove() {
        HardAiStrategy hard = new HardAiStrategy(moveValidator, flipCalculator);
        Board board = Board.createReversioStart();
        Position move = hard.selectMove(board, Player.BLACK);

        assertNotNull(move);
        assertTrue(moveValidator.isValidMove(board, move, Player.BLACK));
    }

    @Test
    void hardAiReturnsNullWhenNoMoves() {
        HardAiStrategy hard = new HardAiStrategy(moveValidator, flipCalculator);
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        assertNull(hard.selectMove(board, Player.BLACK));
    }

    @Test
    void hardAiTakesCornerWhenAvailable() {
        HardAiStrategy hard = new HardAiStrategy(moveValidator, flipCalculator);
        Board board = new Board();
        // Set up so (0,0) corner is clearly the best move
        board.placeDisc(new Position(0, 1), Player.WHITE);
        board.placeDisc(new Position(0, 2), Player.BLACK);
        board.placeDisc(new Position(1, 0), Player.WHITE);
        board.placeDisc(new Position(2, 0), Player.BLACK);

        List<Position> valid = moveValidator.getValidMoves(board, Player.BLACK);
        if (valid.contains(new Position(0, 0))) {
            Position move = hard.selectMove(board, Player.BLACK);
            assertEquals(new Position(0, 0), move);
        }
    }

    @Test
    void hardAiCompletesWithinReasonableTime() {
        HardAiStrategy hard = new HardAiStrategy(moveValidator, flipCalculator);
        Board board = Board.createReversioStart();

        long start = System.currentTimeMillis();
        hard.selectMove(board, Player.BLACK);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 10000, "Hard AI took too long: " + elapsed + "ms");
    }

    @Test
    void allAiStrategiesWorkForWhite() {
        Board board = Board.createReversioStart();
        // After BLACK plays, WHITE should be able to respond
        board.placeDisc(new Position(2, 3), Player.BLACK);
        board.flipDisc(new Position(3, 3));

        assertNotNull(new EasyAiStrategy(moveValidator).selectMove(board, Player.WHITE));
        assertNotNull(new MediumAiStrategy(moveValidator, flipCalculator).selectMove(board, Player.WHITE));
        assertNotNull(new HardAiStrategy(moveValidator, flipCalculator).selectMove(board, Player.WHITE));
    }
}
