package com.gitgames.othello.service;

import com.gitgames.othello.model.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {

    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    public void save(Game game) {
        games.put(game.getId(), game);
    }

    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public void deleteById(String id) {
        games.remove(id);
    }
}
