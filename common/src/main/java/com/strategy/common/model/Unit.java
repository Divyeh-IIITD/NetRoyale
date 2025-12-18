package com.strategy.common.model;

import java.util.UUID;

public class Unit {
    private String id;        // Unique UUID
    private int ownerId;      // 1 or 2
    private UnitType type;
    private int hp;           // Health Points
    private int x;
    private int y;
    private boolean hasMoved; // For turn logic

    // Empty constructor for Jackson JSON deserialization
    public Unit() {}

    public Unit(int ownerId, UnitType type, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        this.type = type;
        this.hp = type.getMaxHp(); // Initialize HP based on the UnitType
        this.x = x;
        this.y = y;
        this.hasMoved = false;
    }

    // --- GETTERS AND SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public UnitType getType() { return type; }
    public void setType(UnitType type) { this.type = type; }

    // This was the missing part causing your error!
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public boolean isHasMoved() { return hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }
}