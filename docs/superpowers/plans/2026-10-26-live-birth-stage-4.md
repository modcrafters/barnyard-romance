# Stage 4: Diets, Food Files and Superfoods Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** What an animal eats, and what each food does, comes from datapack files. Reusable diets say which foods a species takes, which it refuses, and how much each one does. Food can heal. Two superfoods, breeder's mash and golden bran, speed a pregnancy or improve the litter. A debug command scans every recipe and writes ingredient tags, such as "contains meat", for mod packs.

**Architecture:**
- A pure `food` package holds the rules: the parsed diet and food files, the order that decides what a feed does, the heal amount, and the recipe scan's propagation. None of it touches Minecraft classes, so it is unit tested.
- `FoodData` holds the loaded diets and food files. Two reload listeners fill it (`barnyard_romance/diets`, `barnyard_romance/foods`), registered by each loader. It is server-side only.
- `FamiliarityActions.interact` asks `FoodRules` instead of `animal.isFood`. The two tempt mixins also follow diet foods.
- The pregnancy gains a `PregnancyCare` part: banked ticks, one speed boost, and a nutrition bonus. `PregnancyClock` reads the boost lazily, so nothing ticks.
- `FoodScan` walks the recipe graph from every tag a diet refuses, and writes a datapack for mod packs to review.

**Tech Stack:** Minecraft 26.3, Java 25, MultiLoader (common / fabric / neoforge), Fabric API 0.162.0+26.3 (`fabric-resource-loader-v1`), NeoForge 26.3.0.76, JUnit 5, game tests on both loaders.

