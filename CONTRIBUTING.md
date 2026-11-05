# Contributing

## Setup

You need Java 25 for Minecraft 26.3 and Java 21 for 1.21.1. In IntelliJ IDEA (2025.2 or newer), open the folder that contains `gradlew.bat`, set the Project SDK and the Gradle JVM to Java 25, and refresh the Gradle project. Then start the game with the **Fabric Client** or **NeoForge Client** run configuration.

`gradlew collectJars` builds every jar and copies them to `build/libs/<mod version>/`.

## How the code is laid out

One codebase builds three jars: Fabric and NeoForge for 26.3, and NeoForge for 1.21.1. The build uses [Stonecutter](https://stonecutter.kikugie.dev).

- Common code is in `src/`, Fabric code in `fabric/src` and NeoForge code in `neoforge/src`. Shared code keeps to Java 21.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The optional **Stonecutter Dev** plugin highlights them.
- To work on another version, run the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state.

## Tests

| Command | What it runs |
| --- | --- |
| `gradlew :26.3:test`, `gradlew :1.21.1:test` | Unit tests for the pure rules |
| `gradlew :fabric:26.3:runGametest` | Game tests on Fabric |
| `gradlew :neoforge:26.3:runGameTestServer`, `gradlew :neoforge:1.21.1:runGameTestServer` | Game tests on NeoForge |
| `gradlew :fabric:26.3:runClientGametest` | Client tests, which open a real game window |

A new shared game test also needs a `@GameTest` wrapper in `fabric/src/.../fabric/gametest/`. Without one, it's silently skipped on Fabric.

### With Naturalist

These runs test compatibility with [Naturalist](https://modrinth.com/mod/naturalist). Each has its own game folder under `runs/naturalist-*`, and Gradle copies the jar into that folder's `mods`, so the mod doesn't compile against Naturalist or ship it. Start them from Gradle, because an IDE run config doesn't copy the jar.

| Command | What it runs |
| --- | --- |
| `gradlew :neoforge:1.21.1:runClientNaturalist`, `gradlew :fabric:26.3:runClientNaturalist` | The game with Naturalist |
| `gradlew :neoforge:1.21.1:runGameTestServerNaturalist`, `gradlew :fabric:26.3:runGametestNaturalist` | Game tests with Naturalist loaded |

Naturalist needs a newer NeoForge on 26.3 than the mod builds against, so add `-Pneoforge_version=26.3.0.101` to the `:neoforge:26.3` versions of these commands.

## Pull requests

- CI runs all of the above on every pull request. The `all-versions` check must pass before a change can merge.
- If players will notice the change, add short bullets for them at the top of `## Unreleased` in `CHANGELOG.md`. Changes to only CI or docs don't need any.
- New text goes in `assets/barnyard_romance/lang/en_us.json`. `LangFileTest` fails if the code uses a key the file doesn't have, or if the file has a key nothing uses.
