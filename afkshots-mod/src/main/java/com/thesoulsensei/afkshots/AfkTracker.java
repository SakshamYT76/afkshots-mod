package com.thesoulsensei.afkshots;

import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Detects AFK by watching the player entity's own position and look
 * angle across ticks, using only public getters. Deliberately avoids
 * mixing into Mouse/Keyboard/Camera internals, since those are the
 * classes most likely to shift between Minecraft versions.
 */
public class AfkTracker {

    private static final double POSITION_EPSILON = 0.0008;
    private static final float ANGLE_EPSILON = 0.05f;

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private boolean initialized = false;

    private long idleTicks = 0;
    private boolean positionChangedThisTick = false;
    private boolean lookChangedThisTick = false;

    /** Normal per-tick update: watches both position and look angle. */
    public void update(ClientPlayerEntity player) {
        double x = player.getX(), y = player.getY(), z = player.getZ();
        float yaw = player.getYaw(), pitch = player.getPitch();

        if (!initialized) {
            lastX = x; lastY = y; lastZ = z;
            lastYaw = yaw; lastPitch = pitch;
            initialized = true;
            positionChangedThisTick = false;
            lookChangedThisTick = false;
            return;
        }

        positionChangedThisTick = Math.abs(x - lastX) > POSITION_EPSILON
                || Math.abs(y - lastY) > POSITION_EPSILON
                || Math.abs(z - lastZ) > POSITION_EPSILON;
        lookChangedThisTick = angleDelta(yaw, lastYaw) > ANGLE_EPSILON
                || Math.abs(pitch - lastPitch) > ANGLE_EPSILON;

        if (positionChangedThisTick || lookChangedThisTick) {
            idleTicks = 0;
        } else {
            idleTicks++;
        }

        lastX = x; lastY = y; lastZ = z;
        lastYaw = yaw; lastPitch = pitch;
    }

    /**
     * Used while the cinematic controller is itself driving the
     * player's yaw/pitch, so our own camera motion isn't mistaken
     * for the player looking around. Only position counts as "real"
     * activity in this mode.
     */
    public void updatePositionOnly(ClientPlayerEntity player) {
        double x = player.getX(), y = player.getY(), z = player.getZ();
        positionChangedThisTick = Math.abs(x - lastX) > POSITION_EPSILON
                || Math.abs(y - lastY) > POSITION_EPSILON
                || Math.abs(z - lastZ) > POSITION_EPSILON;
        lookChangedThisTick = false;
        lastX = x; lastY = y; lastZ = z;
    }

    private static float angleDelta(float a, float b) {
        float d = Math.abs(a - b) % 360f;
        return d > 180f ? 360f - d : d;
    }

    public long getIdleTicks() {
        return idleTicks;
    }

    public boolean isIdleFor(long ticks) {
        return idleTicks >= ticks;
    }

    public boolean didPositionChange() {
        return positionChangedThisTick;
    }

    public void resetIdle() {
        idleTicks = 0;
    }
}
