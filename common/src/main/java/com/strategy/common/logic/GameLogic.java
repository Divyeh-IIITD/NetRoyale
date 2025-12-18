package com.strategy.common.logic;

import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;

public class GameLogic {

    public static boolean validateMove(GameState state, Unit unit, int targetX, int targetY) {
        // 1. Check Bounds (0-7)
        if (targetX < 0 || targetX > 7 || targetY < 0 || targetY > 7) {
            return false;
        }

        // 2. Check Distance (Max 3 tiles for simplicity, or unit.getMovementRange())
        int dist = Math.abs(targetX - unit.getX()) + Math.abs(targetY - unit.getY());
        int maxMove = unit.getType().getMovementRange();
        if (dist > maxMove) {
            return false;
        }

        // 3. Check Terrain (New for Day 13)
        // If the target tile is a Wall (1), you cannot move there.
        if (state.getBoard()[targetY][targetX] == 1) {
            return false;
        }

        // 4. Check Occupancy (Is there another unit there?)
        for (Unit other : state.getUnits()) {
            if (other.getX() == targetX && other.getY() == targetY) {
                return false; // Tile is occupied
            }
        }

        return true;
    }
}