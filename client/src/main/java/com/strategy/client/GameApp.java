package com.strategy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;
import com.strategy.common.net.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApp extends Application {
    private NetworkClient networkClient;
    private GameCanvas gameCanvas;
    private Label statusLabel;
    private TextArea chatArea;
    private Button restartButton;

    private int myPlayerId = -1;
    private String myUsername = ""; // New: Store the name locally
    private GameState currentGameState;
    private Unit selectedUnit = null;
    private boolean isLoggedIn = false;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // 1. Updated Initial Text
        statusLabel = new Label("Welcome to NetRoyale");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10; -fx-background-color: #ddd;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        root.setTop(statusLabel);

        gameCanvas = new GameCanvas();
        root.setCenter(gameCanvas);

        TextField inputField = new TextField();
        inputField.setPromptText("Enter Username");
        Button actionButton = new Button("Login");

        restartButton = new Button("PLAY AGAIN");
        restartButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        restartButton.setVisible(false);

        chatArea = new TextArea();
        chatArea.setPrefHeight(100);
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        VBox bottomControls = new VBox(10, inputField, actionButton, restartButton, chatArea);
        bottomControls.setStyle("-fx-padding: 10;");
        root.setBottom(bottomControls);

        // --- INPUT HANDLERS ---
        actionButton.setOnAction(e -> handleInput(inputField, actionButton));
        inputField.setOnAction(e -> handleInput(inputField, actionButton));

        restartButton.setOnAction(e -> {
            try {
                Message msg = Message.create("RESTART", "");
                networkClient.sendMessage(mapper.writeValueAsString(msg));
                restartButton.setVisible(false);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        gameCanvas.setOnMouseClicked(event -> {
            if (currentGameState == null) return;

            int col = (int) (event.getX() / 60);
            int row = (int) (event.getY() / 60);

            if (selectedUnit == null) {
                Unit clickedUnit = findUnitAt(col, row);
                if (clickedUnit != null && clickedUnit.getOwnerId() == myPlayerId) {
                    selectedUnit = clickedUnit;
                    gameCanvas.render(currentGameState, myPlayerId, selectedUnit);
                }
            } else {
                Unit targetUnit = findUnitAt(col, row);
                if (targetUnit == null) {
                    sendMoveRequest(selectedUnit, col, row);
                } else if (targetUnit.getOwnerId() != myPlayerId) {
                    sendAttackRequest(selectedUnit, targetUnit);
                }
                selectedUnit = null;
                gameCanvas.render(currentGameState, myPlayerId, selectedUnit);
            }
        });

        networkClient = new NetworkClient(jsonMessage -> {
            Platform.runLater(() -> handleServerMessage(jsonMessage, inputField, actionButton));
        });

        try {
            networkClient.connect("localhost", 8080);
            statusLabel.setText("Connected. Enter Name to Join.");
        } catch (IOException e) {
            statusLabel.setText("Connection Failed: " + e.getMessage());
        }

        Scene scene = new Scene(root, 500, 750);
        // 2. Updated Window Title
        primaryStage.setTitle("NetRoyale");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleInput(TextField inputField, Button actionButton) {
        String text = inputField.getText();
        if (text.isEmpty()) return;

        try {
            if (!isLoggedIn) {
                // 3. Capture Username Here
                this.myUsername = text;

                Message msg = Message.create("LOGIN", text);
                networkClient.sendMessage(mapper.writeValueAsString(msg));
                inputField.setDisable(true);
                actionButton.setDisable(true);
            } else {
                Message msg = Message.create("CHAT", text);
                networkClient.sendMessage(mapper.writeValueAsString(msg));
                inputField.clear();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleServerMessage(String jsonMessage, TextField inputField, Button actionButton) {
        try {
            Message msg = mapper.readValue(jsonMessage, Message.class);

            if ("GAME_START".equals(msg.getType())) {
                JsonNode payload = msg.getPayload();
                if (payload.isTextual()) payload = mapper.readTree(payload.asText());

                this.myPlayerId = payload.get("yourPlayerId").asInt();
                JsonNode stateNode = payload.get("state");
                this.currentGameState = mapper.treeToValue(stateNode, GameState.class);

                gameCanvas.render(currentGameState, myPlayerId, selectedUnit);
                restartButton.setVisible(false);

                // 4. Updated Status Label to show Name
                statusLabel.setText("Playing as: " + myUsername);
                statusLabel.setStyle("-fx-background-color: #ddd; -fx-padding: 10;");

                chatArea.clear();
                chatArea.appendText("--- BATTLE STARTED ---\n");
                SoundManager.play("move.mp3");
            }
            else if ("GAME_UPDATE".equals(msg.getType())) {
                JsonNode stateNode = msg.getPayload();
                GameState newState = mapper.treeToValue(stateNode, GameState.class);

                if (currentGameState != null &&
                        currentGameState.getCurrentTurnPlayerId() != newState.getCurrentTurnPlayerId()) {
                    SoundManager.play("move.mp3");
                }

                if (currentGameState != null) {
                    for (Unit newUnit : newState.getUnits()) {
                        for (Unit oldUnit : currentGameState.getUnits()) {
                            if (newUnit.getId().equals(oldUnit.getId())) {
                                int damageTaken = oldUnit.getHp() - newUnit.getHp();
                                if (damageTaken > 0) {
                                    gameCanvas.addDamageEffect(newUnit.getX(), newUnit.getY(), damageTaken);
                                    SoundManager.play("attack.mp3");
                                }
                            }
                        }
                    }
                }

                this.currentGameState = newState;
                gameCanvas.render(currentGameState, myPlayerId, selectedUnit);

                if (currentGameState.getCurrentTurnPlayerId() == myPlayerId) {
                    statusLabel.setText("YOUR TURN (" + myUsername + ")");
                    statusLabel.setStyle("-fx-background-color: #88ff88; -fx-padding: 10;");
                } else {
                    statusLabel.setText("Opponent's Turn");
                    statusLabel.setStyle("-fx-background-color: #ff8888; -fx-padding: 10;");
                }
            }
            else if ("GAME_OVER".equals(msg.getType())) {
                JsonNode payload = msg.getPayload();
                int winnerId = payload.get("winnerId").asInt();
                boolean iWon = (winnerId == myPlayerId);

                gameCanvas.drawGameOver(iWon);
                statusLabel.setText(iWon ? "VICTORY!" : "DEFEAT");
                currentGameState = null;
                restartButton.setVisible(true);

                if (iWon) {
                    SoundManager.play("win.mp3");
                }
            }
            else if ("WELCOME".equals(msg.getType())) {
                statusLabel.setText("Logged in as " + myUsername + ". Waiting for opponent...");
                isLoggedIn = true;
                inputField.setDisable(false);
                actionButton.setDisable(false);
                inputField.clear();
                inputField.setPromptText("Type chat message...");
                actionButton.setText("Send");
            }
            else if ("CHAT".equals(msg.getType())) {
                chatArea.appendText(msg.getPayload().asText() + "\n");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private Unit findUnitAt(int x, int y) {
        if (currentGameState == null) return null;
        for (Unit u : currentGameState.getUnits()) {
            if (u.getX() == x && u.getY() == y) return u;
        }
        return null;
    }

    private void sendMoveRequest(Unit unit, int targetX, int targetY) {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("unitId", unit.getId());
            node.put("x", targetX);
            node.put("y", targetY);
            networkClient.sendMessage(mapper.writeValueAsString(Message.create("MOVE", node)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendAttackRequest(Unit attacker, Unit target) {
        try {
            SoundManager.play("attack.mp3");
            ObjectNode node = mapper.createObjectNode();
            node.put("attackerId", attacker.getId());
            node.put("targetId", target.getId());
            networkClient.sendMessage(mapper.writeValueAsString(Message.create("ATTACK", node)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}