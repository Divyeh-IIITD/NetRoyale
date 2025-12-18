package com.strategy.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategy.common.net.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final LobbyManager lobbyManager;
    private final ObjectMapper mapper;
    private PrintWriter out;
    private String playerName;

    private GameSession currentGameSession;
    private int playerIdInGame;

    public ClientHandler(Socket socket, LobbyManager lobbyManager) {
        this.socket = socket;
        this.lobbyManager = lobbyManager;
        this.mapper = new ObjectMapper();
    }

    public String getPlayerName() { return playerName; }

    public void setGameSession(GameSession session, int playerId) {
        this.currentGameSession = session;
        this.playerIdInGame = playerId;
    }

    public void sendMessageRaw(String json) {
        if (out != null) out.println(json);
    }

    public void sendMessage(String type, String payload) {
        try {
            Message msg = Message.create(type, payload);
            String json = mapper.writeValueAsString(msg);
            out.println(json);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;
            System.out.println("Client connected: " + socket.getInetAddress());

            String jsonInput;
            while ((jsonInput = in.readLine()) != null) {
                try {
                    Message msg = mapper.readValue(jsonInput, Message.class);

                    if ("LOGIN".equals(msg.getType())) {
                        this.playerName = msg.getPayload().asText();
                        System.out.println("Login: " + playerName);
                        sendMessage("WELCOME", "Welcome " + playerName + "!");
                        lobbyManager.addPlayer(this);
                    }
                    else if ("MOVE".equals(msg.getType())) {
                        if (currentGameSession != null) {
                            JsonNode payload = msg.getPayload();
                            String unitId = payload.get("unitId").asText();
                            int x = payload.get("x").asInt();
                            int y = payload.get("y").asInt();
                            currentGameSession.processMove(playerIdInGame, unitId, x, y);
                        }
                    }
                    else if ("ATTACK".equals(msg.getType())) {
                        if (currentGameSession != null) {
                            JsonNode payload = msg.getPayload();
                            String attId = payload.get("attackerId").asText();
                            String tarId = payload.get("targetId").asText();
                            currentGameSession.processAttack(playerIdInGame, attId, tarId);
                        }
                    }
                    else if ("RESTART".equals(msg.getType())) {
                        if (currentGameSession != null) {
                            currentGameSession.restartGame();
                        }
                    }
                    // --- NEW: CHAT HANDLER ---
                    else if ("CHAT".equals(msg.getType())) {
                        if (currentGameSession != null) {
                            // Forward the raw text payload to the session
                            currentGameSession.processChat(playerName, msg.getPayload().asText());
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Error processing: " + jsonInput);
                }
            }

        } catch (IOException e) {
            System.out.println("Player disconnected: " + playerName);
        } finally {
            lobbyManager.removePlayer(this);
            try { socket.close(); } catch (IOException e) { }
        }
    }
}