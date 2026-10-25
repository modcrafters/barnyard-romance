# Stage 2: Inherited Traits, Weight, Fertility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every inherited trait goes through one rule. A heavier mother has bigger litters and shorter pregnancies. Births wear a mother out until she recovers.

**Architecture:**
- A pure `Inheritance.child` replaces `Growth.child` and `OffspringScale.childScale`.
- `weight` is a new inherited trait on `AnimalTraits`, fed into the existing `LitterRoll` weight argument.
- `fertility` is a new state on `AnimalTraits`. It is stored as a value plus the tick it was set, and recovery is worked out from the clock, like `loveUntil`. Nothing ticks, and the client shows it without new syncs.
- The inspector gets a weight row, plus a fertility row for adult females.

**Tech Stack:** Minecraft 26.3, Java 25, MultiLoader (common / fabric / neoforge), JUnit 5, game tests on both loaders.

**Spec:**
- Stage 2 of `docs/superpowers/plans/2026-10-24-live-birth-roadmap.md`.
- Requests #1, #5 and #10 in that file.
- `docs/ROADMAP.md`.

## Decisions (beyond the outline)

- **Weight** is fixed at birth. Wild spawns and old worlds get 0.5, which keeps today's litters and pregnancies unchanged. Breeding can move it up through improvement, or up and down through the spread.
- **Fertility is a state, not an inherited stat.** The outline put fertility through `Inheritance`. But inheriting a worn-out value would punish a baby for its mother's past litters, and it would recover within days anyway. So newborns start at 1.0, and `Inheritance` covers scale, growth and weight (Stage 5 adds yield).
- **Only mothers wear out.** A birth costs the mother; fathers never lose fertility. The fertility meter shows for adult females only.
- **A worn-out mating** shows love hearts and starts the same rest as a cancelled NeoForge event: both parents get age 6000, love is reset, and the rest is mirrored. There is no pregnancy and no XP. The inspector shows "Worn out".
- **One day is 24,000 ticks** for fertility recovery (`Fertility.DAY_TICKS`), matching the pregnancy presets.

## Global Constraints

- About 80–90% of the logic lives in `common`. Loader code is registration, attachments, events and networking only.
- The server is authoritative. Clients get only what they draw, through attachment `STREAM_CODEC`s.
- Every hook catches `RuntimeException`, logs it, and falls back to vanilla behavior.
- Every new number has a `tuning` key and a default. Bad or missing values fall back to the default (unit values clamp to 0–1) and never throw.
- Saved data stays backward compatible. New codec fields are `optionalFieldOf` with defaults: weight 0.5, fertility 1.0, fertilityTick -1, fatherWeight 0.5.
- Unit tests cover pure logic only, with no `CompoundTag`, `Entity` or other Minecraft class.
- Build with JDK 25 (`C:\Program Files\Java\jdk-25.0.2`) using `./gradlew build` from the repo root. Each new stage adds game tests: `./gradlew :fabric:runGametest` and `./gradlew :neoforge:runGameTestServer`.
- Work on branch `live-birth-stage-2` from `main`. End every commit message with `Co-Authored-By: Claude Opus 5.5 (1M context) <noreply@anthropic.com>`.

## Review Focus

1. **Old saves.** Traits saved before Stage 2 load with weight 0.5, fertility 1.0 and fertilityTick -1. A pregnancy saved before Stage 2 loads with fatherWeight 0.5. Pinned by `oldTraitsDefaultToAverageWeightAndFullFertility` (Task 2) and `oldPregnanciesDefaultToAnAverageFather` (Task 2). The codec defaults are checked by reading them.
2. **Long absences and odd clocks.** A mother unloaded for weeks comes back at exactly 1.0, never above. A `now` before the stored tick never lowers fertility. Pinned by `recoveryCapsAtFullAndIgnoresAClockThatRunsBackwards` (Task 3).
3. **Either partner can start the mating.** The worn-out check finds the mother whether `self` is the male or the female. Pinned by the game test `worn_out_mothers_rest_without_conceiving`, where the father calls `spawnChildFromBreeding` (Task 3).
4. **The spread at the edges.** A spread on a 0.0 or 1.0 parent average never leaves 0–1. Pinned by `spreadNeverLeavesTheUnitRange` (Task 1).
5. **Bad fertility tuning.** Negative costs, a recovery above 1, or a minimum above 1 must load clamped to 0–1. Pinned by extending `badTuningIsClamped` (Task 1).

