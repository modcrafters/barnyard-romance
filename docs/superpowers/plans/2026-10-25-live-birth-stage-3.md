# Stage 3: Age Stages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Animals go through baby, young, adult and old. Young animals can't breed yet. Old animals recover fertility slowly, show a faint dark heart now and then, and eventually die of old age.

**Architecture:**
- `bornTick` is a new trait on `AnimalTraits`. Like `loveUntil` and `fertilityTick`, the stage is worked out from the clock, so nothing ticks and the client shows it without new syncs.
- A pure `age` package holds the rules: `AgeStage` (the four stages) and `Aging` (stage, old start, death tick). `AgeSettings` is the per-species config.
- `AgeController` is the Minecraft-facing part: the young love gate and the old-age death check.
- The client spawns a rare, faint `old_heart` particle over old animals.

**Tech Stack:** Minecraft 26.3, Java 25, MultiLoader (common / fabric / neoforge), JUnit 5, game tests on both loaders.

**Spec:**
- Stage 3 of `docs/superpowers/plans/2026-10-24-live-birth-roadmap.md` (request #11).
- The user's additions on 2026-10-25: a `deathAfterTicks` setting so animals can die of old age, and black hearts over old animals, much fainter and rarer than the love and pregnancy hearts.

## Decisions (beyond the outline)

- **Old age can kill.** This replaces the outline's "nothing ever dies of old age". `deathAfterTicks` is per species, defaults to 60 days (1,440,000 ticks), and 0 turns it off. Each animal's death tick is spread ±`deathSpread` (0.1) by a value taken from its UUID, so litter-mates don't all drop on the same tick. Nothing extra is stored.
- **Unknown birth means it never ages.** Old worlds, wild adults and anything with `bornTick` -1 stay adult forever, so no existing animal suddenly dies.
- **A baby without traits gets `bornTick` = now when it first joins.** That covers vanilla breeding when pregnancy is off, and spawn-egg babies. Wild adults still get -1.
- **Ages count from birth.** Young lasts until `bornTick + growthTicks + youngTicks`. Old starts at `bornTick + oldAfterTicks`. Death comes at `bornTick + deathAfterTicks × spread`. A fed baby that grows up early just stays young a little longer, so there's no need for a "grown at" tick.
- **0 turns a step off.** `oldAfterTicks` 0 means never old; `deathAfterTicks` 0 means never dies. `youngTicks` 0 means no young stage.
- **Death uses its own damage type,** `barnyard_romance:old_age`. It's tagged to bypass armor, invulnerability, effects, resistance, enchantments, shields and wolf armor, with no knockback and no anger. Tamed pets tell their owner "%1$s died of old age". If the damage type is missing, `kill` is used instead.
- **Product yield is left for Stage 5.** No product counts are scaled yet, so `oldYieldFactor` comes in with Stage 5, which also adds the sex and size factors.
- **Fertility recovery is split at the old start.** Ticks before it recover at the full rate, ticks after it at `oldFertilityFactor` (0.5).
- **Old hearts:** client side only, about once every 12 seconds on average (1 in 240 per tick), one heart at a time. They're dark grey, 35% alpha, slow to rise, and smaller than pregnancy hearts. They never show while the animal is pregnant or in love.
- **Inspector:** the life word is now Baby, Young, Adult or Old. Young animals show "Too young" under breeding. Fertility shows for adult and old females.

## Global Constraints

- About 80–90% of the logic lives in `common`. Loader code is registration, attachments, events and networking only.
- The server is authoritative. The client gets `bornTick` through the existing attachment `STREAM_CODEC`.
- Every hook catches `RuntimeException`, logs it, and falls back to vanilla behavior.
- Every new number has a config key and a default. Bad values clamp and never throw.
- Saved data stays backward compatible: `optionalFieldOf("bornTick", -1L)`.
- Unit tests cover pure logic only.
- Build with JDK 25 using `./gradlew build`. Game tests: `./gradlew :fabric:runGametest` and `./gradlew :neoforge:runGameTestServer`.
- Work on branch `live-birth-stage-3` from `main`.

## Review Focus

1. **Old saves never age.** Traits without `bornTick` load as -1, and those animals stay adult and never die. Pinned by `unknownBirthIsAlwaysAdult` and `unknownBirthNeverDies` (`AgingTest`), and `oldTraitsHaveNoBirthTick` (`AnimalTraitsTest`).
2. **Off switches.** `oldAfterTicks` 0 and `deathAfterTicks` 0 never trigger. Pinned by `zeroTurnsOldAgeAndDeathOff`.
3. **The spread stays in bounds.** Spread values 0 and 1 give exactly ±`deathSpread`. Pinned by `deathSpreadsBothWays`.
4. **Fertility across the old start.** Pinned by `oldAgeHalvesRecoveryFromTheOldStart` (`FertilityTest`).
5. **Young animals can't fall in love,** whether fed or put in love directly. Pinned by the game test `young_animals_do_not_fall_in_love`.

---

### Task 1: Age rules, settings and tuning

**Files:**
- Create: `common/.../age/AgeStage.java`, `age/AgeSettings.java`, `age/Aging.java`
- Modify: `config/Tuning.java` (`oldFertilityFactor` 0.5, `deathSpread` 0.1), `config/BarnyardConfig.java` (global and per-species `youngTicks`, `oldAfterTicks`, `deathAfterTicks`; `ageFor(speciesId)`)
- Test: create `age/AgingTest.java`; extend `config/TuningTest.java` and `config/BarnyardConfigTest.java`

**Interfaces:**
- `enum AgeStage { BABY, YOUNG, ADULT, OLD }` with `String label()` ("Baby", ...) and `boolean canBreed()` (ADULT, OLD).
- `record AgeSettings(long youngTicks, long oldAfterTicks, long deathAfterTicks)` with `DEFAULTS = (24_000, 720_000, 1_440_000)`.
- `Aging.stage(boolean baby, long bornTick, int growthTicks, long now, AgeSettings age)`
- `Aging.oldFrom(long bornTick, AgeSettings age)`, which returns `Long.MAX_VALUE` when unknown or off.
- `Aging.deathAt(long bornTick, AgeSettings age, double spread, double spreadRandom)`, which returns `Long.MAX_VALUE` when unknown or off.
- `Aging.spreadRandom(long uuidBits)`, a 0–1 value from the UUID's bits.
- Config ticks clamp to 0–24,000,000 (1,000 days).

- [ ] Write the failing tests, then the code, then run `./gradlew :common:test`.

### Task 2: The birth tick, and fertility that slows with age

**Files:**
- Modify: `sex/AnimalTraits.java` and `sex/AnimalTraitCodecs.java` (append `long bornTick`, plus `withBornTick`)
- Modify: `sex/AnimalSex.java` (a fresh baby gets `bornTick` = now)
- Modify: `pregnancy/PregnancyController.java` (newborns get `bornTick` = now; fertility uses the mother's age)
- Modify: `pregnancy/Fertility.java` (`current(fertility, since, now, oldFrom)`, plus `current` and `wornOut` taking `AgeSettings`)
- Test: `AnimalTraitsTest`, `FertilityTest`

- [ ] Write the failing tests, then the code, then run `./gradlew :common:test`.

### Task 3: Young can't breed, old dies

**Files:**
- Create: `age/AgeController.java` (`stage(Animal)`, `allowsLove(Animal)`, `serverTick(Animal, ServerLevel)`)
- Modify: `familiarity/FamiliarityActions.java` (`allowsLove`, and the feed-to-love path, check `AgeController`)
- Modify: `mixin/AnimalBreedingMixin.java` (calls `AgeController.serverTick`)
- Create: `data/barnyard_romance/damage_type/old_age.json`, the `data/minecraft/tags/damage_type/*.json` entries, and the `en_us.json` death message
- Modify: `gametest/BreedingGameTests.java` and both loader classes. New tests: `young_animals_do_not_fall_in_love`, `old_animals_die_of_old_age` and `animals_of_unknown_age_never_die`. `a_birth_costs_the_mother_fertility` also checks the piglets' `bornTick`.

- [ ] Write the code and game tests, then run `./gradlew build`, `:fabric:runGametest` and `:neoforge:runGameTestServer`.

### Task 4: The inspector, info text and old hearts

**Files:**
- Modify: `inspect/InspectorSheet.java` (`TOO_YOUNG`; `breeding(...)` takes `young`; `showBreeding` and `showFertility` by stage)
- Modify: `client/InspectorScreen.java`, `command/AnimalInfoText.java` (takes an `AgeStage` in place of `boolean baby`), `item/HerdStick.java`
- Create: `client/OldHeartParticle.java`, `particles/old_heart.json`, `textures/particle/old_heart.png`
- Modify: `client/BarnyardParticles.java`, `client/AnimalHearts.java`, and the particle registration in both loaders
- Test: `InspectorSheetTest`, `AnimalInfoTextTest`, `HoverTooltipTest`

- [ ] Write the failing tests, then the code, then run `./gradlew build` and the Fabric client game test (the inspector screenshot).

### Task 5: Docs

- [ ] `docs/DESIGN.md`: age stages, death, the config keys and the tuning keys.
- [ ] `docs/ROADMAP.md`: tick off Stage 3, and move "product yield" to Stage 5.
- [ ] The roadmap plan: note that Stage 3 replaced "nothing dies of old age".
