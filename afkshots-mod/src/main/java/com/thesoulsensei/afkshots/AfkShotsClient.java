package com.thesoulsensei.afkshots;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AfkShotsClient implements ClientModInitializer {
    private final AfkTracker tracker = new AfkTracker();
    private final AfkShotsConfig config = new AfkShotsConfig();
    private final CinematicController controller = new CinematicController(tracker, config);
    private final HudOverlay hud = new HudOverlay(controller);
    private KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        config.load(MinecraftClient.getInstance());
        controller.applyConfig();
        hud.register();

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.afkshots.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.afkshots"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            controller.tick(client);
            while (openSettingsKey.wasPressed()) {
                if (client.currentScreen == null && !controller.isActive()) {
                    client.setScreen(new AfkShotsSettingsScreen(null, controller));
                }
            }
        });
    }
}
