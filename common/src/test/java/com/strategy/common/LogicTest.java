package com.strategy.common;

import com.strategy.common.logic.GameLogic;
import com.strategy.common.model.GameState;
import com.strategy.common.model.Unit;
import com.strategy.common.model.UnitType;

public class LogicTest {
    public static void main(String[] args) {
        // 1. Setup a fake game
        GameState state = new GameState();
        Unit myArcher = new Unit(1, UnitType.ARCHER, 2, 2); // Player 1, at (2,2)
        state.getUnits().add(myArcher);
        state.setCurrentTurnPlayerId(1); // It is Player 1's turn

        // 2. Test a Valid Move (Moving 1 tile right)
        boolean valid = GameLogic.validateMove(state, myArcher, 3, 2);
        System.out.println("Test Valid Move (2,2 -> 3,2): " + (valid ? "PASS" : "FAIL"));

        // 3. Test Invalid Move (Too far)
        boolean tooFar = GameLogic.validateMove(state, myArcher, 7, 7);
        System.out.println("Test Invalid Move (Too Far): " + (!tooFar ? "PASS" : "FAIL"));

        // 4. Test Occupied (Put another unit at 3,2)
        state.getUnits().add(new Unit(1, UnitType.KNIGHT, 3, 2));
        boolean occupied = GameLogic.validateMove(state, myArcher, 3, 2);
        System.out.println("Test Occupied Tile: " + (!occupied ? "PASS" : "FAIL"));
    }
}