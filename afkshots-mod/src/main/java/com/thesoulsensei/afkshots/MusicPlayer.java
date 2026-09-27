package com.thesoulsensei.afkshots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Handles optional vanilla Minecraft music during AFK cinematic shots.
 *
 * No audio files are bundled with the mod.
 * The mod only uses sounds already provided by Minecraft.
 *
 * If a sound ID is unavailable on the current Minecraft version,
 * it is skipped safely instead of causing a crash.
 */
public class MusicPlayer {

    /*
     * Vanilla music candidates.
     *
     * The list is shuffled every time a cinematic starts so the
     * same track is not always selected first.
     */
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

    /**
     * Enables or disables cinematic music.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether cinematic music is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Starts a random available vanilla music track.
     *
     * If music is disabled or a track is already playing,
     * nothing happens.
     */
    public void start(MinecraftClient client) {
        if (!enabled || currentInstance != null) {
            return;
        }

        List<Identifier> shuffledTracks = new ArrayList<>(CANDIDATE_TRACKS);
        Collections.shuffle(shuffledTracks, random);

        for (Identifier id : shuffledTracks) {

            /*
             * Minecraft 1.21.4:
             * Registry#getEntry(Identifier) returns
             * Optional<RegistryEntry.Reference<SoundEvent>>.
             */
            Optional<RegistryEntry.Reference<SoundEvent>> sound =
                    Registries.SOUND_EVENT.getEntry(id);

            if (sound.isPresent()) {

                /*
                 * 1.21.4 supports the RegistryEntry version
                 * of PositionedSoundInstance.master().
                 */
                currentInstance = PositionedSoundInstance.master(
                        sound.get(),
                        1.0f
                );

                client.getSoundManager().play(currentInstance);
                return;
            }
        }

        /*
         * None of the candidate tracks exist on this version.
         * Simply continue without music.
         */
    }

    /**
     * Stops the currently playing cinematic music.
     */
    public void stop(MinecraftClient client) {
        if (currentInstance != null) {
            client.getSoundManager().stop(currentInstance);
            currentInstance = null;
        }
    }

    /**
     * Stops the current track and clears the instance.
     */
    public void reset(MinecraftClient client) {
        stop(client);
    }
}
