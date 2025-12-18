package com.strategy.server;

import java.util.ArrayList;
import java.util.List;

public class LobbyManager {
    // A list of players waiting for a game
    private final List<ClientHandler> waitingPlayers = new ArrayList<>();

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

            GameSession session = new GameSession(player1, player2);

            // Link the players to the session
            player1.setGameSession(session, 1);
            player2.setGameSession(session, 2);

            session.start();
        }
    }

    public synchronized void removePlayer(ClientHandler player) {
        waitingPlayers.remove(player);
    }
}