---

### Task 1: One inheritance rule, and the Stage 2 tuning keys

**Files:**
- Create: `common/src/main/java/net/modcrafters/minecraft/barnyardromance/sex/Inheritance.java`
- Modify: `common/.../config/Tuning.java`
- Modify: `common/.../sex/Growth.java`, deleting `child`
- Delete: `common/.../sex/OffspringScale.java`
- Modify: `common/.../pregnancy/PregnancyController.java` (`rememberParents`)
- Modify: `docs/DESIGN.md` (tuning keys)
- Test: create `common/src/test/.../sex/InheritanceTest.java`. Modify `GrowthTest.java` and `config/TuningTest.java`. Delete `OffspringScaleTest.java`.

**Interfaces:**
- Produces:
  - `Tuning` gains four fields, appended in this order: `double inheritSpread` (0.05), `double fertilityCostPerBirth` (0.25), `double fertilityRecoveryPerDay` (0.1), `double fertilityMinToConceive` (0.25). All are parsed with `unit(...)` and written by `toJson`.
  - `public static double Inheritance.child(double mother, double father, double improveRandom, double spreadRandom, double nutrition)` returns a 0–1 percentage. It reads `BarnyardConfig.current().tuning()`, like `Growth` does.
- The algorithm, which the tests alone don't fix:

  ```
  average = (clamp01(mother) + clamp01(father)) / 2
  chance  = improveBaseChance + improveNutritionChance * clamp01(nutrition)
  next    = improveRandom < chance ? average + improveStep : average
  next   += (clamp01(spreadRandom) * 2 - 1) * inheritSpread   // 0.5 is neutral
  return clamp01(next)
  ```

- [ ] **Step 1: Write the failing tests.**
  - `InheritanceTest`:
    - `anUnluckyBabyMatchesTheParentsAverage`: `child(0.0, 1.0, 0.99, 0.5, 0.0) == 0.5`.
    - `aLuckyBabyStepsUp`: `child(0.4, 0.4, 0.0, 0.5, 0.0) == 0.65`, and `child(0.0, 0.0, 0.0, 0.5, 0.0) == 0.25`.
    - `goodFoodMakesAnAverageRollCountAsLucky`: `child(0.0, 0.5, 0.5, 0.5, 0.0) == 0.25`, and `child(0.0, 0.5, 0.5, 0.5, 1.0) == 0.5`.
    - `theSpreadNudgesBothWays`: `child(0.5, 0.5, 0.99, 0.0, 0.0) == 0.45`, and `child(0.5, 0.5, 0.99, 1.0, 0.0) == 0.55`.
    - `spreadNeverLeavesTheUnitRange`: `child(1.0, 1.0, 0.0, 1.0, 1.0) == 1.0`, and `child(0.0, 0.0, 0.99, 0.0, 0.0) == 0.0`.
    - Each assertion uses a tolerance of 1e-9.
  - `TuningTest`:
    - `defaultsMatchTodaysBehaviour` expects `new Tuning(0.2, 0.3, 24_000L, 15_000, 30_000, 24_000, 0.35, 0.45, 0.25, 0.05, 0.25, 0.1, 0.25)`.
    - `badTuningIsClamped` adds `"fertilityCostPerBirth": -1, "fertilityRecoveryPerDay": 4, "fertilityMinToConceive": 2` and expects 0.0, 1.0 and 1.0.
  - `GrowthTest`: delete `aBabyAveragesItsParentsAndCanGrowFaster`; `InheritanceTest` covers it. Delete `OffspringScaleTest`.
- [ ] **Step 2:** Run `./gradlew :common:test`. Expected: FAIL to compile, because `Inheritance` and the new `Tuning` fields don't exist.
- [ ] **Step 3: Implement `Tuning`'s new fields and `Inheritance.child`.**
  - In `rememberParents`, the conversions stay the same: scale goes through `ScaleRoll.percent` (mother as `FEMALE`, father as `MALE`), then `Inheritance.child`, then `ScaleRoll.fromPercent(babySex, …)`; growth goes through `Growth.percent` → `Inheritance.child` → `Growth.fromPercent`.
  - Each trait draws two `child.getRandom().nextDouble()` values: improve first, then spread.
  - Delete `Growth.child` and `OffspringScale`.
  - In `docs/DESIGN.md`, list the four new tuning keys with their defaults next to the existing ones.
