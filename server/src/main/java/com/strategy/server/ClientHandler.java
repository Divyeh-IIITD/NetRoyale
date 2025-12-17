package com.strategy.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategy.common.net.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final LobbyManager lobbyManager; // Reference to the lobby
    private final ObjectMapper mapper;
    private PrintWriter out;
    private String playerName; // Store the player's name

    // Updated Constructor
    public ClientHandler(Socket socket, LobbyManager lobbyManager) {
        this.socket = socket;
        this.lobbyManager = lobbyManager;
        this.mapper = new ObjectMapper();
    }

    public String getPlayerName() { return playerName; }

    // Helper to send JSON messages easily
    public void sendMessage(String type, String payload) {
        try {
            Message msg = Message.create(type, payload);
            String json = mapper.writeValueAsString(msg);
            out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer; // Store for later use
            System.out.println("Client connected: " + socket.getInetAddress());

            String jsonInput;
            while ((jsonInput = in.readLine()) != null) {
                try {
                    Message msg = mapper.readValue(jsonInput, Message.class);

                    if ("LOGIN".equals(msg.getType())) {
                        this.playerName = msg.getPayload().asText();
                        System.out.println("Login: " + playerName);

                        sendMessage("WELCOME", "Welcome to the Lobby, " + playerName + "!");

                        // CRITICAL: Add this player to the lobby!
                        lobbyManager.addPlayer(this);
                    }
                    // Add other commands here later...

                } catch (Exception e) {
                    System.err.println("Error processing message: " + jsonInput);
                }
            }

        } catch (IOException e) {
            System.out.println("Player disconnected: " + playerName);
        } finally {
            lobbyManager.removePlayer(this); // Remove from lobby if they disconnect
            try { socket.close(); } catch (IOException e) { /* Ignore */ }
        }
    }
}