package com.gitgames.othello.service;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("No game with id " + gameId);
    }
}
