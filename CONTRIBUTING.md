# Contributing

## Setup

You need Java 25 for Minecraft 26.3 and Java 21 for 1.21.1. In IntelliJ IDEA (2025.2 or newer), open the folder that contains `gradlew.bat`, set the Project SDK and the Gradle JVM to Java 25, and refresh the Gradle project. Then start the game with the **Fabric Client** or **NeoForge Client** run configuration.

`gradlew collectJars` builds every jar and copies them to `build/libs/<mod version>/`.

## How the code is laid out

One codebase builds three jars: Fabric and NeoForge for 26.3, and NeoForge for 1.21.1. The build uses [Stonecutter](https://stonecutter.kikugie.dev).

- Common code is in `src/`, Fabric code in `fabric/src` and NeoForge code in `neoforge/src`. Shared code keeps to Java 21.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The optional **Stonecutter Dev** plugin highlights them.
- To work on another version, run the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state.

## How the code is split

The common code is split into core, features and hooks.

- **Core** (`core` and the packages under it) is what the whole mod shares: the config, text, age, the animal record, events, tick order and registration. Core imports only core.
- A **feature** is a top-level package such as `eggs`, `milk` or `herd`. Other features talk to it only through its `api` package, which holds an interface and the values it passes. A feature imports core, its own classes and other features' `api` packages. An `api` package imports only core and other `api` packages.
- A **hook** plugs the mod into the game: mixins, `client`, `look`, `command`, the Jade plugin, the `fabric` and `neoforge` modules, `BarnyardRomance` and `BarnyardFeatures`. Hooks and tests may import anything.
- `FeatureBoundaryScanTest` checks every import against these rules, with no exceptions. When an import breaks them, add a method to an `api`, post an event, or move the class.
- To add a feature, write a `<Name>Feature` class in its package and add it to the list in `BarnyardFeatures`. It registers everything the feature brings: items and blocks, its fields in the animal record, attachments, event listeners and tick hooks. List its items by id in `BarnyardTab`; a stack that needs more than an id is registered with `registrar.tabEntry` and named in `BarnyardTab`.
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

These runs test compatibility with [Naturalist](https://modrinth.com/mod/naturalist). Each has its own game folder under `runs/naturalist-*`, and Gradle copies the jar into that folder's `mods`, so the mod doesn't compile against Naturalist or ship it. On NeoForge the IntelliJ run configs copy it too, after a Gradle sync. Fabric's runs start from Gradle only.

| Command | What it runs |
| --- | --- |
| `gradlew :neoforge:1.21.1:runClientNaturalist`, `gradlew :fabric:26.3:runClientNaturalist` | The game with Naturalist |
| `gradlew :neoforge:1.21.1:runGameTestServerNaturalist`, `gradlew :fabric:26.3:runGametestNaturalist` | Game tests with Naturalist loaded |

Naturalist needs a newer NeoForge on 26.3 than the mod builds against, so add `-Pneoforge_version=26.3.0.101` to the `:neoforge:26.3` versions of these commands.

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
