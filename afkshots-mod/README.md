# AFK Shots

**AFK Shots** is a lightweight client-side Minecraft Fabric mod by **Soulsensei**.

Modrinth creator profile: https://modrinth.com/user/soulsensei

## Features

- Automatically starts a slow cinematic AFK camera after a configurable delay.
- Minimum AFK delay: **5 seconds**.
- Maximum AFK delay: **60 minutes**.
- Press **J** to open the complete settings GUI.
- No commands required.
- **Start AFK Shots Now** directly from the GUI.
- Toggle AFK Shots on/off.
- Toggle vanilla Minecraft music on/off.
- Toggle cinematic letterbox bars on/off.
- Five orbit-speed levels.
- Reset all settings to defaults.
- Settings persist to `config/afkshots.json`.
- Any key press, mouse click, movement or manual look change ends the cinematic immediately.
- Client-side: intended for singleplayer and multiplayer servers without requiring the server to install the mod.
- Works independently of Survival, Adventure and Creative game modes.
- Uses vanilla Minecraft sound events; the mod does not bundle music files.
- No mixins and no heavy rendering system.

## Minecraft versions

The release project is organized around **separate version-specific builds** from Minecraft **1.21.4 through 1.21.11**. Do not treat one compiled JAR as a universal binary for every Minecraft version: Fabric/Loom remaps Minecraft for each target version.

Target matrix:

- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11

The source intentionally uses stable public client APIs where possible. Each target must be compiled against its own Minecraft/Yarn/Fabric API coordinates before publishing that target JAR.

## Default settings

| Setting | Default |
|---|---:|
| Enabled | ON |
| Start after | 30 seconds |
| Minimum | 5 seconds |
| Maximum | 3600 seconds |
| Vanilla music | ON |
| Cinematic bars | ON |
| Orbit speed | 2 / 5 |
| Settings key | J |

## Creator

**Soulsensei**

Modrinth: https://modrinth.com/user/soulsensei

## Important

The source package is prepared for the 1.21.4–1.21.11 release range, but a claim of perfect compatibility requires compiling and launching each target version. This environment does not contain a configured Minecraft/Loom dependency cache, so no fabricated runtime-test claim is made here.

## Smooth Cinematic Camera

AFK Shots uses a lightweight cinematic shot director rather than a recording/replay system. The camera uses eased multi-phase paths, slow orbit/reveal movements, and Minecraft's normal entity rotation interpolation. This keeps the cinematic smooth at normal render FPS without continuously recording frames, creating replay data, or running a second renderer.

The design goal is a **Replay-Mod-inspired cinematic feel with low overhead**, not a replacement for Replay Mod's full free-camera/replay recording system.

### Performance goals
- No frame recording or video encoding.
- No custom 3D assets required.
- No shader pipeline.
- No per-frame allocations in the camera path.
- Camera path math runs once per game tick; Minecraft handles normal render interpolation.
- Intended to remain lightweight on modest/decent PCs while the game's own FPS is preserved.
