package com.strategy.client;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static final Map<String, Image> imageCache = new HashMap<>();

    // Load an image from resources/assets/
    public static Image getImage(String filename) {
        if (imageCache.containsKey(filename)) {
            return imageCache.get(filename);
        }

        try {
            // Look for file in src/main/resources/assets/
            InputStream is = SpriteManager.class.getResourceAsStream("/assets/" + filename);
            if (is == null) {
                // Fail silently (GameCanvas will draw a circle instead)
                return null;
            }

            Image img = new Image(is);
            imageCache.put(filename, img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}