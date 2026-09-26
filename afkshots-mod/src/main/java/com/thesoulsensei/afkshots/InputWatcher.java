package com.thesoulsensei.afkshots;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Detects "any key was pressed" / "any mouse button was clicked" by
 * chaining directly onto the window's raw GLFW callbacks, underneath
 * Minecraft's own input handling entirely.
 *
 * This is intentionally NOT a mixin and touches no Minecraft-obfuscated
 * class at all -- GLFW is a plain native windowing library whose
 * function signatures don't change between Minecraft versions, so this
 * should keep working across 1.21.4 through 1.21.11 (and beyond)
 * without needing any per-version adjustment, unlike Mouse/Keyboard
 * mixins would.
 *
 * The previously-installed callback (Minecraft's own) is captured and
 * still invoked every time -- this only *also* listens in, it never
 * intercepts, blocks, or replaces normal input handling.
 */
public final class InputWatcher {

    private static final AtomicBoolean INPUT_SEEN = new AtomicBoolean(false);
    private static boolean installed = false;

    private InputWatcher() {
    }

    public static void installIfNeeded(MinecraftClient client) {
        if (installed || client.getWindow() == null) {
            return;
        }
        long handle = client.getWindow().getHandle();

        GLFWKeyCallback previousKey = GLFW.glfwSetKeyCallback(handle, null);
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                INPUT_SEEN.set(true);
            }
            if (previousKey != null) {
                previousKey.invoke(window, key, scancode, action, mods);
            }
        });

        GLFWMouseButtonCallback previousMouseButton = GLFW.glfwSetMouseButtonCallback(handle, null);
        GLFW.glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                INPUT_SEEN.set(true);
            }
            if (previousMouseButton != null) {
                previousMouseButton.invoke(window, button, action, mods);
            }
        });

        installed = true;
    }

    /** Returns true if a key or mouse button was pressed since the last call, and clears the flag. */
    public static boolean consumeInputSeen() {
        return INPUT_SEEN.getAndSet(false);
    }
}
