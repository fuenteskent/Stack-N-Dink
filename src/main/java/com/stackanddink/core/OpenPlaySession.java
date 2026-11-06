package com.stackanddink.core;

import com.stackanddink.core.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OpenPlaySession {

    private final UUID sessionId = UUID.randomUUID();
    private final List<Player> players = new ArrayList<>();

    public UUID getSessionId() {
        return sessionId;
    }

    public Player addPlayer(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Player name cannot be empty."
            );
        }

        Player player = new Player(
            UUID.randomUUID(),
            name.trim()
        );

        players.add(player);
        return player;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
    }
}
