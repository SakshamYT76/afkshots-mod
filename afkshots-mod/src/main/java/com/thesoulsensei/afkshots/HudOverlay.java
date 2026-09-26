package com.thesoulsensei.afkshots;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class HudOverlay {
    private final CinematicController controller;
    public HudOverlay(CinematicController controller) { this.controller = controller; }

    public void register() { HudRenderCallback.EVENT.register(this::onHudRender); }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!controller.isActive() || !controller.isLetterboxEnabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int barHeight = Math.max(1, height / 8);
        context.fill(0, 0, width, barHeight, 0xFF000000);
        context.fill(0, height - barHeight, width, height, 0xFF000000);
    }
}
