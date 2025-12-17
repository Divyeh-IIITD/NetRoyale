package com.strategy.server;

import java.util.ArrayList;
import java.util.List;

public class LobbyManager {
    // A list of players waiting for a game
    private final List<ClientHandler> waitingPlayers = new ArrayList<>();

    // "synchronized" ensures two threads don't mess up the list at the same time
    public synchronized void addPlayer(ClientHandler player) {
        waitingPlayers.add(player);
        System.out.println("Player joined lobby. Total waiting: " + waitingPlayers.size());

        checkForMatch();
    }

    private void checkForMatch() {
        if (waitingPlayers.size() >= 2) {
            // Pop the first two players
            ClientHandler player1 = waitingPlayers.remove(0);
            ClientHandler player2 = waitingPlayers.remove(0);

            System.out.println("MATCH FOUND! " + player1.getPlayerName() + " vs " + player2.getPlayerName());

            // TODO (Day 7): Create a GameSession and start the game
            player1.sendMessage("MATCH_FOUND", "Opponent: " + player2.getPlayerName());
            player2.sendMessage("MATCH_FOUND", "Opponent: " + player1.getPlayerName());
        }
    }

    public synchronized void removePlayer(ClientHandler player) {
        waitingPlayers.remove(player);
    }
}