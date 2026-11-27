# Contributing

## Setup

You need Java 25 for Minecraft 26.3 and Java 21 for 1.21.1. In IntelliJ IDEA (2025.2 or newer), open the folder that contains `gradlew.bat`, set the Project SDK and the Gradle JVM to Java 25, and refresh the Gradle project. Then start the game with the **Fabric Client** or **NeoForge Client** run configuration.

`gradlew collectJars` builds every jar and copies them to `build/libs/<mod version>/`.

## How the code is laid out

One codebase builds three jars: Fabric and NeoForge for 26.3, and NeoForge for 1.21.1. The build uses [Stonecutter](https://stonecutter.kikugie.dev).

- Common code is in `src/`, Fabric code in `fabric/src` and NeoForge code in `neoforge/src`. Shared code keeps to Java 21.
- The public API other mods compile against is its own source set, `src/api`. Each loader's `src/api` holds that loader's native events (`api/neoforge`, `api/fabric`). The mod jars include all of it; `apiJar` builds the separate API jars.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The optional **Stonecutter Dev** plugin highlights them.
- To work on another version, run the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state. The one exception is `forge/src`: Forge only has 1.20.1, so its code is written for 1.20.1 and stays that way.

## How the code is split

The common code is split into core, features and hooks.

- **Core** (`core` and the packages under it) is what the whole mod shares: the config, text, age, the animal record, events, tick order and registration. Core imports only core.
- A **feature** is a top-level package such as `eggs`, `milk` or `herd`. Other features talk to it only through its `api` package, which holds a `<Name>Api` interface and the values it passes (some are interfaces callers implement, such as stats' `AnimalSubject`). A feature imports core, its own classes and other features' `api` packages. An `api` package imports only core and other `api` packages.
- A **hook** plugs the mod into the game: mixins, `client`, `look`, `command`, the Jade plugin, the `fabric` and `neoforge` modules, `BarnyardRomance` and `BarnyardFeatures`. Hooks and tests may import anything.
- Core, features and hooks may all import the public API (the top-level `api` package in `src/api`). The API imports nothing from the mod: Gradle keeps `src/api` from seeing `main`, and `ApiSourcesTest` keeps it to Minecraft, the JDK and itself.
- `FeatureBoundaryScanTest` checks every import against these rules, with no exceptions. When an import breaks them, add a method to an `api`, post an event, or move the class.
- To add a feature, write a `<Name>Feature` class in its package and add it to the list in `BarnyardFeatures`. It registers everything the feature brings: items and blocks, its fields in the animal record, attachments, event listeners and tick hooks. List its items by id in `BarnyardTab`; a stack that needs more than an id is registered with `registrar.tabEntry`, named in `BarnyardTab` and listed in `TabEntries`, which start-up checks.
- Give a feature an `api` package only when core or another feature calls into it. Callers get it with `FeaturesRepository.get(<Name>Api.class)`.
- A tick hook's name must also be placed in the order in `AnimalTicks`. A tick hook with no place there stops the game at start-up.

## Tests

| Command | What it runs |
| --- | --- |
| `gradlew :26.3:test`, `gradlew :1.21.1:test` | Unit tests for the pure rules |
| `gradlew :fabric:26.3:runGametest` | Game tests on Fabric |
| `gradlew :neoforge:26.3:runGameTestServer`, `gradlew :neoforge:1.21.1:runGameTestServer` | Game tests on NeoForge |
| `gradlew :fabric:26.3:runClientGametest` | Client tests, which open a real game window |

Game tests live in `src/gametest` (shared), `fabric/src/gametest` and `neoforge/src/gametest`, with 1.21.1 overrides under `versions/1.21.1/src/gametest`. They build a small second mod, `barnyard_romance_gametest`, that only the game test runs load, so none of them ship in the mod's jars and main code can't depend on them.

A new shared game test also needs a `@GameTest` wrapper in `fabric/src/gametest/.../fabric/gametest/`. Without one, it's silently skipped on Fabric.

### With Naturalist

These runs test compatibility with [Naturalist](https://modrinth.com/mod/naturalist). Each has its own game folder under `runs/naturalist-*`, and Gradle copies the jar into that folder's `mods`, so the mod doesn't compile against Naturalist or ship it. On NeoForge and Forge the IntelliJ run configs copy it too, after a Gradle sync. Fabric's runs start from Gradle only.

| Command | What it runs |
| --- | --- |
| `gradlew :neoforge:1.21.1:runClientNaturalist`, `gradlew :fabric:26.3:runClientNaturalist`, `gradlew :forge:1.20.1:runClientNaturalist` | The game with Naturalist |
| `gradlew :neoforge:1.21.1:runGameTestServerNaturalist`, `gradlew :fabric:26.3:runGametestNaturalist`, `gradlew :forge:1.20.1:runGameTestServerNaturalist` | Game tests with Naturalist loaded |

Naturalist needs a newer NeoForge on 26.3 than the mod builds against, so add `-Pneoforge_version=26.3.0.101` to the `:neoforge:26.3` versions of these commands.

### With the farm-animal mods

These runs test compatibility with the farm-animal mods the mod has hooks for: [Untitled Duck Mod](https://modrinth.com/mod/untitled-duck-mod), [Friends&Foes](https://modrinth.com/mod/friends-and-foes), [Meadow](https://modrinth.com/mod/lets-do-meadow), [Autumnity](https://modrinth.com/mod/autumnity) and [Environmental](https://modrinth.com/mod/environmental), with the libraries they need. Each run has its own game folder under `runs/farm-mods-*`, and Gradle copies exactly the jars for that build into its `mods`, so the mod doesn't compile against them or ship them. On NeoForge and Forge the IntelliJ run configs copy them too, after a Gradle sync. Fabric's runs start from Gradle only. The jars are pinned by Modrinth version id in `gradle/farm-mods-runs.gradle`.

| Command | What it runs |
| --- | --- |
| `gradlew :neoforge:1.21.1:runClientFarmMods`, `gradlew :neoforge:26.3:runClientFarmMods`, `gradlew :fabric:26.3:runClientFarmMods`, `gradlew :forge:1.20.1:runClientFarmMods` | The game with the farm-animal mods |
| `gradlew :neoforge:1.21.1:runGameTestServerFarmMods`, `gradlew :neoforge:26.3:runGameTestServerFarmMods`, `gradlew :fabric:26.3:runGametestFarmMods`, `gradlew :forge:1.20.1:runGameTestServerFarmMods` | Game tests with the mods loaded |

NeoForge 1.21.1 and Forge 1.20.1 load all five mods. Only Untitled Duck Mod and Friends&Foes have a 26.3 build, so the 26.3 runs load just those two. A run names the mods it should have in `-Dbarnyard_romance.expect_mods`, and a farm-mod game test fails, rather than passes empty, when one of them is missing.

Untitled Duck Mod needs a newer NeoForge on 26.3 than the mod builds against, so add `-Pneoforge_version=26.3.0.103` to the `:neoforge:26.3` versions of these commands.

### With Fresh Animations

`gradlew :fabric:26.3:runClientFreshAnimations`, `gradlew :neoforge:26.3:runClientFreshAnimations`, `gradlew :neoforge:1.21.1:runClientFreshAnimations` or `gradlew :forge:1.20.1:runClientFreshAnimations` starts the game with [Fresh Animations](https://modrinth.com/resourcepack/fresh-animations) and the two mods it needs, Entity Texture Features and Entity Model Features. The run has its own game folder, `runs/fresh-animations-client`. Gradle copies the two jars into its `mods` and the pack into its `resourcepacks`, and switches the pack on in its `options.txt`. On NeoForge and Forge the IntelliJ run config does the same after a Gradle sync.

Forge 1.20.1's mods are published with Minecraft's SRG names, and its dev game runs on Mojang's, so on Forge every one of these runs remaps its jars first (`runModsSource` in `gradle/modrinth-maven.gradle`). A dev run can't show whether a mixin into another mod's class finds its target in a real game: the mod's compat mixins name such a method by both names on 1.20.1, so check the jar's refmap and a real Forge server when you change one.

## Before a release

CI runs client tests only on Fabric 26.3. NeoForge 1.21.1 has its own copy of the renderer mixin, which shares almost no lines with 26.3's, so check it by eye before you push a release tag. Start the game with `gradlew :neoforge:1.21.1:runClient` in a creative world:

- A pregnant cow widens sideways as her pregnancy goes on, and only sideways.
- Rams, bulls and billy goats have horns, a calf has none, and a goat shows only the mod's horns, not vanilla's as well.
- Roosters have a comb and wattles, and hens have none.
- A hen sitting in a nesting box is drawn inside it.
- Sneaking while looking at an animal shows its stat card, and the herd book opens, pages and shows heads.

## Pull requests

- CI runs all of the above on every pull request. The `all-versions` check must pass before a change can merge.
- If players will notice the change, add short bullets for them at the top of `## Unreleased` in `CHANGELOG.md`. Changes to only CI or docs don't need any.
- New text goes in `assets/barnyard_romance/lang/en_us.json`. `LangFileTest` fails if the code uses a key the file doesn't have, or if the file has a key nothing uses.