- [ ] **Step 4:** Run `./gradlew :common:test`. Expected: PASS.
- [ ] **Step 5:** Commit: `Fold growth and size inheritance into one rule with a small spread.`

### Task 2: Weight, a heritable trait that shapes litters

**Files:**
- Modify: `common/.../sex/AnimalTraits.java` and `sex/AnimalTraitCodecs.java`
- Modify: `common/.../pregnancy/Pregnancy.java` and `pregnancy/PregnancyCodecs.java`
- Modify: `common/.../pregnancy/PregnancyController.java` (conception and `rememberParents`)
- Test: create `common/src/test/.../sex/AnimalTraitsTest.java` and `pregnancy/PregnancyTest.java`

**Interfaces:**
- Consumes: `Inheritance.child` (Task 1).
- Produces:
  - `AnimalTraits` appends `double weight, double fertility, long fertilityTick`, in that order after `cooldownUntil`. Every existing convenience constructor fills in `0.5, 1.0, -1L`.
  - Every existing wither carries the three new fields through.
  - New withers: `AnimalTraits withWeight(double weight)` and `AnimalTraits withFertility(double fertility, long tick)`.
  - `AnimalTraitCodecs.MAP_CODEC` adds `optionalFieldOf("weight", 0.5)`, `optionalFieldOf("fertility", 1.0)` and `optionalFieldOf("fertilityTick", -1L)`. `STREAM_CODEC` adds DOUBLE, DOUBLE and VAR_LONG (12 fields; `StreamCodec.composite` goes up to 14).
  - `Pregnancy` gains `double fatherWeight` after `fatherGrowthTicks`, before `litter`. The 7-arg and 6-arg constructors pass 0.5. `withConceivedAt` carries it.
  - `PregnancyCodecs.MAP_CODEC` adds `optionalFieldOf("fatherWeight", 0.5)`; `STREAM_CODEC` adds DOUBLE, so the constructor it uses becomes the 8-arg one with an empty litter.
- At conception, `LitterRoll.pick` and `pickTicks` get `motherTraits.weight()` in place of the fixed 0.5. The new `Pregnancy` stores `fatherTraits.weight()`.
- In `rememberParents`, the child's weight is `Inheritance.child(motherWeight, pregnancy.fatherWeight(), …)`. When the mother has no traits, her weight counts as 0.5. The child's traits get `.withWeight(weight)`.

- [ ] **Step 1: Write the failing tests.**
  - `AnimalTraitsTest`:
    - `oldTraitsDefaultToAverageWeightAndFullFertility`: `new AnimalTraits(Sex.FEMALE, 0.9)` has weight 0.5, fertility 1.0 and fertilityTick -1.
    - `withersKeepEveryOtherField`: `new AnimalTraits(Sex.FEMALE, 0.9).withWeight(0.8).withFertility(0.5, 100L).fed(0.4, 7L).withLove(50L)` has weight 0.8, fertility 0.5, fertilityTick 100, familiarity 0.4 and loveUntil 50.
  - `PregnancyTest`, `oldPregnanciesDefaultToAnAverageFather`: `new Pregnancy(0L, 1L, "minecraft:cow", 1.0, 1, 0.0).fatherWeight() == 0.5`, and `.withConceivedAt(9L).fatherWeight() == 0.5`.
- [ ] **Step 2:** Run `./gradlew :common:test`. Expected: FAIL to compile.
- [ ] **Step 3:** Implement the fields, codecs, withers and controller changes above.
- [ ] **Step 4:** Run `./gradlew build`. Expected: BUILD SUCCESSFUL, all tests pass.
- [ ] **Step 5:** Commit: `Add an inherited weight trait that picks litter size and pregnancy length.`

### Task 3: Fertility, worn out by births

