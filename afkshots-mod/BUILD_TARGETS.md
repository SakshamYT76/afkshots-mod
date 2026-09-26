# Build targets

The default build is Minecraft **1.21.4**.

Example:

```text
./gradlew build -Ptarget_mc=1.21.4 -Ptarget_yarn=1.21.4+build.1 -Ptarget_loader=0.16.9 -Ptarget_fabric=0.106.0+1.21.4
```

For 1.21.5 through 1.21.11, use the corresponding Yarn/Fabric API releases from Fabric's Maven repository. Each build produces a version-specific archive named like:

`afkshots-1.0.0+mc1.21.4.jar`

This is intentional: Fabric's own documentation explains that Loom handles Minecraft version-specific mappings/remapping, and Minecraft 1.21.11 or older uses the obfuscated/remapped Loom path. The project therefore does not claim one compiled JAR is magically identical across every target.
