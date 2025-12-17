package com.strategy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8080;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    // Create the single Lobby Manager that everyone shares
    private static final LobbyManager lobbyManager = new LobbyManager();

    public static void main(String[] args) {
        System.out.println("Starting Game Server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running. Waiting for players...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Pass the lobbyManager to the new handler
                ClientHandler handler = new ClientHandler(clientSocket, lobbyManager);

                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}