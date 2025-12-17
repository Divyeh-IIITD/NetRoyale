package com.strategy.common.logic;

import com.strategy.common.model.Board;
import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;

import java.util.Optional;

public class GameLogic {

    // Returns true if the move is valid, false (or throws exception) otherwise.
    public static boolean validateMove(GameState state, Unit unit, int targetX, int targetY) {
        Board board = new Board(8, 8); // Should ideally come from state, but fixed for now

        // Rule 1: Target must be on the board
        if (!board.isWithinBounds(targetX, targetY)) {
            System.out.println("Invalid Move: Out of bounds.");
            return false;
        }

        // Rule 2: Unit must belong to the player whose turn it is
        if (unit.getOwnerId() != state.getCurrentTurnPlayerId()) {
            System.out.println("Invalid Move: Not your unit.");
            return false;
        }

        // Rule 3: Target tile must be empty (No stacking units!)
        if (isTileOccupied(state, targetX, targetY)) {
            System.out.println("Invalid Move: Tile occupied.");
            return false;
        }

        // Rule 4: Movement Range (Manhattan Distance or Euclidean?)
        // Let's use Manhattan Distance for a grid ( |x1-x2| + |y1-y2| )
        int distance = Math.abs(unit.getX() - targetX) + Math.abs(unit.getY() - targetY);
        int maxRange = unit.getType().getMovementRange(); // You need to add this getter to UnitType!

        // TEMPORARY FIX: If you haven't added getMovementRange() to UnitType yet, assume 3.
        // int maxRange = 3;

        if (distance > maxRange) {
            System.out.println("Invalid Move: Too far. Distance: " + distance + ", Max: " + maxRange);
            return false;
        }

        return true;
    }

    private static boolean isTileOccupied(GameState state, int x, int y) {
        return state.getUnits().stream()
                .anyMatch(u -> u.getX() == x && u.getY() == y);
    }
}