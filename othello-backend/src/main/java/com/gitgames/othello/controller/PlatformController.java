package com.gitgames.othello.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PlatformController {

    private final Instant startTime = Instant.now();

    @GetMapping("/platform/health")
    public Map<String, Object> health() {
        long uptime = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        return Map.of(
                "status", "ok",
                "game", "othello",
                "version", "1.0.0",
                "uptime", uptime
        );
    }
}
