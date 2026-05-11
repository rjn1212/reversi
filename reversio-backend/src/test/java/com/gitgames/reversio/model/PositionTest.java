package com.gitgames.reversio.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void validPositions() {
        assertTrue(new Position(0, 0).isValid());
        assertTrue(new Position(7, 7).isValid());
        assertTrue(new Position(3, 4).isValid());
    }

    @Test
    void invalidPositions() {
        assertFalse(new Position(-1, 0).isValid());
        assertFalse(new Position(0, -1).isValid());
        assertFalse(new Position(8, 0).isValid());
        assertFalse(new Position(0, 8).isValid());
    }

    @Test
    void centerSquares() {
        assertTrue(new Position(3, 3).isCenterSquare());
        assertTrue(new Position(3, 4).isCenterSquare());
        assertTrue(new Position(4, 3).isCenterSquare());
        assertTrue(new Position(4, 4).isCenterSquare());
    }

    @Test
    void nonCenterSquares() {
        assertFalse(new Position(0, 0).isCenterSquare());
        assertFalse(new Position(2, 3).isCenterSquare());
        assertFalse(new Position(3, 5).isCenterSquare());
        assertFalse(new Position(5, 4).isCenterSquare());
    }

    @Test
    void equality() {
        assertEquals(new Position(3, 4), new Position(3, 4));
        assertNotEquals(new Position(3, 4), new Position(4, 3));
    }
}
