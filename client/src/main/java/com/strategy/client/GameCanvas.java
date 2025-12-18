package com.strategy.client;

import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class GameCanvas extends Canvas {
    private static final int TILE_SIZE = 60;
    private static final int BOARD_SIZE = 8;
    private static final int VIEW_RANGE = 2;
    private static final int MOVEMENT_RANGE = 3;

    private final List<DamageEffect> effects = new ArrayList<>();
    private final Set<String> validMoveTiles = new HashSet<>();
    private final Map<String, UnitAnimation> animations = new HashMap<>();

    private GameState lastState;
    private int myPlayerId;
    private Unit selectedUnit;

    public GameCanvas() {
        super(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawInternal();
            }
        };
        timer.start();
    }

    public void addDamageEffect(int tileX, int tileY, int damage) {
        double pixelX = (tileX * TILE_SIZE) + (TILE_SIZE / 2.0);
        double pixelY = (tileY * TILE_SIZE) + (TILE_SIZE / 2.0);
        effects.add(new DamageEffect(pixelX, pixelY, "-" + damage));
    }

    public void render(GameState state, int myPlayerId, Unit selectedUnit) {
        this.lastState = state;
        this.myPlayerId = myPlayerId;
        this.selectedUnit = selectedUnit;

        if (state != null) {
            Set<String> activeIds = new HashSet<>();
            for (Unit u : state.getUnits()) {
                activeIds.add(u.getId());
                if (animations.containsKey(u.getId())) {
                    animations.get(u.getId()).updateData(u);
                } else {
                    animations.put(u.getId(), new UnitAnimation(u));
                }
            }
            animations.keySet().removeIf(id -> !activeIds.contains(id));
        }

        if (selectedUnit != null && selectedUnit.getOwnerId() == myPlayerId) {
            calculateValidMoves(selectedUnit, state);
        } else {
            validMoveTiles.clear();
        }
    }

    private void calculateValidMoves(Unit unit, GameState state) {
        validMoveTiles.clear();
        if (state == null) return;

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{unit.getX(), unit.getY(), MOVEMENT_RANGE});
        Set<String> visited = new HashSet<>();
        visited.add(unit.getX() + "," + unit.getY());

        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0], cy = current[1], movesLeft = current[2];

            if (movesLeft <= 0) continue;

            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                String key = nx + "," + ny;

                if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE) continue;
                if (visited.contains(key)) continue;
                if (state.getBoard()[ny][nx] == 1) continue;

                boolean occupied = false;
                for (Unit u : state.getUnits()) {
                    if (u.getX() == nx && u.getY() == ny) { occupied = true; break; }
                }
                if (occupied) continue;

                visited.add(key);
                validMoveTiles.add(key);
                queue.add(new int[]{nx, ny, movesLeft - 1});
            }
        }
    }

    private void drawInternal() {
        GraphicsContext gc = getGraphicsContext2D();

        // --- NEW: LOBBY SCREEN LOGIC ---
        // If we don't have a game state yet, draw the Lobby
        if (lastState == null) {
            drawWelcomeScreen(gc);
            return;
        }
        // -------------------------------

        // Clear screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        boolean[][] isVisible = new boolean[BOARD_SIZE][BOARD_SIZE];
        if (lastState != null) {
            calculateVisibility(lastState, myPlayerId, isVisible);
        }

        // Draw Map
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!isVisible[row][col]) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(0.5);
                    gc.strokeRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    continue;
                }

                if ((row + col) % 2 == 0) gc.setFill(Color.web("#8fbc8f"));
                else gc.setFill(Color.web("#90ee90"));
                gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Valid Move Highlights
                if (validMoveTiles.contains(col + "," + row)) {
                    double centerX = col * TILE_SIZE + TILE_SIZE / 2.0;
                    double centerY = row * TILE_SIZE + TILE_SIZE / 2.0;
                    double dotRadius = TILE_SIZE / 8.0;
                    gc.setFill(Color.rgb(255, 0, 0, 0.7));
                    gc.fillOval(centerX - dotRadius, centerY - dotRadius, dotRadius * 2, dotRadius * 2);
                }

                // Walls
                if (lastState != null && lastState.getBoard()[row][col] == 1) {
                    Image wallImg = SpriteManager.getImage("wall.png");
                    if (wallImg != null) gc.drawImage(wallImg, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    else {
                        gc.setFill(Color.DARKSLATEGRAY);
                        gc.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // Draw Units
        for (UnitAnimation anim : animations.values()) {
            Unit u = anim.getData();
            boolean isMine = (u.getOwnerId() == myPlayerId);
            boolean isOnVisibleTile = isVisible[u.getY()][u.getX()];

            if (isMine || isOnVisibleTile) {
                anim.animate();
                drawUnit(gc, anim, myPlayerId);
            }
        }

        // Selection
        if (selectedUnit != null) {
            drawHighlight(selectedUnit.getX(), selectedUnit.getY());
        }

        // Effects
        drawEffects(gc);
    }

    // --- NEW: THE LOBBY DESIGN ---
    private void drawWelcomeScreen(GraphicsContext gc) {
        // 1. Background
        gc.setFill(Color.web("#1a1a2e")); // Dark Navy Blue
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 2. Title Text
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        gc.fillText("NET ROYALE", getWidth() / 2, getHeight() / 2 - 50);

        // 3. Subtitle
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("Tactical Strategy Warfare", getWidth() / 2, getHeight() / 2 - 10);

        // 4. Instructions
        gc.setFill(Color.web("#4CAF50")); // Green
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText("Enter Name & Login below to Join", getWidth() / 2, getHeight() / 2 + 50);

        // 5. Waiting Animation (Simple Pulse)
        double time = System.currentTimeMillis() / 1000.0;
        if ((int)(time * 2) % 2 == 0) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Courier New", 14));
            gc.fillText("WAITING FOR PLAYERS...", getWidth() / 2, getHeight() / 2 + 100);
        }
    }
    // -----------------------------

    private void drawEffects(GraphicsContext gc) {
        if (effects.isEmpty()) return;
        Iterator<DamageEffect> it = effects.iterator();
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        while (it.hasNext()) {
            DamageEffect effect = it.next();
            gc.setFill(Color.rgb(255, 0, 0, effect.getOpacity()));
            gc.setStroke(Color.rgb(255, 255, 255, effect.getOpacity()));
            gc.setLineWidth(1);
            gc.fillText(effect.getText(), effect.getX(), effect.getY());
            gc.strokeText(effect.getText(), effect.getX(), effect.getY());
            if (!effect.update()) it.remove();
        }
    }

    private void calculateVisibility(GameState state, int myPlayerId, boolean[][] isVisible) {
        for (Unit unit : state.getUnits()) {
            if (unit.getOwnerId() == myPlayerId) {
                int startX = unit.getX();
                int startY = unit.getY();
                for (int r = 0; r < BOARD_SIZE; r++) {
                    for (int c = 0; c < BOARD_SIZE; c++) {
                        int dist = Math.abs(startX - c) + Math.abs(startY - r);
                        if (dist <= VIEW_RANGE) isVisible[r][c] = true;
                    }
                }
            }
        }
    }

    private void drawUnit(GraphicsContext gc, UnitAnimation anim, int myPlayerId) {
        Unit unit = anim.getData();
        double x = anim.getVisualX() * TILE_SIZE;
        double y = anim.getVisualY() * TILE_SIZE;

        String baseName = unit.getType().name().toLowerCase();
        Image sprite = SpriteManager.getImage(baseName + ".png");
        if (sprite == null) sprite = SpriteManager.getImage(baseName + ".jpg");

        if (sprite != null) {
            gc.drawImage(sprite, x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
            gc.setLineWidth(3);
            if (unit.getOwnerId() == myPlayerId) gc.setStroke(Color.BLUE);
            else gc.setStroke(Color.RED);
            gc.strokeRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        } else {
            if (unit.getOwnerId() == myPlayerId) gc.setFill(Color.BLUE);
            else gc.setFill(Color.RED);
            gc.fillOval(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(unit.getType().name().substring(0, 1), x + 25, y + 35);
        }

        double hpPercent = (double) unit.getHp() / unit.getType().getMaxHp();
        if (hpPercent < 0) hpPercent = 0;

        gc.setFill(Color.RED);
        gc.fillRect(x + 5, y - 8, TILE_SIZE - 10, 5);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(x + 5, y - 8, (TILE_SIZE - 10) * hpPercent, 5);
    }

    public void drawHighlight(int x, int y) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void drawGameOver(boolean iWon) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.setTextAlign(TextAlignment.CENTER);
        if (iWon) {
            gc.setFill(Color.LIMEGREEN);
            gc.fillText("VICTORY!", getWidth() / 2, getHeight() / 2);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("DEFEAT", getWidth() / 2, getHeight() / 2);
        }
    }
}