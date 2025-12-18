package com.strategy.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.strategy.common.logic.GameLogic;
import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;
import com.strategy.common.model.UnitType;
import com.strategy.common.net.Message;

import java.util.Optional;
import java.util.UUID;

public class GameSession {
    private final String gameId;
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final GameState gameState;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean isGameOver = false;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.gameId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.gameState = new GameState();
        initializeBoard();
    }

    // --- NEW: CHAT LOGIC ---
    public synchronized void processChat(String senderName, String messageText) {
        try {
            // Format: "Alice: Hello!"
            String fullText = senderName + ": " + messageText;

            // Send to both players
            Message msg = Message.create("CHAT", fullText);
            String jsonOutput = mapper.writeValueAsString(msg);

            player1.sendMessageRaw(jsonOutput);
            player2.sendMessageRaw(jsonOutput);

        } catch (Exception e) { e.printStackTrace(); }
    }
    // -----------------------

    public synchronized void restartGame() {
        System.out.println("Restarting Game Session: " + gameId);
        this.gameState.getUnits().clear();
        this.gameState.setBoard(new int[8][8]);
        this.isGameOver = false;
        initializeBoard();
        start();
    }

    private void initializeBoard() {
        // Player 1
        gameState.getUnits().add(new Unit(1, UnitType.ARCHER, 0, 0));
        gameState.getUnits().add(new Unit(1, UnitType.KNIGHT, 0, 1));
        gameState.getUnits().add(new Unit(1, UnitType.SCOUT, 1, 0));

        // Player 2
        gameState.getUnits().add(new Unit(2, UnitType.ARCHER, 7, 7));
        gameState.getUnits().add(new Unit(2, UnitType.KNIGHT, 7, 6));
        gameState.getUnits().add(new Unit(2, UnitType.SCOUT, 6, 7));

        // Walls
        gameState.getBoard()[3][3] = 1;
        gameState.getBoard()[3][4] = 1;
        gameState.getBoard()[4][3] = 1;
        gameState.getBoard()[4][4] = 1;
        gameState.getBoard()[2][6] = 1;
        gameState.getBoard()[5][1] = 1;

        gameState.setCurrentTurnPlayerId(1);
    }

    public void start() {
        sendGameStart(player1, 1);
        sendGameStart(player2, 2);
    }

    public synchronized void processMove(int playerId, String unitId, int targetX, int targetY) {
        if (isGameOver) return;
        if (gameState.getCurrentTurnPlayerId() != playerId) return;

        Optional<Unit> unitOpt = gameState.getUnits().stream()
                .filter(u -> u.getId().equals(unitId))
                .findFirst();

        if (unitOpt.isPresent()) {
            Unit unit = unitOpt.get();
            boolean isValid = GameLogic.validateMove(gameState, unit, targetX, targetY);

            if (isValid) {
                unit.setX(targetX);
                unit.setY(targetY);
                toggleTurn(playerId);
                broadcastGameUpdate();
            }
        }
    }

    public synchronized void processAttack(int playerId, String attackerId, String targetId) {
        if (isGameOver) return;
        if (gameState.getCurrentTurnPlayerId() != playerId) return;

        Optional<Unit> attOpt = gameState.getUnits().stream().filter(u -> u.getId().equals(attackerId)).findFirst();
        Optional<Unit> defOpt = gameState.getUnits().stream().filter(u -> u.getId().equals(targetId)).findFirst();

        if (attOpt.isPresent() && defOpt.isPresent()) {
            Unit attacker = attOpt.get();
            Unit defender = defOpt.get();

            int dist = Math.abs(attacker.getX() - defender.getX()) + Math.abs(attacker.getY() - defender.getY());
            int maxRange = attacker.getType().getAttackRange();

            if (dist <= maxRange) {
                int damage = attacker.getType().getDamage();
                defender.setHp(defender.getHp() - damage);

                if (defender.getHp() <= 0) {
                    gameState.getUnits().remove(defender);
                    checkWinCondition();
                }

                if (!isGameOver) {
                    toggleTurn(playerId);
                    broadcastGameUpdate();
                }
            }
        }
    }

    private void checkWinCondition() {
        boolean p1HasUnits = gameState.getUnits().stream().anyMatch(u -> u.getOwnerId() == 1);
        boolean p2HasUnits = gameState.getUnits().stream().anyMatch(u -> u.getOwnerId() == 2);

        if (!p1HasUnits || !p2HasUnits) {
            isGameOver = true;
            int winnerId = p1HasUnits ? 1 : 2;
            broadcastGameOver(winnerId);
        }
    }

    private void toggleTurn(int currentPlayerId) {
        int nextPlayer = (currentPlayerId == 1) ? 2 : 1;
        gameState.setCurrentTurnPlayerId(nextPlayer);
    }

    private void broadcastGameUpdate() {
        try {
            Message updateMsg = Message.create("GAME_UPDATE", gameState);
            String jsonOutput = mapper.writeValueAsString(updateMsg);
            player1.sendMessageRaw(jsonOutput);
            player2.sendMessageRaw(jsonOutput);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void broadcastGameOver(int winnerId) {
        try {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("winnerId", winnerId);
            Message msg = Message.create("GAME_OVER", payload);
            String jsonOutput = mapper.writeValueAsString(msg);
            player1.sendMessageRaw(jsonOutput);
            player2.sendMessageRaw(jsonOutput);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendGameStart(ClientHandler player, int yourPlayerId) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("yourPlayerId", yourPlayerId);
            root.set("state", mapper.valueToTree(gameState));
            player.sendMessage("GAME_START", mapper.writeValueAsString(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}