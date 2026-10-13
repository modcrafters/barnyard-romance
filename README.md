# Barnyard Romance

A Minecraft Java mod (Fabric + NeoForge, one shared codebase) that adds male/female animals, sex-based size and visuals, realistic breeding rules, pregnancy, and food-driven pregnancy progress, inspired by classic TerraFirmaCraft, working for vanilla and (most) modded animals.

See `docs/` for design, and `AGENTS.md` for implementation instructions for Cursor.

This checkout is the Milestone 1 skeleton: it compiles and launches on both loaders. Sex, size, and `/barnyard info` are not implemented yet.

## Versions

- Minecraft **26.3** (latest stable release with both Fabric and NeoForge)
- Java **25**
- Fabric Loader 0.19.5, Fabric API 0.162.0+26.3
- NeoForge 26.3.0.76

## Import and run in IntelliJ IDEA

IDEA 2025.2 or newer is required for Java 25.

1. Open this folder (the one that contains `gradlew.bat`) as a project.
2. Set the Project SDK and the Gradle JVM to Java 25:
   - `File > Project Structure > Project SDK`
   - `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`
3. Refresh the Gradle project.
4. Run one of these configurations:
   - **Fabric Client** or **Fabric Server** (Gradle run configurations)
   - **NeoForge Client** or **NeoForge Server** (Application run configurations)

`gradlew build` produces the loader jars under `fabric/build/libs` and `neoforge/build/libs`. The `common` jar is not a mod you install.
