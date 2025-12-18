package com.strategy.client;

public class DamageEffect {
    private double x;
    private double y;
    private final String text;
    private int life;
    private final int maxLife; // Needed for smooth fading

    public DamageEffect(double x, double y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;

        // 60 Frames = 1.0 Second (at 60 FPS)
        this.life = 60;
        this.maxLife = 60;
    }

    public boolean update() {
        y -= 0.5; // Float upwards
        life--;
        return life > 0; // Keep alive until life hits 0
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getText() { return text; }

    public double getOpacity() {
        // Fade out smoothly from 1.0 to 0.0
        return (double) life / maxLife;
    }
}