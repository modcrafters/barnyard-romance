# Live-Birth Animals: Staged Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the breeding cycle for animals that give birth: tuned pregnancies and litters, inherited traits, food, products, age, visuals, a herd book, and translations. Chickens and other egg layers come later.

**Architecture:**
- Each stage ships as its own branch and commits.
- Stage 1 is detailed below and lands as **one commit**.
- Stages 2–8 are outlined with their decisions locked in. Each one gets its own detailed plan (same format as Stage 1) when it starts, because each depends on what the stage before it settled.
- Pure rules stay in `common` with unit tests. Loaders only add glue.

**Tech Stack:**
- Minecraft 26.3, Java 25.
- MultiLoader (common / fabric / neoforge), Gradle 9.7.1.
- Mixins with `defaultRequire: 1`. JUnit 5, with no Minecraft classes on the test classpath.

**Spec:** `docs/DESIGN.md` and `docs/ROADMAP.md`, plus the user's 14 answers from 2026-10-24 (listed under "Requests" below).

## Requests (2026-10-24)

| # | Request | Stage |
|---|---|---|
| 1 | Pregnancy length and litter size are configurable as min–max; a heritable, improvable **weight** trait chooses where in the range an animal lands | 1 (ranges), 2 (weight) |
| 2 | Data-driven good/bad foods; superfood recipes that speed pregnancy or improve the baby; superfoods only when Pam's HarvestCraft is present | 4 |
| 3 | A pregnant female gets wider, not taller or longer | 6 |
| 4 | If a pregnant animal dies, the pregnancy is simply lost | 1 (docs only) |
| 5 | Fertility and a "worn out" cooldown | 2 |
| 6 | Sync the breeding cooldown and love mode so the inspector is accurate | 1 |
| 7 | Configurable foods for healing and stats, restoring vanilla horse healing and temper (part of #2) | 4 |
| 8 | Fire NeoForge `BabyEntitySpawnEvent` | 1 |
| 9 | Milk only after a birth, drying up after a few days unmilked once the young are grown; males drop more; drops grow with size | 5 |
| 10 | Every trait is the parents' average plus a chance to improve | 2 |
| 11 | Age stages: baby, young, adult, old | 3 |
| 12 | Herd book with lineage | 7 |
| 13 | Every tuning number lives in config | 1, then each stage adds its own |
| 14 | Translations | 8 |

## Global Constraints

- About 80–90% of the logic lives in `common`. Loader code is registration, attachments, events and networking only.
- The server is authoritative. Clients get only what they draw, through attachment `STREAM_CODEC`s.
- Unsupported or custom-breeding modded animals fail safe: the feature is off and nothing crashes. Every hook catches `RuntimeException`, logs it once, and falls back to vanilla behavior.
- Every new number has a config key and a default equal to today's behavior. Bad or missing values fall back to that default and never throw at runtime.
- Saved data stays backward compatible. New codec fields are `optionalFieldOf` with defaults, and old worlds load unchanged.
- Unit tests cover pure logic only. Tests may not touch `CompoundTag`, `Entity`, or any other Minecraft class.
- Build with JDK 25 (`C:\Program Files\Java\jdk-25.0.2`). Run `./gradlew build` from the repo root. Expect both loader jars and all tests passing.
- End every commit message with `Co-Authored-By: Claude Opus 5.5 (1M context) <noreply@anthropic.com>`.

## Review Focus

1. **Old config files.** A `barnyard_romance.json` written before Stage 1 (with `litterSize` and no `litterMin`/`litterMax`) must still load with the same behavior. Tested in Task 2 (`legacyLitterSizeMeansAFixedLitter`).
2. **Inverted or out-of-range ranges.** A config with `litterMin: 5, litterMax: 2`, or with negative ticks, must load as a sane range, not crash. Tested in Task 2 (`invertedRangesAreSwapped`).
3. **A user value beats a preset.** A species key the user set wins over the built-in preset. A key the user left out falls back to the preset, then to the global value. Tested in Task 2 (`userValueBeatsPresetBeatsGlobal`).
4. **Stale love and cooldown on the client.** When the client clock passes `loveUntil` or `cooldownUntil`, the inspector shows "Ready" again with no new sync. Tested in Task 4 (`breedingStatusExpiresWithTheClock`).
5. **A cancelled NeoForge event.** A listener that cancels the event, or swaps the child for `null`, must leave no pregnancy behind. Both parents must then get vanilla's 6000-tick cooldown with love reset. Verified manually in Task 3 Step 3, since it needs the loader.

---

## Stage 1: Quick wins (one commit)

Branch: `live-birth-stage-1`, from `familiarity-and-inspector`.

### Task 1: Tuning numbers move into config

**Files:**
- Create: `common/src/main/java/net/modcrafters/minecraft/barnyardromance/config/Tuning.java`
- Modify: `config/BarnyardConfig.java` (parse/write a `"tuning"` object; accessor `tuning()`)
- Modify: `familiarity/Familiarity.java`, `sex/Growth.java`, `sex/OffspringScale.java` (constants become reads from `BarnyardConfig.current().tuning()`)
- Modify: every caller of `Familiarity.GAIN/THRESHOLD/DAY_TICKS`, `Growth.FAST/SLOW/DEFAULT` and `OffspringScale.*` (in `InspectorSheet`, `Pregnancy`, `PregnancyCodecs`, `PregnancyController`, `AnimalTraits`, `AnimalTraitCodecs`, `MeatDrop`, and the tests)
- Test: `common/src/test/java/.../config/TuningTest.java`

**Interfaces:**
- Produces:
  - `public record Tuning(double familiarityGain, double familiarityThreshold, long feedCooldownTicks, int growthFastTicks, int growthSlowTicks, int growthWildTicks, double improveBaseChance, double improveNutritionChance, double improveStep)`
  - `public static final Tuning Tuning.DEFAULTS = new Tuning(0.2, 0.3, 24_000L, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25)`
  - `static Tuning Tuning.from(JsonObject)`, which clamps every value:
    - chances and the gain to 0–1
    - ticks to at least 1
    - if `growthFastTicks > growthSlowTicks`, the two are swapped
    - `growthWildTicks` is clamped into `[fast, slow]`
  - `JsonObject Tuning.toJson()`
  - `BarnyardConfig.tuning()`
  - `Familiarity.gain()`, `Familiarity.threshold()`, `Familiarity.dayTicks()`
  - `Growth.fast()`, `Growth.slow()`, `Growth.wild()`
- Keep the `MeatDrop` vanilla constant as-is. It describes vanilla, not tuning.

- [ ] **Step 1: Write failing tests in `TuningTest`:**
  - `defaultsMatchTodaysBehaviour`: `BarnyardConfig.defaults().tuning()` equals `Tuning.DEFAULTS`.
  - `parsedTuningOverridesDefaults`: parsing `{"tuning":{"familiarityThreshold":0.5}}` gives a threshold of 0.5 and leaves the gain at 0.2.
  - `badTuningIsClamped`: parsing `{"tuning":{"familiarityGain":7,"growthFastTicks":40000,"growthSlowTicks":10000}}` gives:
    - gain 1.0
    - fast 10000, slow 40000
    - wild 24000
  - `tuningRoundTripsThroughJson`: `parse(defaults().toJson()).tuning()` equals `DEFAULTS`.
- [ ] **Step 2:** Run `./gradlew :common:test --tests '*TuningTest'`. Expected: FAIL (`Tuning` is not defined).
- [ ] **Step 3:** Implement `Tuning` and wire it into `BarnyardConfig` (`parse`, `writeJson`, `defaults`). Replace the constants with the accessors above. The existing `FamiliarityTest` and `GrowthTest` switch from the constants to the accessors, and their expected values do not change.
- [ ] **Step 4:** Run `./gradlew :common:test`. Expected: all tests pass, including the old ones unchanged in meaning.

### Task 2: Litter and pregnancy ranges, with vanilla presets

**Files:**
- Create: `config/SpeciesPresets.java` (built-in defaults per vanilla mammal)
- Create: `pregnancy/LitterRoll.java` (pure)
- Modify: `config/BarnyardConfig.java`:
  - `SpeciesSettings` gains `litterMin`, `litterMax`, `pregnancyTicksMin` and `pregnancyTicksMax`.
  - The global config gains the same four keys.
  - An old `litterSize` / `pregnancyTicks` key sets both min and max.
- Modify: `pregnancy/PregnancyController.java` (roll both values at conception)
- Test: `config/BarnyardConfigTest.java` (extend it, or create it if missing), `pregnancy/LitterRollTest.java`

**Interfaces:**
- Produces:
  - `public record Range(long min, long max)` in `config`, with `static Range of(long a, long b)` that swaps an inverted pair.
  - `BarnyardConfig.litterRange(String speciesId) -> Range`, clamped to `[1, 8]`.
  - `BarnyardConfig.pregnancyRange(String speciesId) -> Range`, clamped to `[1, 480_000]`.
  - `LitterRoll.pick(Range range, double weight, double random) -> long`.
    - For Stage 1, every caller passes `weight = 0.5`. Stage 2 passes the real trait.
    - Formula: `min + floor(clamp(random + (weight - 0.5) * 0.5, 0, 0.999999) * (max - min + 1))`. Weight 0.5 is neutral.
  - `LitterRoll.pickTicks(Range range, double weight, double random) -> long`. Same `x`, but a higher weight means a shorter pregnancy: `max - floor(x * (max - min + 1))`.
- Lookup order for each key: the user's species entry, then `SpeciesPresets`, then the user's global value.
- **Presets** (ticks; 24,000 = one Minecraft day):

  | species | pregnancy | litter |
  |---|---|---|
  | cow, mooshroom | 60,000–72,000 | 1–1 |
  | horse, donkey | 72,000–96,000 | 1–1 |
  | llama, trader_llama | 60,000–72,000 | 1–1 |
  | camel | 72,000–96,000 | 1–1 |
  | sheep, goat | 36,000–48,000 | 1–2 |
  | pig | 30,000–36,000 | 2–4 |
  | rabbit | 12,000–18,000 | 2–5 |
  | wolf, fox, cat, ocelot | 24,000–30,000 | 1–3 |
  | panda, polar_bear | 48,000–60,000 | 1–2 |
  | hoglin | 30,000–36,000 | 2–3 |

  Species not listed keep the global values (today: 24,000 and 1).

- [ ] **Step 1: Write failing tests:**
  - `legacyLitterSizeMeansAFixedLitter`: parsing `{"litterSize":3}` gives `litterRange("x:y")` equal to `Range(3,3)`.
  - `invertedRangesAreSwapped`: parsing `{"species":{"minecraft:pig":{"litterMin":5,"litterMax":2}}}` gives `Range(2,5)`.
  - `userValueBeatsPresetBeatsGlobal`:
    - With `{"species":{"minecraft:pig":{"litterMax":6}}}`, pig gives `Range(2,6)`. The min comes from the preset.
    - `minecraft:cow` gives `Range(1,1)` for litter and `Range(60000,72000)` for pregnancy.
    - `mod:unknown` gives `Range(1,1)` and `Range(24000,24000)`.
  - `LitterRollTest`, `rollStaysInRangeAndWeightPushesUp`:
    - `pick(Range(2,5), 0.5, 0.0) == 2`
    - `pick(Range(2,5), 0.5, 0.999) == 5`
    - `pick(Range(2,5), 1.0, 0.5) == 5`
    - `pick(Range(2,5), 0.0, 0.5) == 3`
    - `pick(Range(3,3), x, y) == 3` for any x and y
  - `LitterRollTest`, `heavierMothersCarryShorter`: `pickTicks(Range(30000,36000), 1.0, 0.999) == 30000`, and `pickTicks(Range(30000,36000), 0.0, 0.0) == 36000`.
- [ ] **Step 2:** Run `./gradlew :common:test`. Expected: the new tests FAIL.
- [ ] **Step 3:** Implement `Range`, `SpeciesPresets`, `LitterRoll` and the config changes. `toJson` writes min/max keys. In `PregnancyController.startOrRefuse`, roll the litter size and pregnancy ticks with `level.getRandom().nextDouble()` and `weight = 0.5`. Remove the old `litterSize(String)` and `pregnancyTicks(String)`.
- [ ] **Step 4:** Run `./gradlew :common:test`. Expected: PASS.

### Task 3: NeoForge `BabyEntitySpawnEvent` at conception

Vanilla fires this when the baby is made. For us that is conception, while both parents are present.

**Files:**
- Modify: `platform/services/IPlatformHelper.java`
- Modify: `pregnancy/Litter.java` (`conceive`)
- Modify: `pregnancy/PregnancyController.java` (a cancelled event refuses the pregnancy)
- Modify: `neoforge/src/main/java/.../platform/NeoForgePlatformHelper.java` (or the existing class that implements `IPlatformHelper`)

**Interfaces:**
- Produces: `default @Nullable AgeableMob onBabySpawn(Animal parentA, Animal parentB, @Nullable AgeableMob proposed) { return proposed; }` in `IPlatformHelper`. Fabric keeps the default.
- On NeoForge:
  1. Post `new BabyEntitySpawnEvent(parentA, parentB, proposed)` on `NeoForge.EVENT_BUS`.
  2. Return `null` if the event was cancelled, otherwise `event.getChild()`.
- In `Litter.conceive`:
  - Call this hook only for the first baby.
  - If it returns `null`, `conceive` returns `Optional.empty()`. The signature changes to `Optional<List<CompoundTag>> conceive(...)`.
  - If it returns a different mob, that mob's NBT becomes the first entry.
- When `PregnancyController` gets `Optional.empty()`, it copies vanilla's cancel path: `self.setAge(6000); partner.setAge(6000); self.resetLove(); partner.resetLove(); return true;`. No pregnancy and no XP.

- [ ] **Step 1:** Implement the hook and its callers. This is not unit-testable because it is Minecraft-bound.
- [ ] **Step 2:** Run `./gradlew build`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 3 (manual, NeoForge client):** Add a temporary dev-only listener that cancels every event, then breed two cows. Expected:
  - The hearts stop.
  - No pregnancy shows in the inspector.
  - The cows can't breed again for 5 minutes.

  Remove the listener before committing.

### Task 4: Love mode and cooldown synced to the inspector

**Files:**
- Modify: `sex/AnimalTraits.java` and `sex/AnimalTraitCodecs.java`:
  - Add `long loveUntil` and `long cooldownUntil`, defaulting to `-1L` in `MAP_CODEC` and both written in `STREAM_CODEC`.
  - Existing constructors pass `-1L`.
  - Add `withLove(long)` and `withCooldown(long)`.
- Create: `mixin/AnimalLoveMixin.java`, registered in `barnyard_romance.mixins.json`:
  - `Animal.setInLove(Player)` TAIL writes `loveUntil = gameTime + getInLoveTime()`.
  - `Animal.resetLove()` TAIL writes `loveUntil = -1`.
  - Both run on the server only and only when traits exist. Each catches and logs `RuntimeException`.
- Modify: `pregnancy/PregnancyController.java`. After `finalizeSpawnChildFromBreeding`, write `cooldownUntil = gameTime + animal.getAge()` on both parents, plus `loveUntil = -1`.
- Modify: `inspect/InspectorSheet.java` and `client/InspectorScreen.java`.
- Test: `inspect/InspectorSheetTest.java`

**Interfaces:**
- Produces: `InspectorSheet.breeding(boolean needsTaming, boolean tamed, double familiarity, long loveUntil, long cooldownUntil, long now) -> String`. In priority order it returns:
  1. `NOT_TAMED`
  2. `"Needs 30% familiarity"`, built from `Familiarity.threshold()`
  3. `"Resting " + Growth.clock(cooldownUntil - now)` when `cooldownUntil > now`
  4. `"In love"` when `loveUntil > now`
  5. `READY`

  `breedingReady` is true only for the "In love" and `READY` results.
- The client passes `animal.level().getGameTime()` as `now`.

- [ ] **Step 1: Write failing tests:**
  - `breedingShowsRestAndLove`:
    - `breeding(false,false,1.0,-1,1200,0)` returns `"Resting 1m 0s"`.
    - `breeding(false,false,1.0,600,-1,0)` returns `"In love"`.
    - A pair that is both resting and in love shows "Resting".
  - `breedingStatusExpiresWithTheClock`: `breeding(false,false,1.0,600,1200,1200)` returns `READY`.
  - Update the five existing `breedingSaysWhatIsMissing` assertions to the new signature with `-1,-1,0`.
- [ ] **Step 2:** Run `./gradlew :common:test --tests '*InspectorSheetTest'`. Expected: FAIL (wrong arity).
- [ ] **Step 3:** Implement the fields, the mixin, the controller writes, and the sheet and screen changes.
- [ ] **Step 4:** Run `./gradlew build`. Expected: everything passes.

### Task 5: Docs and the single commit

**Files:** `docs/DESIGN.md`, `docs/ROADMAP.md`

- [ ] **Step 1: Update `DESIGN.md`:**
  - §4: litter and pregnancy ranges and presets. The weight trait is marked as Stage 2.
  - §4 edge cases: "Pregnant animal dies: the pregnancy is lost; no birth, no message."
  - §8: the `tuning` block and the lookup order (species, then preset, then global).
  - §3: the NeoForge event fires at conception.
- [ ] **Step 2: Rewrite the open items in `ROADMAP.md`** as Stages 2–8 of this plan (one checkbox per bullet below), and tick the Stage 1 items.
- [ ] **Step 3:** Run `./gradlew build`. Expected: BUILD SUCCESSFUL, all tests pass.
- [ ] **Step 4:** Smoke-test in game on Fabric. Expected:
  - A pig's litter is 2–4.
  - A cow's pregnancy lasts 2.5–3 days.
  - The inspector shows "In love" after a feed and "Resting …" after a conception.
- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "Move tuning into config, add litter and pregnancy ranges, sync love and rest, fire the NeoForge baby event."
```

---

## Stage 2: Inherited traits, weight, fertility (#1, #10, #5)

- **New traits on `AnimalTraits`:**
  - `weight` (0–1). A heavier mother picks higher in the litter range and shorter in the pregnancy range, through `LitterRoll`'s weight argument.
  - `fertility` (0–1, starts at 1).
- **One inheritance rule for every trait:** `Inheritance.child(motherPct, fatherPct, random, nutrition, tuning)`.
  - It averages the two parents' percentages.
  - With chance `improveBaseChance + improveNutritionChance * nutrition`, it adds `improveStep`.
  - It applies a small configurable random spread (`inheritSpread`, default 0.05).
  - It clamps the result to 0–1.
  - `Growth.child` and `OffspringScale.childScale` are folded into it.
  - Scale, growth, weight and fertility all go through it. Stage 5 adds yield the same way.
- **Traits live in the litter.** The father's trait percentages are stored in `Pregnancy` at conception, so the baby's traits don't depend on the father being alive at birth.
- **Fertility ("worn out"):**
  - Each birth costs `fertilityCostPerBirth` (0.25).
  - Fertility recovers `fertilityRecoveryPerDay` (0.1).
  - Below `fertilityMinToConceive` (0.25), mating gives hearts and a cooldown but no pregnancy.
  - All values are in `tuning`.
- **Shown in the inspector:** a weight meter, and a fertility meter for adults.
- **Old worlds:**
  - Missing `weight` defaults to 0.5.
  - Missing `fertility` defaults to 1.0.

## Stage 3: Age stages (#11)

- **Birth tick:** a `bornTick` trait. For old worlds and wild spawns it is unknown (-1), and those animals count as adults.
- **The four stages:**
  - Baby is vanilla's baby state.
  - Young lasts for `youngTicks` after vanilla adulthood (per species, default 1 day). A young animal can't breed yet.
  - Adult comes next.
  - Old starts after `oldAfterTicks` (per species, default 30 days).
- **What "old" changes:**
  - Fertility recovery is multiplied by `oldFertilityFactor` (0.5).
  - Product yield is multiplied by `oldYieldFactor` (0.75).
  - Nothing ever dies of old age.
- **Shown in the inspector:** the stage word replaces "Adult" or "Baby".

## Stage 4: Data-driven foods and superfoods (#2, #7)

- **Food files:** a datapack registry `data/<ns>/barnyard_romance/foods/<name>.json`. Each file holds:
  - `species`: a list or tag.
  - `items`: an item tag or ids.
  - `effects`, each optional:
    - `familiarity`
    - `heal`
    - `horseTemper`
    - `pregnancySpeed` (a multiplier for N ticks)
    - `pregnancyProgress` (a one-time bonus)
    - `babyImprove` (adds to the nutrition term of `Inheritance`)
    - `bad` (slows the pregnancy)
- **Vanilla defaults ship in the mod's own data.** The horse family gets back vanilla's healing and temper values from `AbstractHorse.handleEating`, which closes the bypass.
- **Superfoods:**
  - New items, e.g. a "breeder's mash" and a "golden bran".
  - Their recipes, and the food entries that use them, sit behind a mod-loaded condition: Fabric `fabric:load_conditions` and NeoForge `neoforge:conditions`, keyed to Pam's HarvestCraft 2 mod ids.
  - Check the exact ids when this stage starts.
  - Without Pam's, the items exist but have no recipe, so they're creative/command only.
- **Fail safe:** if a food file fails to parse, that file is skipped and logged. The rest still load.

## Stage 5: Products (#9)

- **Lactation:**
  - A birth sets `milkUntil = birth + lactationTicks`.
  - Each milking pushes `milkUntil` forward by `milkKeepsFlowingTicks` (default 3 days).
  - While the youngest baby is still a baby, milk never dries up.
  - After that, three unmilked days end the milk.
  - Males never give milk.
  - A cow with no milk refuses the bucket, with a message on the action bar.
- **Drops:**
  - The existing `MeatDrop` path generalizes to every animal loot drop (meat, leather, wool, rabbit hide).
  - Count = vanilla × the sex multiplier (`maleYield` 1.25, `femaleYield` 1.0) × the size factor (`scale ^ yieldScaleExponent`, default 1.0) × the age factor (Stage 3).
  - Rounding is random, so the average is exact.
- **Inspector:** shows "Milk: 2d left" for lactating females.

## Stage 6: Pregnancy belly (#3)

- **Client-only render tweak:** a mixin on `LivingEntityRenderer`'s pose scale step, plus a render-state field filled from the synced pregnancy progress.
- **Width only:** the model is scaled on its local X axis only. Width is `1 + bellyMaxWiden * smoothstep(bellyStart, 1, progress)`, defaulting to 0.15 and 0.4.
  - Height and length stay the same.
  - The hitbox does not change.
- **Settings:** per species, with a client config toggle.
- **First pass:** the whole model widens, including the head.
- **Possible follow-up:** widening only the body part per model, if the head looks wrong.

## Stage 7: Herd book and lineage (#12)

- **Lineage traits:** `motherId`, `fatherId` (UUIDs) and their last known names, stored at birth.
- **Herd book item:**
  - Using it on an animal registers that animal in the book. The book stores UUID, species, name, sex and birth tick, and is saved on the item.
  - Opening the book lists the registered animals.
  - Selecting one shows its traits and a two-generation family tree from the stored lineage.
- **Brainstorm first:** run `superpowers:brainstorming` with the user before this stage's detailed plan. Open questions:
  - Is the book per player or per item?
  - Should it be shareable?
  - Does it show live data or only what was recorded?

## Stage 8: Translations (#14)

- Every `Component.literal` becomes `Component.translatable` with keys in `en_us.json`.
- `InspectorSheet` and the other pure text helpers return `Text(key, args...)` records instead of `String`. Their tests check the keys and arguments.
- The client resolves them with `Component.translatable(key, args)`.

## Later (unchanged from the roadmap)

- Per-sex textures, layers and models (Milestone 5).
- Seasons integration.
- Territorial males.
- Compatibility presets for popular animal mods.
- Egg layers (chickens and friends).
