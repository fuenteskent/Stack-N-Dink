package com.stackanddink.core;

import com.stackanddink.core.model.Player;
import com.stackanddink.core.model.Court;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OpenPlaySession {

    private final UUID sessionId = UUID.randomUUID();

    private final List<Player> players = new ArrayList<>();
    private final List<Court> courts = new ArrayList<>();

    private int nextCourtNumber = 1;

    // SESSION MANAGEMENT

    public UUID getSessionId() {
        return sessionId;
    }

    // PLAYER MANAGEMENT

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

    // COURT MANAGEMENT

    public Court addCourt() {
        Court court = new Court(nextCourtNumber);
        nextCourtNumber++;

        courts.add(court);
        return court;
    }

    public boolean removeCourt(int courtNumber) {
        return courts.removeIf(
            court -> court.number() == courtNumber
        );
    }

    public List<Court> getCourts() {
        return List.copyOf(courts);
    }
}