**Files:**
- Create: `common/.../pregnancy/Fertility.java`
- Modify: `common/.../pregnancy/PregnancyController.java`
- Modify: `common/.../gametest/BreedingGameTests.java`, `fabric/.../gametest/FabricBreedingGameTests.java` and `neoforge/.../gametest/NeoForgeBreedingGameTests.java`
- Test: create `common/src/test/.../pregnancy/FertilityTest.java`

**Interfaces:**
- Consumes: `AnimalTraits.fertility()`, `fertilityTick()` and `withFertility` (Task 2). The `Tuning` fertility keys (Task 1).
- Produces, all reading `BarnyardConfig.current().tuning()`:
  - `public static final long DAY_TICKS = 24_000L`.
  - `public static double current(double fertility, long since, long now)`:
    - When `since < 0` or `now <= since`, return `clamp01(fertility)`.
    - Otherwise return `clamp01(fertility + recoveryPerDay * (now - since) / DAY_TICKS)`.
  - `public static double current(AnimalTraits traits, long now)`, which calls the above with `traits.fertility()` and `traits.fertilityTick()`.
  - `public static double afterBirth(double current)`, which returns `max(0, current - costPerBirth)`.
  - `public static boolean canConceive(double current)`, which returns `current >= minToConceive`.
  - `public static boolean wornOut(AnimalTraits traits, long now)`, which returns `!canConceive(current(traits, now))`.
- In `PregnancyController.startOrRefuse`:
  - When the mother is not pregnant and `!Fertility.canConceive(Fertility.current(motherTraits, now))`, call `restWithoutPregnancy(self, partner)` and `level.broadcastEntityEvent(mother, (byte) 18)` (love hearts), then return true.
  - `restWithoutPregnancy` is the existing cancelled-event block (set age 6000, reset love, `BreedingStatus.resting`) moved into a private method. The cancelled-event path calls it too.
- In `giveBirth`, the mother's traits update becomes `traits.gaveBirth(now, species).withFertility(Fertility.afterBirth(Fertility.current(traits, now)), now)`.
- Two new game tests, registered in `CASES` and in both loader classes:
  - `worn_out_mothers_rest_without_conceiving` (maxTicks 20):
    - Spawn a female pig whose traits are `withFertility(0.1, now)`, and a male pig. Put both in love.
    - Call `father.spawnChildFromBreeding(level, mother)`.
    - Assert that the mother has no pregnancy, that `assertResting` holds for both, and that there are no XP orbs.
  - `a_birth_costs_the_mother_fertility` (maxTicks 200):
    - Breed pigs, then backdate the pregnancy like `pigsConceiveALitterAndRest`.
    - In `succeedWhen`, assert that the mother has no pregnancy.
    - Assert that her stored fertility is within 1e-6 of 0.75 and that her fertilityTick is at least the conception tick.
    - Assert that every piglet has fertility 1.0 and a weight in 0–1.

- [ ] **Step 1: Write the failing tests.** In `FertilityTest`, use the default tuning:
  - `aBirthCostsAQuarter`: `afterBirth(1.0) == 0.75` and `afterBirth(0.1) == 0.0`.
  - `fertilityRecoversATenthADay`: `current(0.5, 0L, 24_000L) == 0.6` and `current(0.5, 0L, 12_000L) == 0.55`.
  - `recoveryCapsAtFullAndIgnoresAClockThatRunsBackwards`:
    - `current(0.5, 0L, 24_000L * 100) == 1.0`
    - `current(0.5, 1_000L, 0L) == 0.5`
    - `current(0.8, -1L, 99L) == 0.8`
  - `aQuarterIsEnoughToConceive`: `canConceive(0.25)` is true and `canConceive(0.2499)` is false.
  - `aWornOutMotherRecoversInADay`: with traits `withFertility(0.2, 0L)`, `wornOut(traits, 0L)` is true and `wornOut(traits, 24_000L)` is false.
- [ ] **Step 2:** Run `./gradlew :common:test`. Expected: FAIL to compile.
- [ ] **Step 3:** Implement `Fertility` and the controller changes. Add the two game tests.
- [ ] **Step 4:** Run `./gradlew build`, then `./gradlew :fabric:runGametest` and `./gradlew :neoforge:runGameTestServer`. Expected: all pass, including the two new cases.
- [ ] **Step 5:** Commit: `Wear mothers out with each birth and let them recover by the day.`

### Task 4: Inspector rows, then docs

