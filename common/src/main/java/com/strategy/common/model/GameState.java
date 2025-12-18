package com.strategy.common.model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Unit> units;
    private int currentTurnPlayerId;

    // 0 = Empty Grass
    // 1 = Wall/Rock (Blocks Move & Attack)
    private int[][] board;

    public GameState() {
        this.units = new ArrayList<>();
        this.board = new int[8][8]; // Default 8x8 empty board
    }

    // --- GETTERS & SETTERS ---

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public int getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(int currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }
}