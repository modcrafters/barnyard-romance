# Barnyard Romance

A Minecraft Java mod (Fabric + NeoForge, one shared codebase) that adds male/female animals, sex-based size and visuals, realistic breeding rules, pregnancy, and food-driven pregnancy progress, inspired by classic TerraFirmaCraft, working for vanilla and (most) modded animals.

See `docs/` for design, and `AGENTS.md` for implementation instructions for Cursor.

Animals get a saved sex and a sex-based `minecraft:scale`. Wild animals are only S or M. A baby takes the average of its parents and has a chance to come out one band larger, so L and XL come from breeding. The tooltip shows S, M, L, or XL. A small animal drops 2–3 meat and a large one drops 8–9. `/barnyard info`, while you look at an animal, still includes the exact scale. Looking at an animal also shows the tooltip above it. A male and a female breed into a pregnancy instead of an instant baby. The default lasts one Minecraft day (20 real minutes) and the litter size is 1; both are set in `config/barnyard_romance.json`. Food-driven progress is not in yet.

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
