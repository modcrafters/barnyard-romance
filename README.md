# Barnyard Romance

A Minecraft Java mod (Fabric + NeoForge, one shared codebase) that adds male/female animals, sex-based size and visuals, realistic breeding rules, pregnancy, and food-driven pregnancy progress, inspired by classic TerraFirmaCraft, working for vanilla and (most) modded animals.

Animals get a saved sex and a sex-based `minecraft:scale`. Wild animals are only S or M. A baby takes the average of its parents and has a chance to come out one band larger, so L and XL come from breeding. The tooltip shows S, M, L, or XL. A small animal drops 2–3 meat and a large one drops 8–9. `/barnyard info`, while you look at an animal, still includes the exact scale. Looking at an animal also shows the tooltip above it. A male and a female breed into a pregnancy instead of an instant baby. The default lasts one Minecraft day (20 real minutes) and the litter size is 1; both are set in `config/barnyard_romance.json`. Food-driven progress is not in yet.

## Versions

One codebase builds three jars:

| Minecraft | Loader | Java | Loader version |
| --- | --- | --- | --- |
| **26.3** (latest stable release with both Fabric and NeoForge) | Fabric | 25 | Fabric Loader 0.19.5, Fabric API 0.162.0+26.3 |
| **26.3** | NeoForge | 25 | NeoForge 26.3.0.76 |
| **1.21.1** (where most modpacks are) | NeoForge | 21 | NeoForge 21.1.265 |

Shared code keeps to Java 21. Every change must build and pass on all three before it can merge.

## Import and run in IntelliJ IDEA

IDEA 2025.2 or newer is required for Java 25.

1. Open this folder (the one that contains `gradlew.bat`) as a project.
2. Set the Project SDK and the Gradle JVM to Java 25:
   - `File > Project Structure > Project SDK`
   - `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`
3. Install the **Stonecutter Dev** plugin (optional; it highlights the `//?` version comments).
4. Refresh the Gradle project.
5. Run one of these configurations:
   - **Fabric Client** or **Fabric Server** (Gradle run configurations)
   - **NeoForge Client** or **NeoForge Server** (Application run configurations)

### How the build is laid out

The build uses [Stonecutter](https://stonecutter.kikugie.dev) so one codebase can build for several Minecraft versions:

- The common code is in `src/`. Fabric code is in `fabric/src`, and NeoForge code is in `neoforge/src`.
- Each Minecraft version is its own Gradle project: `:26.3` (common), `:fabric:26.3` and `:neoforge:26.3`. Each one's `gradle.properties` under `versions/<mc>` holds that version's Minecraft, Java and loader versions.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The IDE always shows one version, the *active* one.
- Switch with the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state, and CI checks this.

`gradlew collectJars` builds every loader jar and copies them to `build/libs/<mod version>/`. Each jar's own build output is under `<loader>/versions/<mc>/build/libs`. The common jars are not mods you install.

## Tests

- `gradlew :26.3:test` runs the unit tests for the pure rules.
- `gradlew :fabric:26.3:runGametest` and `gradlew :neoforge:26.3:runGameTestServer` start a headless game test server, breed real animals in a small pen, and exit non-zero if any check fails. The shared checks live in `src/.../gametest/`. NeoForge adds three more for `BabyEntitySpawnEvent`. Fabric writes a JUnit report to `fabric/versions/26.3/build/gametest/junit.xml`.
- `gradlew :fabric:26.3:runClientGametest` opens a real client and screenshots the inspector.
- On 1.21.1 the same checks run with `gradlew :1.21.1:test` and `gradlew :neoforge:1.21.1:runGameTestServer` (NeoForge only). There each test runs in its own batch, and `GameTestCompat` covers the steps that differ, such as test positions counting from one block lower.
- CI (`.github/workflows/ci.yml`) runs all of these on every pull request. The `all-versions` check passes only when every version passed; it is the check for a ruleset on `main` to require.

### 1.21.1 differences

- Pigs eat silently: their eating sound only came after 1.21.1.
- The card above an animal is placed with the FOV setting and the camera, so it doesn't follow view bobbing or zoom mods.

### 1.21.1 test gaps

- Every shared game test runs on 1.21.1.
- The Fabric client game test (`InspectorClientGameTest`) runs on 26.3 only, because there is no Fabric build for 1.21.1. On 1.21.1 the inspector, herd card and tooltip are checked by hand.
