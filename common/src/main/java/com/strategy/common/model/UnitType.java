package com.strategy.common.model;

public enum UnitType {
    ARCHER(80, 15, 3, 3),   // HP, Dmg, AttackRange, MoveRange
    KNIGHT(150, 20, 1, 2),
    SCOUT(60, 10, 1, 5);

    private final int maxHp;
    private final int damage;
    private final int attackRange;
    private final int movementRange; // Added this

    UnitType(int maxHp, int damage, int attackRange, int movementRange) {
        this.maxHp = maxHp;
        this.damage = damage;
        this.attackRange = attackRange;
        this.movementRange = movementRange;
    }

    public int getMovementRange() { return movementRange; }
    // Add other getters...
}