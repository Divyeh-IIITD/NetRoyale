package com.strategy.client;

import com.strategy.common.model.Unit;

public class UnitAnimation {
    private String unitId;
    private double currentX;
    private double currentY;
    private int targetX;
    private int targetY;

    // We store the real unit data here (for HP, Type, etc.)
    private Unit latestUnitData;

    public UnitAnimation(Unit unit) {
        this.unitId = unit.getId();
        // Start exactly where the unit is
        this.currentX = unit.getX();
        this.currentY = unit.getY();
        this.targetX = unit.getX();
        this.targetY = unit.getY();
        this.latestUnitData = unit;
    }

    public void updateData(Unit unit) {
        // Update the TARGET. We do not change currentX/Y immediately.
        // This causes the "slide" effect.
        this.targetX = unit.getX();
        this.targetY = unit.getY();
        this.latestUnitData = unit;
    }

    public void animate() {
        double speed = 0.15; // 0.15 tiles per frame (adjust for faster/slower)

        // Slide X
        if (Math.abs(currentX - targetX) > speed) {
            if (currentX < targetX) currentX += speed;
            else currentX -= speed;
        } else {
            currentX = targetX; // Snap when close
        }

        // Slide Y
        if (Math.abs(currentY - targetY) > speed) {
            if (currentY < targetY) currentY += speed;
            else currentY -= speed;
        } else {
            currentY = targetY; // Snap when close
        }
    }

    public double getVisualX() { return currentX; }
    public double getVisualY() { return currentY; }
    public Unit getData() { return latestUnitData; }
}