package com.strategy.client;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, AudioClip> soundCache = new HashMap<>();

    public static void play(String filename) {
        try {
            // Debug: Check if already loaded
            if (soundCache.containsKey(filename)) {
                soundCache.get(filename).play();
                return;
            }

            // Look for the file in client/src/main/resources/audio/
            URL resource = SoundManager.class.getResource("/audio/" + filename);

            // Debug: Check if file exists
            if (resource == null) {
                System.err.println("❌ ERROR: Could not find sound file: /audio/" + filename);
                System.err.println("   -> Make sure the file is in 'client/src/main/resources/audio/'");
                return;
            }

            AudioClip clip = new AudioClip(resource.toExternalForm());
            soundCache.put(filename, clip);
            clip.play();

        } catch (Exception e) {
            System.err.println("❌ EXCEPTION playing sound: " + filename);
            e.printStackTrace();
        }
    }
}