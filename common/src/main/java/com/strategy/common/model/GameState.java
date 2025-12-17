package com.strategy.common.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private String gameId;
    private int currentTurnPlayerId; // 1 or 2
    private boolean isGameOver;
    private String winnerId;
    private List<Unit> units; // We store units in a list, not a 2D array

    public GameState() {
        this.units = new ArrayList<>();
        this.currentTurnPlayerId = 1;
        this.isGameOver = false;
    }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }

    public int getCurrentTurnPlayerId() { return currentTurnPlayerId; }
    public void setCurrentTurnPlayerId(int id) { this.currentTurnPlayerId = id; }
}