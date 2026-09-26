package com.thesoulsensei.afkshots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;

/** Lightweight client-side AFK cinematic controller. */
public final class CinematicController {
    // Degrees per tick. The controller changes the target smoothly and Minecraft
    // interpolates entity rotation between ticks, keeping the camera inexpensive.
    private static final float[] SPEEDS = {0.055f, 0.085f, 0.12f, 0.17f, 0.24f};
    private static final float LOOK_EPSILON = 0.08f;
    private static final float PITCH_MIN = -18f;
    private static final float PITCH_MAX = 8f;
    private static final int SHOT_LENGTH_TICKS = 20 * 18;

    private final AfkTracker tracker;
    private final AfkShotsConfig config;
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private boolean active;
    private float orbitYaw;
    private float targetYaw;
    private float currentPitch;
    private float targetPitch;
    private float lastSetYaw;
    private float lastSetPitch;
    private long activeTicks;
    private int shotPhase;
    private Perspective previousPerspective;

    public CinematicController(AfkTracker tracker, AfkShotsConfig config) {
        this.tracker = tracker;
        this.config = config;
    }

    public void applyConfig() {
        config.delaySeconds = AfkShotsConfig.clampDelay(config.delaySeconds);
        config.speed = AfkShotsConfig.clampSpeed(config.speed);
        musicPlayer.setEnabled(config.music);
    }

    public void tick(MinecraftClient client) {
        InputWatcher.installIfNeeded(client);
        boolean rawInputSeen = InputWatcher.consumeInputSeen();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            if (active) stop(client);
            return;
        }

        if (active) {
            tracker.updatePositionOnly(player);
            if (rawInputSeen || lookChangedSinceLastDrive(player) || tracker.didPositionChange() || client.currentScreen != null) {
                stop(client);
                return;
            }
            driveCamera(player);
            return;
        }

        tracker.update(player);
        if (rawInputSeen) tracker.resetIdle();
        long thresholdTicks = config.delaySeconds * 20L;
        if (config.enabled && tracker.isIdleFor(thresholdTicks) && client.currentScreen == null) {
            start(client, player);
        }
    }

    public void startNow(MinecraftClient client) {
        if (client != null && client.player != null && !active) start(client, client.player);
    }

    private void start(MinecraftClient client, ClientPlayerEntity player) {
        active = true;
        activeTicks = 0;
        shotPhase = 0;
        orbitYaw = player.getYaw();
        targetYaw = orbitYaw;
        currentPitch = player.getPitch();
        targetPitch = Math.max(PITCH_MIN, Math.min(PITCH_MAX, currentPitch));
        lastSetYaw = orbitYaw;
        lastSetPitch = currentPitch;
        previousPerspective = client.options.getPerspective();
        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        musicPlayer.setEnabled(config.music);
        musicPlayer.start(client);
    }

    private void driveCamera(ClientPlayerEntity player) {
        activeTicks++;

        // A lightweight "director" inspired by replay-camera language: each shot
        // is a deterministic keyframe path with cubic easing. It never records or
        // renders another world, so the cost stays close to ordinary third-person.
        final int shotLength = SHOT_LENGTH_TICKS;
        long phaseTick = activeTicks % shotLength;
        shotPhase = (int) ((activeTicks / shotLength) % 6);
        float t = phaseTick / (float) shotLength;
        float e = cubicEase(t);
        float e2 = cubicEase(Math.min(1f, t * 1.08f));
        float speed = SPEEDS[config.speed - 1];
        float span = 22f + speed * 34f;
        float sign = (shotPhase & 1) == 0 ? 1f : -1f;

        float yawOffset;
        float pitch;
        switch (shotPhase) {
            case 0 -> { // Establishing orbit: wide, slow, almost drone-like.
                yawOffset = sign * span * (0.10f + 0.90f * e);
                pitch = -9f + 3f * (float)Math.sin(Math.PI * e);
            }
            case 1 -> { // Push-in illusion: compressed yaw arc + upward reveal.
                yawOffset = sign * span * 0.72f * (1f - (float)Math.cos(Math.PI * e * 0.5));
                pitch = -15f + 10f * e;
            }
            case 2 -> { // Parallax-style side sweep.
                yawOffset = -sign * span * 0.95f * e2;
                pitch = -7f + 4f * (float)Math.sin(Math.PI * e);
            }
            case 3 -> { // Hero orbit, then settle toward the original heading.
                float arc = (float)Math.sin(Math.PI * e);
                yawOffset = sign * span * 0.90f * arc;
                pitch = -4f - 8f * arc;
            }
            case 4 -> { // High-to-low reveal.
                yawOffset = sign * span * 0.48f * e;
                pitch = 5f - 19f * e;
            }
            default -> { // Soft cinematic reset before the next sequence.
                yawOffset = sign * span * 0.18f * (1f - e);
                pitch = -10f + 2f * (float)Math.sin(Math.PI * 2.0 * e);
            }
        }

        targetYaw = orbitYaw + yawOffset;
        targetPitch = Math.max(PITCH_MIN, Math.min(PITCH_MAX, pitch));

        // Two-stage smoothing prevents the stepped 20-tick look of a basic AFK
        // spinner. Minecraft then interpolates entity rotation between ticks.
        orbitYaw = smoothAngle(orbitYaw, targetYaw, 0.24f);
        currentPitch = smooth(currentPitch, targetPitch, 0.20f, 0.045f);
        player.setYaw(orbitYaw);
        player.setPitch(currentPitch);
        lastSetYaw = orbitYaw;
        lastSetPitch = currentPitch;
    }

    private static float cubicEase(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private static float smooth(float current, float target, float response, float maxStep) {
        float delta = target - current;
        float step = delta * response;
        if (step > maxStep) step = maxStep;
        if (step < -maxStep) step = -maxStep;
        return current + step;
    }

    private static float smoothAngle(float current, float target, float amount) {
        return current + wrapAngle(target - current) * amount;
    }

    private static float lerp(float current, float target, float amount) {
        return current + (target - current) * amount;
    }

    private boolean lookChangedSinceLastDrive(ClientPlayerEntity player) {
        float yawDiff = wrapAngle(player.getYaw() - lastSetYaw);
        float pitchDiff = player.getPitch() - lastSetPitch;
        return Math.abs(yawDiff) > LOOK_EPSILON || Math.abs(pitchDiff) > LOOK_EPSILON;
    }

    private static float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    public void stop(MinecraftClient client) {
        active = false;
        tracker.resetIdle();
        musicPlayer.stop(client);
        if (previousPerspective != null) {
            client.options.setPerspective(previousPerspective);
            previousPerspective = null;
        }
    }

    public boolean isActive() { return active; }
    public boolean isEnabled() { return config.enabled; }
    public boolean isMusicEnabled() { return config.music; }
    public boolean isLetterboxEnabled() { return config.letterbox; }
    public int getThresholdSeconds() { return config.delaySeconds; }
    public int getSpeed() { return config.speed; }

    public void setEnabled(boolean value) { config.enabled = value; config.save(); }
    public void setMusicEnabled(boolean value) { config.music = value; musicPlayer.setEnabled(value); config.save(); }
    public void setLetterboxEnabled(boolean value) { config.letterbox = value; config.save(); }
    public void setThresholdSeconds(int seconds) { config.delaySeconds = AfkShotsConfig.clampDelay(seconds); config.save(); }
    public void setSpeed(int speed) { config.speed = AfkShotsConfig.clampSpeed(speed); config.save(); }

    public void resetSettings() {
        config.reset();
        applyConfig();
        config.save();
    }
}
