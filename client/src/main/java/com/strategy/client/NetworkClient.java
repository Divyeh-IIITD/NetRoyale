package com.strategy.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = false;

    // We use a Consumer so the UI can define "what to do when a message arrives"
    private final Consumer<String> onMessageReceived;

    public NetworkClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.running = true;

        // Start listening in a background thread
        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                // Pass the message up to the UI
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }
}