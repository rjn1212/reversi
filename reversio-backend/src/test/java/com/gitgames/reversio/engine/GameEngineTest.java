package com.gitgames.reversio.engine;

import com.gitgames.reversio.model.Board;
import com.gitgames.reversio.model.MoveResult;
import com.gitgames.reversio.model.Player;
import com.gitgames.reversio.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        FlipCalculator flipCalc = new FlipCalculator();
        MoveValidator moveVal = new MoveValidator(flipCalc);
        WinConditionChecker winCheck = new WinConditionChecker(moveVal);
        engine = new GameEngine(moveVal, flipCalc, winCheck);
    }

    @Test
    void executeMoveFlipsDiscs() {
        Board board = Board.createReversioStart();
        MoveResult result = engine.executeMove(board, new Position(2, 3), Player.BLACK);

        assertFalse(result.pass());
        assertEquals(new Position(2, 3), result.position());
        assertEquals(1, result.flipped().size());
        assertEquals(new Position(3, 3), result.flipped().get(0));

        // Verify board state
        assertEquals(Player.BLACK, board.getDiscAt(new Position(2, 3)));
        assertEquals(Player.BLACK, board.getDiscAt(new Position(3, 3))); // was WHITE, now flipped
        assertEquals(4, board.countDiscs(Player.BLACK));
        assertEquals(1, board.countDiscs(Player.WHITE));
    }

    @Test
    void executePassReturnsPassResult() {
        MoveResult result = engine.executePass();
        assertTrue(result.pass());
        assertNull(result.position());
        assertTrue(result.flipped().isEmpty());
    }

    @Test
    void executePlacementNoFlips() {
        Board board = new Board();
        MoveResult result = engine.executePlacement(board, new Position(3, 3), Player.BLACK);

        assertFalse(result.pass());
        assertEquals(new Position(3, 3), result.position());
        assertTrue(result.flipped().isEmpty());
        assertEquals(Player.BLACK, board.getDiscAt(new Position(3, 3)));
    }

    @Test
    void gameNotOverAtStart() {
        Board board = Board.createReversioStart();
        assertFalse(engine.isGameOver(board));
    }

    @Test
    void gameOverWhenBoardFull() {
        Board board = new Board();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                board.placeDisc(new Position(r, c), (r + c) % 2 == 0 ? Player.BLACK : Player.WHITE);
            }
        }
        assertTrue(engine.isGameOver(board));
    }

    @Test
    void gameOverWhenNeitherCanMove() {
        // All BLACK in one corner, all WHITE in another, no outflanking possible
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(7, 7), Player.WHITE);
        assertTrue(engine.isGameOver(board));
    }

    @Test
    void winnerIsPlayerWithMoreDiscs() {
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(0, 1), Player.BLACK);
        board.placeDisc(new Position(0, 2), Player.WHITE);
        assertEquals(Player.BLACK, engine.getWinner(board));
    }

    @Test
    void drawWhenEqualDiscs() {
        Board board = new Board();
        board.placeDisc(new Position(0, 0), Player.BLACK);
        board.placeDisc(new Position(0, 1), Player.WHITE);
        assertNull(engine.getWinner(board));
    }

    @Test
    void validMovesFromOpening() {
        Board board = Board.createReversioStart();
        List<Position> moves = engine.getValidMoves(board, Player.BLACK);
        assertEquals(4, moves.size());
    }

    @Test
    void multipleFlipsInSingleMove() {
        Board board = new Board();
        // Set up a position where BLACK can flip in two directions
        board.placeDisc(new Position(3, 0), Player.BLACK);
        board.placeDisc(new Position(3, 1), Player.WHITE);
        board.placeDisc(new Position(1, 2), Player.BLACK);
        board.placeDisc(new Position(2, 2), Player.WHITE);

        MoveResult result = engine.executeMove(board, new Position(3, 2), Player.BLACK);
        assertEquals(2, result.flipped().size());
        assertEquals(Player.BLACK, board.getDiscAt(new Position(3, 1)));
        assertEquals(Player.BLACK, board.getDiscAt(new Position(2, 2)));
    }

    @Test
    void fullGameSequenceFromOpening() {
        Board board = Board.createReversioStart();
        // Play a few known opening moves
        engine.executeMove(board, new Position(2, 3), Player.BLACK); // d3
        assertEquals(4, board.countDiscs(Player.BLACK));
        assertEquals(1, board.countDiscs(Player.WHITE));

        engine.executeMove(board, new Position(2, 2), Player.WHITE); // c3
        assertEquals(3, board.countDiscs(Player.BLACK));
        assertEquals(3, board.countDiscs(Player.WHITE));
    }
}