**Files:**
- Modify: `common/.../inspect/InspectorSheet.java`, `client/InspectorScreen.java` and `client/InspectorLayout.java`
- Create: `common/src/main/resources/assets/barnyard_romance/textures/gui/sprites/inspector/weight.png` and `fertility.png`
  - Both are 13×13 and in the flat style of `hourglass.png`.
  - Weight is a grey iron weight with a handle. Fertility is a green sprout with a pink bud.
- Modify: `docs/ROADMAP.md`, `docs/superpowers/plans/2026-10-24-live-birth-roadmap.md` (link this plan) and `docs/DESIGN.md` (traits)
- Test: modify `common/src/test/.../inspect/InspectorSheetTest.java` and `client/InspectorLayoutTest.java`

**Interfaces:**
- Consumes: `Fertility.current` and `wornOut` (Task 3), and `AnimalTraits.weight()` (Task 2).
- Produces:
  - `InspectorSheet` gains `double weight, String weightText` after `growthText`, and `double fertility, String fertilityText` after `maxHealth`. `unknown(...)` fills in `0.0, ""`.
  - `public boolean showFertility()` returns `known && female && "Adult".equals(life)`.
  - `public static final String WORN_OUT = "Worn out"`.
  - `breeding(boolean needsTaming, boolean tamed, double familiarity, boolean wornOut, long loveUntil, long cooldownUntil, long now)`. The checks run in this order: not tamed, needs familiarity, resting, `WORN_OUT`, in love, ready.
  - The values are shown as percentages with `Familiarity.percent`.
- Layout: `ROW_WEIGHT = 96`, `ROW_HEALTH = 112`, `ROW_FERTILITY = 138`, `ROW_BREEDING = 154`, `HEIGHT = 184`.
- In the screen:
  - The weight row is a `meter` with icon `weight`, label "Weight" and fill `fill_feed`.
  - The fertility row is a `meter` with icon `fertility`, label "Fertility" and fill `fill_pregnant`, drawn only when `showFertility()`.
  - `wornOut` is passed as `female && Fertility.wornOut(value, now)`.

- [ ] **Step 1: Write the failing tests.**
  - `InspectorSheetTest`:
    - Every `breeding(...)` call gains `false` as the fourth argument, and the `sheet(...)` helper fills the new fields.
    - `aWornOutMotherSaysSo`: `breeding(false, false, 1.0, true, -1, -1, 0)` is `WORN_OUT`, and `readyToBreed(WORN_OUT)` is false.
    - `restingWinsOverWornOut`: `breeding(false, false, 1.0, true, -1, 1200, 0)` is `"Resting 1m 0s"`.
    - `fertilityIsShownForAdultFemalesOnly`: the female adult sheet is true; the male adult and the female baby are false.
  - `InspectorLayoutTest`, `everythingFitsInsideThePanelBorder`:
    - Use `ROW_FERTILITY` in place of `ROW_BREEDING` for the heart rows.
    - Add `ROW_GROWTH + 14 + 9 <= ROW_WEIGHT`, `ROW_WEIGHT + 16 <= ROW_HEALTH` and `ROW_FERTILITY + 16 <= ROW_BREEDING`.
- [ ] **Step 2:** Run `./gradlew :common:test`. Expected: FAIL to compile.
- [ ] **Step 3:** Implement the sheet, layout, screen and sprites.
  - Draw the sprites with a short Pillow script kept in the scratchpad, not the repo.
  - Update `docs/DESIGN.md` (the weight and fertility traits), tick the Stage 2 items in `docs/ROADMAP.md`, and link this plan from the Stage 2 section of the roadmap plan.
- [ ] **Step 4:** Run `./gradlew build`. Expected: BUILD SUCCESSFUL.
- [ ] **Step 5:** Commit: `Show weight and fertility in the inspector, and tick off Stage 2.`

## Follow-up (after review)

- The inspector's stats are a list. `InspectorSheet.rows()` says which rows show, in order. `InspectorLayout.stack` places each one right below the last, so a hidden row (fertility on males, breeding on babies) leaves no gap. The Health row is one or two lines tall depending on the heart rows, and the panel height follows the list but never cuts the portrait. This replaces the fixed `ROW_*` constants from Task 4.
