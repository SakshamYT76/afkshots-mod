package com.thesoulsensei.afkshots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Plays one of vanilla Minecraft's own built-in music tracks while the
 * cinematic is active. No audio is bundled with this mod -- it only
 * triggers playback of music that already ships inside the base game,
 * exactly like the main menu or a jukebox does. That means there's
 * effectively no extra CPU/RAM cost beyond what the vanilla sound
 * engine already spends playing one track, so it should be fine even
 * on low-end/"trash" hardware -- it isn't decoding, mixing, or
 * streaming anything extra of its own.
 *
 * Tracks are looked up by their vanilla resource-ID string (e.g.
 * "minecraft:music.overworld.forest") instead of by Java field name.
 * Resource IDs are far more stable across versions than the constant
 * names in the SoundEvents class, and any ID that doesn't exist on a
 * given Minecraft version is silently skipped in favor of the next
 * candidate, so a missing track can't crash the mod -- worst case it
 * just plays no music that session.
 *
 * NOTE: PositionedSoundInstance.master(SoundEvent, float) is the
 * signature used across most recent versions. A small number of
 * releases wrap SoundEvent in RegistryEntry<SoundEvent> for this call
 * instead -- if this fails to compile on the version you target,
 * that's the one line/type to adjust.
 */
public class MusicPlayer {

    private static final List<Identifier> CANDIDATE_TRACKS = List.of(
            Identifier.of("minecraft", "music.overworld.forest"),
            Identifier.of("minecraft", "music.overworld.meadow"),
            Identifier.of("minecraft", "music.overworld.old_growth_taiga"),
            Identifier.of("minecraft", "music.overworld.flower_forest"),
            Identifier.of("minecraft", "music.overworld.sparse_jungle"),
            Identifier.of("minecraft", "music.overworld.swamp"),
            Identifier.of("minecraft", "music.overworld.badlands"),
            Identifier.of("minecraft", "music.overworld.jagged_peaks"),
            Identifier.of("minecraft", "music.creative"),
            Identifier.of("minecraft", "music.menu")
    );

    private final Random random = new Random();
    private SoundInstance currentInstance;
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void start(MinecraftClient client) {
        if (!enabled || currentInstance != null) {
            return;
        }

        List<Identifier> shuffled = new ArrayList<>(CANDIDATE_TRACKS);
        Collections.shuffle(shuffled, random);

        for (Identifier id : shuffled) {
            Optional<SoundEvent> sound = Registries.SOUND_EVENT.getOrEmpty(id);
            if (sound.isPresent()) {
                currentInstance = PositionedSoundInstance.master(sound.get(), 1.0f);
                client.getSoundManager().play(currentInstance);
                return;
            }
        }
        // None of the candidate track IDs exist on this version -- skip music, no crash.
    }

    public void stop(MinecraftClient client) {
        if (currentInstance != null) {
            client.getSoundManager().stop(currentInstance);
            currentInstance = null;
        }
    }
}
