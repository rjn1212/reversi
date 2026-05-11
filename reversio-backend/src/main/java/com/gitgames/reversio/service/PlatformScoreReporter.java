package com.gitgames.reversio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitgames.reversio.model.Game;
import com.gitgames.reversio.model.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PlatformScoreReporter {

    private static final Logger LOG = Logger.getLogger(PlatformScoreReporter.class.getName());

    @Value("${platform.api.url:}")
    private String platformApiUrl;

    @Value("${platform.api.key:}")
    private String platformApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void reportGameResult(Game game) {
        if (platformApiUrl.isBlank() || platformApiKey.isBlank()) {
            return;
        }

        String userId = game.getPlatformUserId(game.getHumanSide());
        if (userId == null) {
            return;
        }

        int humanDiscs = game.getBoard().countDiscs(game.getHumanSide());
        Player aiSide = game.getHumanSide().opponent();
        int aiDiscs = game.getBoard().countDiscs(aiSide);
        boolean won = game.getWinner() == game.getHumanSide();
        boolean draw = game.getWinner() == null;

        String outcome = draw ? "draw" : won ? "win" : "loss";

        try {
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("result", outcome);
            metadata.put("opponentScore", aiDiscs);
            metadata.put("difficulty", game.getAiDifficulty().name());
            metadata.put("variant", game.getVariant().name());

            ObjectNode json = objectMapper.createObjectNode();
            json.put("gameSlug", "reversio");
            json.put("userId", userId);
            json.put("score", humanDiscs);
            json.set("metadata", metadata);
            json.put("completedAt", Instant.now().toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(platformApiUrl + "/api/scores"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + platformApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(json)))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(r -> {
                        if (r.statusCode() >= 400) {
                            LOG.warning("Score report failed with status " + r.statusCode());
                        }
                    });
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to report score", e);
        }
    }
}
