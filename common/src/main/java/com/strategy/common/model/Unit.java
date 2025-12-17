package com.strategy.common.model;

import java.util.UUID;

public class Unit {
    private String id;        // Unique UUID
    private int ownerId;      // 1 or 2
    private UnitType type;
    private int hp;
    private int x;
    private int y;
    private boolean hasMoved; // For turn logic

    // Empty constructor for Jackson JSON deserialization
    public Unit() {}

    public Unit(int ownerId, UnitType type, int x, int y) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        this.type = type;
        this.hp = 100; // Use type.maxHp in real logic
        this.x = x;
        this.y = y;
        this.hasMoved = false;
    }

    // Getters and Setters are MANDATORY for Jackson to work!
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public UnitType getType() { return type; }
    public void setType(UnitType type) { this.type = type; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
}