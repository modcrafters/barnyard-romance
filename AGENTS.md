# Instructions for Cursor / coding agents

## Goal
Implement **Barnyard Romance** following `docs/DESIGN.md` and `docs/ROADMAP.md`. Start with Milestone 1 only, then stop and report.

## Tech stack
- Minecraft Java: latest stable release supported by both Fabric and NeoForge (check at start; must be >= 1.20.5 for the `minecraft:scale` attribute).
- Multi-loader Gradle project, opens cleanly in IntelliJ IDEA. Use the MultiLoader Template (jaredlll08) or Architectury layout:
  - `common/`: all loader-independent logic (sex, scale, breeding rules, pregnancy, config model, food rules). Mixins live here where possible.
  - `fabric/`: Fabric entrypoints, registration, data attachments, networking, config screen glue.
  - `neoforge/`: NeoForge entrypoints, registration, data attachments, networking, config glue.
- Common code talks to loader features through small service interfaces (ServiceLoader pattern): e.g. `EntityDataService`, `ConfigService`, `NetworkService`.
- Java 25 (Minecraft 26.3). Mod id: `barnyard_romance`. Package: `com.barnyardromance` (adjust if user wants).

## Rules
- Keep ~80-90% of logic in `common`.
- Everything data-driven or configurable where reasonable (per species on/off, size ranges, foods, durations).
- Server is authoritative for sex, pregnancy, breeding. Visuals are client-side; sync needed data to clients.
- Unsupported/custom-breeding modded animals must fail safe (feature simply off), never crash.
- Provide Gradle run configs for Fabric client/server and NeoForge client/server.
- Write short unit tests for pure logic in `common` (size rolls, inheritance, pregnancy progress math).
