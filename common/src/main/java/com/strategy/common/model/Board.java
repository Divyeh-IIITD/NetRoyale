package com.strategy.common.model;

import java.util.Optional;

public class Board {
    private final int width;
    private final int height;

    // We don't store units inside the Board class itself usually;
    // we pass the list of units TO the board methods.
    // This makes the Board class "stateless" regarding units, which is cleaner.

    public Board() {
        this(8, 8); // Default to 8x8 standard chess size
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}