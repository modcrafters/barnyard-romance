# Barnyard Romance: Design

## 1. Sex
- Every breedable animal (`Animal` subclass, vanilla + modded) gets a persistent `sex` (MALE/FEMALE), assigned randomly on first spawn (configurable ratio, default 50/50).
- Stored via data attachment (Fabric Data Attachment API / NeoForge attachments), saved to NBT, synced to clients.
- Existing animals in old worlds get a sex assigned lazily the first time they load.

## 2. Size (Milestone 1)
- Uses vanilla `minecraft:scale` attribute (1.20.5+), so hitbox, rendering and riding follow automatically. No new models needed.
- Defaults: females span 0.70 to 0.90, males span 1.10 to 1.30. The stored value is that scale. The tooltip shows S / M / L / XL. Per-species overrides in config.
- Natural spawns, spawn eggs, and summoned animals roll only the S–M half of their sex. L and XL come from breeding.
- A baby starts at the average of the parents' positions in their own ranges. There is a 35% chance to step up one band. The mother's familiarity at conception is the nutrition value: full familiarity raises that chance to 80%.
- Meat drops follow the actual scale. A 0.70 animal drops 2–3 meat, a 1.30 animal drops 8–9. Looting still adds on top. Leather, feathers, and wool stay on the vanilla table.
- Babies keep their rolled scale, and the adult size is applied when they grow up.
- Test in vanilla: `/attribute @e[type=cow,limit=1,sort=nearest] minecraft:scale base set 1.15`

## 3. Breeding rules
- Hook vanilla breeding (`Animal#canMate` / love mode) via mixin: only MALE + FEMALE of the same species can mate, and only when both are at least 30% familiar. A feed that crosses that line puts an adult into love mode. Below 30%, pregnancy cannot start.
- Horses, donkeys, llamas, camels, and wolves override `canMate` without calling `Animal#canMate`, so they get the same check through their own overrides. A horse and a donkey of opposite sexes still make a mule.
- If a same-sex pair reaches breeding anyway (a modded override), love mode ends with no baby and no XP.
- On NeoForge, `BabyEntitySpawnEvent` fires at conception (for the first baby), while both parents are present. A listener can swap that baby. If it cancels the event, or swaps a real baby for nothing, there is no pregnancy and no XP; both parents get vanilla's 6,000-tick rest and love mode ends.
- Love mode and the breeding rest are synced to clients as the game tick they end, so the inspector shows "In love" and "Resting 4m 10s" and goes back to "Ready" by itself.
- Optional: cooldowns, fertility (see Ideas).
- Config: per-species enable/disable; species with custom breeding code can be excluded or patched with compat.

## 4. Pregnancy
- On successful mating, cancel instant baby spawn. Mark the female pregnant and store: progress (0-100%), father species, father traits, litter size, and the mother's familiarity. The litter itself is made at conception, while the father is still there, so mules, mixed wool colors, horse stats, and variants come out as in vanilla even if the father is gone by the birth. The stored litter is saved but not sent to clients.
- Progress ticks periodically (not every tick; e.g. every N ticks or on day time), so it's cheap and survives chunk unload (compute elapsed game time on load).
- At 100% the female gives birth. The baby starts with half of the familiarity stored at conception.
- Pregnancy length and litter size are min–max ranges, rolled at conception. Vanilla mammals have built-in presets (24,000 ticks = one day):

  | species | pregnancy | litter |
  |---|---|---|
  | cow, mooshroom, llama, trader llama | 60,000–72,000 | 1 |
  | horse, donkey, camel | 72,000–96,000 | 1 |
  | sheep, goat | 36,000–48,000 | 1–2 |
  | pig | 30,000–36,000 | 2–4 |
  | rabbit | 12,000–18,000 | 2–5 |
  | wolf, fox, cat, ocelot | 24,000–30,000 | 1–3 |
  | panda, polar bear | 48,000–60,000 | 1–2 |
  | hoglin | 30,000–36,000 | 2–3 |

  Other species use the global range (default 24,000 ticks and one baby). Until Stage 2, every mother rolls with a neutral weight. Stage 2 adds a heritable **weight** trait: a heavier mother lands higher in the litter range and shorter in the pregnancy range.
