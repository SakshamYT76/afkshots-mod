# AFK Shots Version Matrix

AFK Shots is intentionally shipped as **one build per Minecraft target**, not as one binary pretending to be universal. The source avoids mixins and uses stable client APIs, but Minecraft/Fabric binaries are still remapped per target.

| Minecraft | Source target | Build status in this source package |
|---|---|---|
| 1.21.4 | Yes | Configured default target |
| 1.21.5 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.6 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.7 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.8 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.9 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.10 | Yes | Build with matching Yarn/Fabric API coordinates |
| 1.21.11 | Yes | Build with matching Yarn/Fabric API coordinates |

## Important

`build.gradle` accepts `target_mc`, `target_yarn`, `target_loader`, and `target_fabric`. This lets a release pipeline build each target independently while keeping one shared source tree.

The exact coordinates should be selected from the Fabric/Maven release available for each target at build time. Do not copy the 1.21.4 dependency coordinates into a 1.21.11 build.

## Camera implementation note

The cinematic director intentionally avoids a custom replay recorder and heavy render pipeline. It uses lightweight eased camera targets and Minecraft's normal rotation interpolation so the feature remains suitable for lower-end/decent hardware. Version-specific builds should be compiled and smoke-tested for each target listed above.
