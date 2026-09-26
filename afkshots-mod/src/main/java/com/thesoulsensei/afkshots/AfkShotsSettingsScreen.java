package com.thesoulsensei.afkshots;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/** Full client-only settings screen. No commands are required. */
public final class AfkShotsSettingsScreen extends Screen {
    private static final int STEP = 5;
    private final Screen parent;
    private final CinematicController controller;
    private ButtonWidget enabledButton;
    private ButtonWidget timeButton;
    private ButtonWidget musicButton;
    private ButtonWidget barsButton;
    private ButtonWidget speedButton;

    public AfkShotsSettingsScreen(Screen parent, CinematicController controller) {
        super(Text.literal("AFK Shots Settings"));
        this.parent = parent;
        this.controller = controller;
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = Math.max(45, this.height / 2 - 82);

        enabledButton = addDrawableChild(ButtonWidget.builder(enabledText(), b -> {
            controller.setEnabled(!controller.isEnabled()); refresh();
        }).dimensions(x - 100, y, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("- 5 seconds"), b -> adjustTime(-STEP))
                .dimensions(x - 100, y + 27, 95, 20).build());
        timeButton = addDrawableChild(ButtonWidget.builder(timeText(), b -> {})
                .dimensions(x + 5, y + 27, 95, 20).build());
        timeButton.active = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("+ 5 seconds"), b -> adjustTime(STEP))
                .dimensions(x - 100, y + 54, 200, 20).build());

        musicButton = addDrawableChild(ButtonWidget.builder(musicText(), b -> {
            controller.setMusicEnabled(!controller.isMusicEnabled()); refresh();
        }).dimensions(x - 100, y + 81, 200, 20).build());

        barsButton = addDrawableChild(ButtonWidget.builder(barsText(), b -> {
            controller.setLetterboxEnabled(!controller.isLetterboxEnabled()); refresh();
        }).dimensions(x - 100, y + 108, 200, 20).build());

        speedButton = addDrawableChild(ButtonWidget.builder(speedText(), b -> {
            controller.setSpeed(controller.getSpeed() >= 5 ? 1 : controller.getSpeed() + 1); refresh();
        }).dimensions(x - 100, y + 135, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Start AFK Shots Now"), b -> {
            controller.startNow(this.client);
            closeScreen();
        }).dimensions(x - 100, y + 162, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Defaults"), b -> {
            controller.resetSettings(); refresh();
        }).dimensions(x - 100, y + 189, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Modrinth"), b ->
                Util.getOperatingSystem().open("https://modrinth.com/user/soulsensei"))
                .dimensions(x + 5, y + 189, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> closeScreen())
                .dimensions(x - 100, y + 216, 200, 20).build());
    }

    private void adjustTime(int delta) {
        controller.setThresholdSeconds(controller.getThresholdSeconds() + delta);
        refresh();
    }

    private void refresh() {
        enabledButton.setMessage(enabledText());
        timeButton.setMessage(timeText());
        musicButton.setMessage(musicText());
        barsButton.setMessage(barsText());
        speedButton.setMessage(speedText());
    }

    private Text enabledText() { return Text.literal("AFK Shots: " + (controller.isEnabled() ? "ON" : "OFF")); }
    private Text timeText() { return Text.literal("Start after: " + controller.getThresholdSeconds() + "s"); }
    private Text musicText() { return Text.literal("Vanilla Music: " + (controller.isMusicEnabled() ? "ON" : "OFF")); }
    private Text barsText() { return Text.literal("Cinematic Bars: " + (controller.isLetterboxEnabled() ? "ON" : "OFF")); }
    private Text speedText() { return Text.literal("Orbit Speed: " + controller.getSpeed() + "/5"); }

    private void closeScreen() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AFK SHOTS"), this.width / 2, 18, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("by Soulsensei"), this.width / 2, 31, 0xFFAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
