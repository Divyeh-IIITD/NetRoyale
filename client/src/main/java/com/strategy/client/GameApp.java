package com.strategy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategy.common.net.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApp extends Application {
    private NetworkClient networkClient;
    private TextArea chatArea;
    private final ObjectMapper mapper = new ObjectMapper(); // For creating JSON

    @Override
    public void start(Stage primaryStage) {
        // --- UI SETUP ---
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(400);

        TextField inputField = new TextField();
        inputField.setPromptText("Enter your username to login...");

        Button sendButton = new Button("Login");

        // --- BUTTON ACTION (The critical JSON part) ---
        sendButton.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                try {
                    // 1. Construct the Protocol Message
                    // Type: LOGIN, Payload: The text in the box
                    Message msg = Message.create("LOGIN", text);

                    // 2. Convert Java Object -> JSON String
                    String jsonToSend = mapper.writeValueAsString(msg);

                    // 3. Send raw JSON string over the network
                    networkClient.sendMessage(jsonToSend);

                    inputField.clear();
                } catch (Exception ex) {
                    chatArea.appendText("Error sending message: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                }
            }
        });

        VBox root = new VBox(10, chatArea, inputField, sendButton);
        root.setStyle("-fx-padding: 20px;");

        // --- NETWORK SETUP ---
        networkClient = new NetworkClient(jsonMessage -> {
            // This runs whenever the server sends us something back
            Platform.runLater(() -> {
                chatArea.appendText("Server Raw JSON: " + jsonMessage + "\n");
            });
        });

        try {
            networkClient.connect("localhost", 8080);
            chatArea.appendText("Connected to Server. Please login.\n");
        } catch (IOException e) {
            chatArea.appendText("Failed to connect: " + e.getMessage() + "\n");
        }

        // --- WINDOW SHOW ---
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Tactical Game Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}