**Spec:**
- Stage 4 of `docs/superpowers/plans/2026-10-24-live-birth-roadmap.md` (requests #2, #7).
- The user's decisions on 2026-10-26, recorded below.

## Decisions (from the 2026-10-26 conversation)

- **No Pam's HarvestCraft.** Superfood recipes use vanilla items through the mod's own item tags. Packs widen those tags or replace the recipes. Nothing checks for other mods.
- **Diets are reusable files.** Each diet file lists the species it applies to by default (`species`). A species' config entry can point at another diet with `diet`, and that wins. A species with no diet keeps only its vanilla food. Nothing about diets is in Java: the user asked on 2026-10-26 for nothing to be hardcoded.
- **First match wins:**
  1. A diet's `refuses` tag: the animal turns away and the item is kept.
  2. A food file that names the species or its diet: that file's effects, as written.
  3. A diet `eats` entry: the default effects × the entry's `factor`.
  4. The species' vanilla food (`animal.isFood`): the default effects × 1.
  5. Otherwise it isn't food.
- **Default effects** for steps 3 and 4: familiarity gain = `familiarityGain`, and heal from the heal rule.
- **Heal rule:** an explicit `heal` wins. Otherwise a player food heals `foodHealFactor` (0.5) × its nutrition, and anything else heals `foodHealDefault` (1.0). Both are scaled by the diet factor.
- **Horses keep vanilla's heal values** through `data/barnyard_romance/barnyard_romance/foods/equine.json`:
  - horse family (horse, donkey): wheat 2, sugar 1, hay bale 20, apple 3, red mushroom 3, carrot 3, golden carrot 4, golden apple and enchanted golden apple 10;
  - llamas: wheat 2, hay bale 10;
  - camel: cactus 2.
- **Horse temper is dropped.** Familiarity already tames the horse family (Stage 1), so `horseTemper` is not built (YAGNI).
- **Bad food is a negative `pregnancyProgress`.** There is no separate `bad` effect.
- **A hurt animal takes normal food any time.** Inside the daily wait, it only heals. Familiarity, love mode and the feed clock don't change. The shy rule still holds: below 30% you must sneak.
- **Superfoods** (`"superfood": true` in their food file):
  - They skip the once-a-day rule and never touch familiarity or the feed clock.
  - They only work on a pregnant female. Otherwise the item is kept and the player sees "Only a pregnant mother needs this."
  - Only one speed boost runs at a time. A second mash while one runs is refused with "She's already on a mash." and kept.
  - Golden bran is refused once the litter's nutrition is already 1.0: "She can't eat any more of this."
- **Superfood list:**
  - Breeder's mash: `pregnancySpeed` ×2 for 24,000 ticks (one day saved), plus heal 2.
  - Golden bran: `babyImprove` 0.25, plus heal 2.
  - Both are for the `grazer`, `herbivore` and `omnivore` diets.
- **One group tag:** `data/barnyard_romance/tags/item/contains_meat.json`.
  - It holds `#minecraft:meat`, `#minecraft:fishes`, `#c:foods/raw_meat`, `#c:foods/cooked_meat`, `#c:foods/raw_fish`, `#c:foods/cooked_fish` and `minecraft:rabbit_stew`. All `c:` entries are `"required": false`.
  - Fish counts as meat: no vanilla rule treats them differently.
  - `sweets` and `contains_fish` were dropped (decided 2026-10-26), because no vanilla diet refuses them. Packs can add their own group tags.
- **The scan command** is `/barnyard foods scan` (op level 2, like the other `/barnyard` commands).
  - **The groups it scans are every tag named in any loaded diet's `refuses`.** Today that is only `contains_meat`. A pack whose diet refuses its own `#mypack:sweets` gets that tag scanned too.
  - It writes a datapack folder `<server dir>/barnyard_romance/scan/barnyard_romance_scan/`, holding `pack.mcmeta` and one `replace: false` tag file per group.
  - Next to the folder it writes `report.json`: each added item, its groups, the recipe that put it there, and the recipe types it skipped as unreadable.
  - Rules:
    - An ingredient carries a group only if **every** item it accepts is in the group, so "any meat" counts but "carrot or beef" doesn't.
    - An item joins a group if **any** recipe that makes it has such an ingredient.
    - It repeats until nothing changes.
    - Only food items are written: items with the `food` component, items in `#c:foods`, or items some diet eats. Non-food items still pass groups on, e.g. a modded dough.
  - Recipe ingredients come from `placementInfo().ingredients()`, and the result from `display()` → `result().resolveForStacks(SlotDisplayContext.fromLevel(level))`. A recipe that is `isSpecial()`, `isImpossibleToPlace()` or has no result is skipped and counted by recipe type.
  - The scan never installs the datapack itself. The pack maker copies it into `datapacks/` and runs `/reload`.

## File formats

```jsonc
// data/<ns>/barnyard_romance/diets/<name>.json
{ "species": ["minecraft:cow"],
  "eats": [ { "items": "#c:crops", "factor": 1.0 }, { "items": ["minecraft:hay_block"], "factor": 1.0 } ],
  "refuses": [ "#barnyard_romance:contains_meat" ] }

// data/<ns>/barnyard_romance/foods/<name>.json
{ "species": ["minecraft:horse"],          // and/or
  "diets": ["barnyard_romance:grazer"],     // at least one of the two
  "items": "#barnyard_romance:breeders_mash" ,
  "superfood": false,
  "effects": { "familiarity": 0.2, "heal": 2.0,
               "pregnancySpeed": { "factor": 2.0, "ticks": 24000 },
               "pregnancyProgress": 0.1, "babyImprove": 0.25 } }
```

- `items` is one string or a list. `#ns:path` is a tag and `ns:path` is an item.
- `factor` clamps to 0–4. `heal` clamps to 0–1024. `pregnancyProgress` clamps to −1–1. `babyImprove` clamps to 0–1. `pregnancySpeed.factor` clamps to 0.1–10, and `ticks` to 1–480,000.

**Built-in diets** (data files in `common/src/main/resources/data/barnyard_romance/barnyard_romance/diets/`):

| Diet | Species (the file's `species`) | Eats (factor) | Refuses |
|---|---|---|---|
| `grazer` | cow, mooshroom, sheep, goat, horse, donkey, llama, trader_llama, camel | `#c:crops` 1, `hay_block` 1, `#c:foods/vegetable` 1, `#c:seeds` 0.5, `#c:foods/fruit` 0.75, `#c:foods/bread` 0.5 | `contains_meat` |
| `herbivore` | rabbit | `#c:foods/vegetable` 1, `#c:crops` 1, `#minecraft:small_flowers` 0.75, `#c:foods/fruit` 0.75 | `contains_meat` |
| `omnivore` | pig, fox, polar_bear | `#c:crops` 1, `#c:foods/vegetable` 1, `#c:foods/fruit` 1, `#c:foods/bread` 0.75, `#barnyard_romance:contains_meat` 0.75 | (none) |
| `carnivore` | wolf, cat, ocelot | `contains_meat` 1, `#c:foods/vegetable` 0.25, `#c:foods/fruit` 0.25 | (none) |
| `panda` | panda | `bamboo` 1, `cake` 1, `#c:foods/fruit` 0.25 | `contains_meat` |
| `hoglin` | hoglin | `crimson_fungus` 1, `#c:crops` 0.25 | `contains_meat` |

## Global Constraints

- About 80–90% of the logic lives in `common`. Loader code is registration, reload listeners, creative tabs and game test wiring only.
- The server is authoritative. Diets and food files are server data and are not synced. A client predicting a non-vanilla diet food may skip the arm swing, which is fine.
- Every hook catches `RuntimeException`, logs it, and falls back to vanilla behaviour, as `FamiliarityActions` does now.
- A diet or food file that fails to parse is skipped and logged, and the rest still load. A species pointing at a missing diet acts as if it had none, logged once per reload.
- Every new number has a config key and a default. Bad values clamp and never throw.
- Saved data stays backward compatible: `optionalFieldOf("care", PregnancyCare.NONE)`.
- Unit tests cover pure logic only.
- Build with JDK 25 using `./gradlew build`. Game tests: `./gradlew :fabric:runGametest` and `./gradlew :neoforge:runGameTestServer`.
- Work on branch `live-birth-stage-4` from `main` (8f7a0bf). The branch already holds the roadmap edit dropping Pam's.

## Review Focus

1. **Vanilla breeding still works with no diet or a broken diet.** A modded animal, or a species whose diet file is missing, still eats its vanilla food at factor 1. Pinned by `vanillaFoodAlwaysFeeds` and `missingDietFallsBackToVanillaFood` (`FoodRulesTest`).
2. **A refused food is never used up.** A cow offered beef keeps the beef and doesn't change familiarity or the feed clock. Pinned by the game test `grazers_refuse_meat`.
3. **Old pregnancies still load and finish on time.** A pregnancy without `care` loads as `NONE`, and its progress equals the old formula. Pinned by `noCareMatchesTheOldClock` (`PregnancyClockTest`) and `oldPregnancyLoadsWithoutCare` (`PregnancyTest`).
4. **The debug sticks still set exact percentages after a mash.** `withConceivedAt` clears the banked ticks and any boost, but keeps the nutrition bonus. Pinned by `settingConceivedAtClearsTheBoost` (`PregnancyTest`).
5. **Cycles in recipes don't hang the scan.** Wheat ↔ hay bale and similar pairs end in a fixpoint. Pinned by `cyclesStop` (`FoodScanTest`).

---

### Task 1: The pure food model, parsing and rules

**Files:**
- Create in `common/.../food/`:
  - `ItemMatch.java`: one parsed `items` entry, a tag or an item `Identifier`.
  - `DietEntry.java`, `Diet.java`, `FoodEffects.java`, `FoodFile.java`
  - `FoodCodecs.java`: Mojang `Codec`s for `Diet` and `FoodFile`, with clamping.
  - `FoodRules.java`: the first-match order.
  - `FoodHeal.java`
- Modify: `config/Tuning.java` (`foodHealFactor` 0.5 unit, `foodHealDefault` 1.0 clamped 0–20)
- Modify: `config/BarnyardConfig.java` (per-species `diet` string, plus `dietOverride(speciesId)`, which returns the user's entry or `null`)
- Test: create `food/FoodCodecsTest.java`, `food/FoodRulesTest.java` and `food/FoodHealTest.java`; extend `TuningTest` and `BarnyardConfigTest`

**Interfaces:**
- `record ItemMatch(boolean tag, Identifier id)`, plus `static ItemMatch parse(String)`, which throws `IllegalArgumentException` on a bad id.
- `record DietEntry(List<ItemMatch> items, double factor)`
- `record Diet(List<Identifier> species, List<DietEntry> eats, List<ItemMatch> refuses)`
- `FoodRules.dietFor(Identifier speciesId, Identifier override, Map<Identifier, Diet> diets)`, returning `Optional<Map.Entry<Identifier, Diet>>`. The override wins. Otherwise it picks the first diet, by sorted id, that lists the species. A missing override falls back to the listing diet.
- `record PregnancySpeed(double factor, long ticks)`, inside `FoodEffects`.
- `record FoodEffects(OptionalDouble familiarity, OptionalDouble heal, Optional<PregnancySpeed> pregnancySpeed, double pregnancyProgress, double babyImprove)`, with `boolean needsPregnancy()` (true when it has any pregnancy or baby effect).
- `record FoodFile(List<Identifier> species, List<Identifier> diets, List<ItemMatch> items, boolean superfood, FoodEffects effects)`
- `FoodCodecs.DIET` and `FoodCodecs.FOOD_FILE`. A food file that names neither species nor diets is an error.
- `interface ItemFacts { boolean matches(ItemMatch match); boolean vanillaFood(); OptionalInt nutrition(); }`, which keeps `FoodRules` pure. Tests pass fakes.
- `sealed interface Feed`:
  - `Refused`, `NotFood`
  - `Eaten(FoodEffects effects, double factor, boolean superfood)`
- `FoodRules.resolve(Identifier speciesId, Identifier dietId, Diet diet, List<FoodFile> files, ItemFacts item)`, returning a `Feed`. `diet` may be `null`; `dietId` may be `null`.
- `FoodHeal.amount(OptionalDouble explicitHeal, OptionalInt nutrition, double factor, Tuning tuning)`, returning a `float`.

- [x] **Step 1: Write the failing tests:**
  - `FoodCodecsTest`:
    - `parsesTagsAndItems`
    - `oneStringOrAList`
    - `clampsFactorsAndEffects`
    - `foodFileNeedsSpeciesOrDiet`
    - `rejectsBadIds`
  - `FoodRulesTest`:
    - `refusedBeatsEverything`
    - `foodFileBeatsDiet`
    - `foodFileMatchesByDiet`
    - `firstDietEntryWins`
    - `vanillaFoodAlwaysFeeds`
    - `missingDietFallsBackToVanillaFood`
    - `unknownItemIsNotFood`
  - `FoodHealTest`:
    - `explicitHealWins`
    - `playerFoodHealsHalfItsNutrition` (carrot 3 → 1.5)
    - `nonFoodHealsTheDefault` (→ 1.0)
    - `factorScalesHeal` (0.25 × carrot → 0.375)
  - `TuningTest`: defaults and clamps for the two new keys.
  - `FoodRulesTest`: `dietOverrideWins`, `dietListsItsSpecies`, `missingOverrideFallsBackToTheListingDiet`.
  - `BarnyardConfigTest`: `dietOverrideReadsAndWrites`.
- [x] **Step 2: Run `./gradlew :common:test`.** Expected: these tests fail to compile or fail.
- [x] **Step 3: Implement the files above.**
- [x] **Step 4: Run `./gradlew :common:test`.** Expected: PASS.
- [x] **Step 5: Commit** "Add diets and food files as pure rules, with the heal rule."

### Task 2: Loading diets and food files, the group tags and the built-in data

**Files:**
- Create: `common/.../food/FoodData.java`, the static holder for the current `Map<Identifier, Diet>` and `List<FoodFile>`, with `dietFor(Animal)` and `resolve(Animal, ItemStack)`.
- Create: `common/.../food/FoodReloadListeners.java`. It holds two `SimpleJsonResourceReloadListener` subclasses, using `FileToIdConverter.json("barnyard_romance/diets")` and `FileToIdConverter.json("barnyard_romance/foods")`. Each `apply` replaces its half of `FoodData` and logs the counts.
- Create: `common/.../food/MinecraftItemFacts.java`, implementing `ItemFacts` for an `Animal` and an `ItemStack`. It uses `stack.is(TagKey)` and `stack.is(item)`, `animal.isFood(stack)`, and `stack.get(DataComponents.FOOD)`'s `nutrition()`.
- Modify: `fabric/.../BarnyardRomanceFabric.java`: `ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, listener)` for both listeners.
- Modify: `neoforge/.../BarnyardRomanceNeoForge.java`: `AddServerReloadListenersEvent#addListener(id, listener)` on `NeoForge.EVENT_BUS`.
- Create the data under `common/src/main/resources/data/barnyard_romance/`:
  - `barnyard_romance/diets/{grazer,herbivore,omnivore,carnivore,panda,hoglin}.json`, from the diet table;
  - `barnyard_romance/foods/equine.json`, with the vanilla horse, llama and camel heal values;
  - `tags/item/contains_meat.json`, from the group tag decision.

**Interfaces:**
- Consumes: `FoodCodecs`, `FoodRules`, `BarnyardConfig.dietOverride`.
- Produces:
  - `FoodData.resolve(Animal animal, ItemStack stack)`, returning a `Feed`. It returns `Feed.NotFood` for untracked species and falls back to `animal.isFood` when nothing is loaded.
  - `FoodData.eats(Animal animal, ItemStack stack)`, which is true for `Feed.Eaten`.
  - `FoodData.replaceDiets(Map<Identifier, Diet>)` and `FoodData.replaceFoods(List<FoodFile>)`.

- [x] **Step 1: Write the code and data.**
- [x] **Step 2: Add the game tests** to `gametest/BreedingGameTests.java` and the Fabric list in `FabricBreedingGameTests`:
  - `built_in_diets_load`: a cow resolves to the `grazer` diet, and a wolf to `carnivore`.
  - `group_tag_holds_vanilla_meat`: `rabbit_stew`, `beef` and `cod` are in `contains_meat`.
- [x] **Step 3: Run `./gradlew build`, `:fabric:runGametest` and `:neoforge:runGameTestServer`.** Expected: all pass.
- [x] **Step 4: Commit** "Load diets and food files from datapacks, with built-in diets and group tags."

### Task 3: Feeding by diet, healing and tempting

**Files:**
- Modify: `familiarity/FamiliarityActions.java`. `interact` uses `FoodData.resolve`:
  - `Refused` sends "It won't eat that." and returns `CONSUME` without using the item.
  - `NotFood` returns `null`, as before.
  - `Eaten` that isn't a superfood follows today's path. Familiarity gain = `effects.familiarity().orElse(gain())` × `factor`. It heals by `FoodHeal.amount`.
  - A hurt animal inside the daily wait still eats, but only heals. "Hurt" means `getHealth() < getMaxHealth()`.
  - Superfoods are routed to Task 4's `PregnancyCareActions.feed`.
- Modify: `familiarity/Familiarity.java`. Add `Click click(boolean sneaking, boolean familiar, Response response, boolean hurt)`, where a hurt animal gets `HEAL_ONLY` in place of `WAIT`. Add `HEAL_ONLY` to `Click`.
- Modify: `familiarity/FamiliarityActions.ignoresFood`. A hurt animal is interested.
- Modify: `mixin/TemptGoalMixin.java`. A `shouldFollow(LivingEntity)` `RETURN` inject ORs in a player holding a food the mob eats (`FoodData.eats` on either hand).
- Modify: `mixin/TemptingSensorMixin.java`. An `isTemptation(PathfinderMob, ItemStack)` `RETURN` inject ORs in `FoodData.eats`.
- Test: extend `familiarity/FamiliarityTest.java` with `hurtAnimalsHealInsideTheWait` and `hurtButShyStillScares`.

**Interfaces:**
- Consumes: `FoodData.resolve`, `FoodData.eats`, `FoodHeal.amount`.
- Produces: `Familiarity.Click.HEAL_ONLY`.

- [x] **Step 1: Write the failing unit tests, then run `./gradlew :common:test`.** Expected: FAIL.
- [x] **Step 2: Implement the changes above, then run `./gradlew :common:test`.** Expected: PASS.
- [x] **Step 3: Add the game tests:**
  - `grazers_refuse_meat`: a cow and beef. Beef is kept, familiarity and `lastFedTick` don't change.
  - `diet_food_feeds`: a cow and a carrot (not cow food in vanilla). Familiarity rises by 0.2.
  - `carnivores_get_little_from_salad`: a wolf and a carrot. Familiarity rises by 0.05.
  - `hurt_animals_heal_from_food_any_time`: a cow fed this tick, hurt by 4 and given a carrot, heals 1.5 with no change to familiarity.
  - `horses_heal_like_vanilla`: a hurt horse heals 2 from wheat.

  For the feeding tests, use a mock player from `helper.makeMockPlayer(GameType.SURVIVAL)` with the item in hand and call `FamiliarityActions.interact` directly.
- [x] **Step 4: Run both loaders' game tests.** Expected: all pass.
- [x] **Step 5: Commit** "Feed animals by diet, heal them with food, and tempt them with any food they eat."

### Task 4: Pregnancy care: speed, progress and a better litter

**Files:**
- Create: `common/.../pregnancy/PregnancyCare.java`
- Modify:
  - `pregnancy/Pregnancy.java`: append `PregnancyCare care` to the canonical record, with all short constructors passing `PregnancyCare.NONE`. Add `withCare(PregnancyCare)`. `withConceivedAt` clears the care's ticks but keeps its `nutritionBonus`.
  - `pregnancy/PregnancyCodecs.java`: `care` saved with `optionalFieldOf("care", NONE)` and added to the `STREAM_CODEC` through `PregnancyCare.STREAM_CODEC`.
- Modify: `pregnancy/PregnancyClock.java`. Add overloads taking a `Pregnancy`, and point every caller at them. The callers are `InspectorScreen`, `HerdStick`, `PregnancyText` and `PregnancyController.catchUp`.
- Modify: `pregnancy/PregnancyController.rememberParents`, so nutrition = `clamp(motherFamiliarity + care.nutritionBonus(), 0, 1)`.
- Create: `common/.../food/PregnancyCareActions.java`, holding `feed(Animal, Player, ItemStack, Feed.Eaten)` for superfoods, with the refusals and messages from Decisions. It consumes one item outside creative and heals as the effects say.
- Test: create `pregnancy/PregnancyCareTest.java`; extend `PregnancyClockTest` and `PregnancyTest`.

**Interfaces:**
- `record PregnancyCare(long bankedTicks, long boostFrom, long boostUntil, double boostFactor, double nutritionBonus)`, with `NONE = (0, 0, 0, 1.0, 0.0)` and these methods:
  - `long extraTicks(long now)`: `bankedTicks + round((boostFactor − 1) × max(0, min(now, boostUntil) − boostFrom))`.
  - `boolean boosting(long now)`: `now < boostUntil`.
  - `PregnancyCare boost(long now, double factor, long ticks)`: banks the previous boost's full extra first.
  - `PregnancyCare progress(long durationTicks, double fraction)`: banks `round(fraction × durationTicks)`.
  - `PregnancyCare improve(double amount)`
- `PregnancyClock.progress(long now, Pregnancy p)`, `remainingTicks(long now, Pregnancy p)` and `ready(long now, Pregnancy p)`. Effective elapsed = `max(0, now − conceivedAt + care.extraTicks(now))`.
- Remaining real ticks, where R is the remaining effective ticks:
  - if a boost runs for b more ticks and `R ≤ factor × b`, it is `ceil(R / factor)`;
  - otherwise it is `b + (R − factor × b)`.

- [x] **Step 1: Write the failing tests:**
  - `PregnancyCareTest`:
    - `boostDoublesProgressWhileItRuns`
    - `boostStopsAtItsEnd`
    - `secondBoostBanksTheFirst`
    - `negativeProgressSlowsButNeverGoesBelowZero`
    - `improveAddsToNutrition`
  - `PregnancyClockTest`:
    - `noCareMatchesTheOldClock`
    - `remainingCountsTheBoost` (R = 6,000 and a ×2 boost with 24,000 left → 3,000)
  - `PregnancyTest`:
    - `oldPregnancyLoadsWithoutCare` (codec round trip of a map without `care`)
    - `settingConceivedAtClearsTheBoost`
- [x] **Step 2: Run `./gradlew :common:test`.** Expected: FAIL.
- [x] **Step 3: Implement the changes above, then run `./gradlew :common:test`.** Expected: PASS.
- [x] **Step 4: Commit** "Let food speed up a pregnancy and improve the litter."

### Task 5: The superfood items, recipes and their tags

**Files:**
- Modify: `item/BarnyardItems.java`. Add `BREEDERS_MASH` and `GOLDEN_BRAN`, plain `Item`s stacking to 64 with `Rarity.UNCOMMON`, plus their `ResourceKey`s.
- Modify: both loader entrypoints, to register the two items and add them to the vanilla Food & Drinks creative tab. Fabric uses `ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)`. NeoForge uses `BuildCreativeModeTabContentsEvent`.
- Create the data:
  - `data/barnyard_romance/tags/item/superfood/grains.json`, holding `minecraft:wheat`.
  - `superfood/roots.json`, holding carrot, beetroot and potato.
  - `superfood/golden.json`, holding golden carrot and glistering melon slice.
  - `data/barnyard_romance/recipe/breeders_mash.json`, shapeless: 2 × `#superfood/grains` + 2 × `#superfood/roots` → 2 mash.
  - `recipe/golden_bran.json`, shapeless: 2 × `#superfood/grains` + 1 × `#superfood/golden` → 2 bran.
  - `barnyard_romance/foods/breeders_mash.json` and `golden_bran.json`, with the effects from Decisions.
- Create the assets:
  - `assets/barnyard_romance/items/{breeders_mash,golden_bran}.json`, plus `models/item/*.json` using `minecraft:item/generated`.
  - 16×16 textures in `textures/item/`, painted by `build/paint_superfoods.py` with PIL, as `build/paint_inspector.py` was. Breeder's mash is a brown bowl of mash with green flecks. Golden bran is a small gold-flecked sack.
  - `lang/en_us.json` entries "Breeder's Mash" and "Golden Bran".

- [x] **Step 1: Write the items, data and assets.**
- [x] **Step 2: Add the game tests:**
  - `breeders_mash_speeds_a_pregnancy`: a pregnant cow fed mash has `care.boosting(now)`. A second mash is refused and kept.
  - `golden_bran_feeds_the_litter`: `nutritionBonus` is 0.25 after one bran.
  - `superfoods_need_a_pregnant_mother`: a male cow keeps the mash.
  - `superfood_recipes_load`: both recipe ids exist in `server.getRecipeManager()`.
- [x] **Step 3: Run `./gradlew build` and both loaders' game tests.** Expected: all pass.
- [x] **Step 4: Run the Fabric client game test,** then check its screenshot to make sure the item textures render without the missing-texture checkerboard. Use `/give` before the screenshot, or add the items to the test player's hotbar.
- [x] **Step 5: Commit** "Add breeder's mash and golden bran with vanilla recipes built from item tags."

### Task 6: The recipe scan command

**Files:**
- Create: `common/.../food/FoodScan.java`, pure, with the propagation rule from Decisions.
- Create: `common/.../food/FoodScanRunner.java`. It turns `server.getRecipeManager().getRecipes()` into `ScanRecipe`s, runs `FoodScan`, and writes the datapack and report under `server.getServerDirectory().resolve("barnyard_romance/scan")`. It returns a `ScanSummary`.
- Modify: `command/BarnyardCommands.java`. Add `/barnyard foods scan`, which sends "Scanned N recipes: added X items to Y tags. Wrote <path>. Copy the barnyard_romance_scan folder into your world's datapacks and run /reload." Skipped recipe types follow on one line when there are any.
- Test: create `food/FoodScanTest.java`.

**Interfaces:**
- `record ScanRecipe(String id, String type, String result, List<Set<String>> ingredients)`
- `record ScanResult(Map<String, SortedSet<String>> added, Map<String, String> because)`. `because` maps an item to the recipe id that first put it in a group.
- `FoodScan.run(List<ScanRecipe> recipes, Map<String, Set<String>> groups, Predicate<String> food)`, returning a `ScanResult`. `groups` maps a group tag to its current members, already resolved.
- `record ScanSummary(int recipes, int items, int tags, Map<String, Integer> skippedByType, Path folder)`
- `FoodScanRunner.run(MinecraftServer server, Map<String, Set<String>> groups, Path folder)`, returning a `ScanSummary`.
- `FoodScanRunner.liveGroups(MinecraftServer server)`, returning `Map<String, Set<String>>`: every tag in a loaded diet's `refuses`, with its current members.
- `pack.mcmeta` uses `min_format` and `max_format` equal to the server's current data pack version, `SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA)`.

- [x] **Step 1: Write the failing `FoodScanTest`:**
  - `sugarCarriesIntoCake`
  - `deepChainsPropagate` (sugar → dough → pie)
  - `everyOptionMustBeInTheGroup`
  - `anyRecipeIsEnough`
  - `onlyFoodIsWritten`
  - `seedsAreNotRepeated`
  - `cyclesStop`
- [x] **Step 2: Run `./gradlew :common:test`.** Expected: FAIL.
- [x] **Step 3: Implement `FoodScan`, then run `./gradlew :common:test`.** Expected: PASS.
- [x] **Step 4: Implement the runner and the command,** writing JSON with Gson pretty printing and UTF-8.
- [x] **Step 5: Add the game test `food_scan_reads_real_recipes`.** It runs `FoodScanRunner.run(server, groups, folder)` against the test server, pointing at a temp folder. The test uses an override `groups` of `{ "barnyard_romance:test_sugar": {"minecraft:sugar"} }`, because vanilla's only meat dish is already in the shipped tag. It checks three things:
  - `cake` and `pumpkin_pie` are added through their real recipes;
  - `sugar` itself is not written;
  - `report.json` and `pack.mcmeta` exist.

  The command calls the same method with the groups read from the live tags.
- [x] **Step 6: Run `./gradlew build` and both loaders' game tests.** Expected: all pass.
- [x] **Step 7: Commit** "Add /barnyard foods scan, which writes ingredient tags from the recipe graph."

### Task 7: Docs

- [x] `docs/DESIGN.md`:
  - replace §5 with diets, food files, the first-match order, healing, superfoods and the scan;
  - add `diet`, `foodHealFactor` and `foodHealDefault` to the config and tuning lists;
  - show both file formats with an example.
- [x] `docs/ROADMAP.md`: tick off Stage 4, adding lines for diets, healing and the scan command.
- [x] The roadmap plan: note in Stage 4 that `horseTemper` and `bad` were dropped and diets were added. Link this plan.
- [x] Commit "Document diets, food files, superfoods and the recipe scan, and tick off Stage 4."