- Growth time is inherited. Wild animals take 24,000 ticks (20 minutes). A baby averages its parents and can step toward the fast end. The range is 15,000 to 30,000 ticks.
- Edge cases:
  - Pregnant animal dies: the pregnancy is lost; no birth, no message.
  - Chunk unloaded: catch up on load.
  - Female is moved to another dimension.
- Visual cue: gold hearts drift up from a pregnant female. The hover card shows the portrait, sex, size letter, name, familiarity bar, feed bar, and vanilla health hearts. The Barnyard Inspector item opens the detailed dialog.

## 5. Food-driven pregnancy
- Per-species "good" and "bad" food lists in data files, using item tags (e.g. `#c:crops/wheat`) so modded foods work.
- Good food: temporary speed-up buff or one-time progress bonus. Bad food / starvation: slows progress.
- Defaults shipped for vanilla animals; datapacks can override.

## 6. Visuals (configurable, later milestones)
- **Textures:** renderer chooses `<animal>_male.png` / `<animal>_female.png` if present, else falls back to vanilla. Resource packs can supply them, no code needed.
- **Extra layers:** add-on render layers on top of vanilla model (horns, manes, wattles), toggled per species in config.
- **Custom models:** optional per-sex model swap, loaded from data/resource files (Blockbench-friendly) so artists can add them without Java.
- Visuals are client-side: players need the mod installed on servers.

## 7. Modded animal compatibility
- Anything extending vanilla `Animal` with standard breeding works automatically.
- Mods with custom breeding (e.g. Alex's Mobs) need small compat patches or are excluded via config.
- Ship compatibility presets (configs) for popular animal mods with sensible pregnancy times and good foods.

## 8. Config
- Global: enable mod, sex ratio, size defaults, pregnancy on/off, and the global `pregnancyTicksMin`/`pregnancyTicksMax` and `litterMin`/`litterMax`.
- Per species: enabled, male/female size range, `pregnancyTicksMin`/`pregnancyTicksMax`, `litterMin`/`litterMax`, good/bad foods, texture/layer/model toggles.
- Lookup order for each range end: the user's species entry, then the built-in preset, then the global value. An old single `pregnancyTicks` or `litterSize` key still works and sets both ends. An inverted pair is swapped; litters are clamped to 1–8 and pregnancies to 1–480,000 ticks.
- `tuning` block (defaults are today's behavior; bad values are clamped or fall back to the default):
  - `familiarityGain` (0.2), `familiarityThreshold` (0.3), `feedCooldownTicks` (24,000)
  - `growthFastTicks` (15,000), `growthSlowTicks` (30,000), `growthWildTicks` (24,000)
  - `improveBaseChance` (0.35), `improveNutritionChance` (0.45), `improveStep` (0.25), `inheritSpread` (0.05)
  - `fertilityCostPerBirth` (0.25), `fertilityRecoveryPerDay` (0.1), `fertilityMinToConceive` (0.25)
- Format: data-driven JSON per species + a main config file (use a cross-loader config lib, or a simple own JSON config in `common`).

## 9. Ideas (from brainstorming)
- **Inherited traits:** babies get size, color, milk/wool yield between parents with random variation.
- **Age stages:** baby → young → adult → old. Old animals breed less and produce less.
- **Fertility & cooldown:** fertility drops if bred too often (TFC "worn out").
- **Familiarity:** feed an animal its normal food once per Minecraft day for +20%, up to 100%. A click and a sneak-click are the same feed. Below 30% familiarity, only a sneak-click is accepted; a normal click scares the animal away and does not feed it. At 30%, feeding starts love mode, and milking, shearing, and breeding all open. A dispenser with shears follows the same rule. There is no decay. Horses, donkeys, mules, llamas, and camels only breed when tamed, so reaching 30% also tames them to the player who fed them; riding to tame still works.
- **Seasonal breeding:** optional integration with Serene Seasons (some species breed only in spring).
- **Herd book item:** right-click an animal to view sex, age, pregnancy progress, parents, traits.
- **Name tag lineage:** named animals keep a family tree viewable in the herd book.
- **Male behavior:** males get mildly territorial with too many males in one pen.
- **Product differences:** only females give milk (optionally only after giving birth); males give more meat/leather.
- **Compatibility profiles:** presets for popular animal mods.

## 10. Inspiration
Classic TerraFirmaCraft (and modern TFC 1.18/1.20): sexes, familiarity, pregnancy, aging, nutrition.
