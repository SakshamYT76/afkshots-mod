package com.thesoulsensei.afkshots;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent client-only AFK Shots settings. */
public final class AfkShotsConfig {
    public static final int MIN_DELAY_SECONDS = 5;
    public static final int MAX_DELAY_SECONDS = 3600;
    public static final int DEFAULT_DELAY_SECONDS = 30;
    public static final int DEFAULT_SPEED = 2;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int delaySeconds = DEFAULT_DELAY_SECONDS;
    public boolean enabled = true;
    public boolean music = true;
    public boolean letterbox = true;
    public int speed = DEFAULT_SPEED;

    private Path path;

    public void load(MinecraftClient client) {
        path = client.runDirectory.toPath().resolve("config").resolve("afkshots.json");
        try {
            if (Files.exists(path)) {
                AfkShotsConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), AfkShotsConfig.class);
                if (loaded != null) {
                    delaySeconds = clampDelay(loaded.delaySeconds);
                    enabled = loaded.enabled;
                    music = loaded.music;
                    letterbox = loaded.letterbox;
                    speed = clampSpeed(loaded.speed);
                }
            }
        } catch (Exception ignored) {
            // Keep safe defaults if a damaged config is encountered.
        }
        save();
    }

    public void save() {
        if (path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // A failed save must never stop Minecraft or the cinematic.
        }
    }

    public static int clampDelay(int value) {
        return Math.max(MIN_DELAY_SECONDS, Math.min(MAX_DELAY_SECONDS, value));
    }

    public static int clampSpeed(int value) {
        return Math.max(1, Math.min(5, value));
    }

    public void reset() {
        delaySeconds = DEFAULT_DELAY_SECONDS;
        enabled = true;
        music = true;
        letterbox = true;
        speed = DEFAULT_SPEED;
    }
}
