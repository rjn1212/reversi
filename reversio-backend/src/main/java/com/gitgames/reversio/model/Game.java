package com.gitgames.reversio.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Game {

    private final String id;
    private final Board board;
    private Player currentTurn;
    private GamePhase phase;
    private Player winner;
    private String winReason;
    private final AiDifficulty aiDifficulty;
    private final GameVariant variant;
    private final Player humanSide;
    private final List<MoveRecord> moveHistory = new ArrayList<>();
    private boolean lastMoveWasPass;
    private final Map<Player, String> platformUserIds = new EnumMap<>(Player.class);
    private final Instant createdAt;
    private Instant lastActivityAt;

    public Game(String id, AiDifficulty aiDifficulty, GameVariant variant, Player humanSide) {
        this.id = id;
        this.aiDifficulty = aiDifficulty;
        this.variant = variant;
        this.humanSide = humanSide;
        this.currentTurn = Player.BLACK;
        this.createdAt = Instant.now();
        this.lastActivityAt = this.createdAt;

        if (variant == GameVariant.REVERSIO) {
            this.board = Board.createReversioStart();
            this.phase = GamePhase.PLAYING;
        } else {
            this.board = Board.createReversiStart();
            this.phase = GamePhase.PLACEMENT;
        }
    }

    public String getId() { return id; }
    public Board getBoard() { return board; }
    public Player getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Player currentTurn) { this.currentTurn = currentTurn; }
    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }
    public Player getWinner() { return winner; }
    public void setWinner(Player winner) { this.winner = winner; }
    public String getWinReason() { return winReason; }
    public void setWinReason(String winReason) { this.winReason = winReason; }
    public AiDifficulty getAiDifficulty() { return aiDifficulty; }
    public GameVariant getVariant() { return variant; }
    public Player getHumanSide() { return humanSide; }
    public List<MoveRecord> getMoveHistory() { return moveHistory; }
    public boolean isLastMoveWasPass() { return lastMoveWasPass; }
    public void setLastMoveWasPass(boolean lastMoveWasPass) { this.lastMoveWasPass = lastMoveWasPass; }
    public Map<Player, String> getPlatformUserIds() { return platformUserIds; }
    public String getPlatformUserId(Player player) { return platformUserIds.get(player); }
    public void setPlatformUserId(Player player, String userId) { platformUserIds.put(player, userId); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public Player getAiSide() { return humanSide.opponent(); }
    public boolean isAiTurn() { return currentTurn == getAiSide(); }
    public int getMoveNumber() { return moveHistory.size(); }

    public void switchTurn() {
        this.currentTurn = this.currentTurn.opponent();
    }

    public void recordMove(MoveRecord record) {
        moveHistory.add(record);
        lastActivityAt = Instant.now();
    }
}
