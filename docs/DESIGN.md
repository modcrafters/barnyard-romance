# Barnyard Romance: Design

## 1. Sex
- Every breedable animal (`Animal` subclass, vanilla + modded) gets a persistent `sex` (MALE/FEMALE), assigned randomly on first spawn (configurable ratio, default 50/50).
- Stored via data attachment (Fabric Data Attachment API / NeoForge attachments), saved to NBT, synced to clients.
- Existing animals in old worlds get a sex assigned lazily the first time they load.

## 2. Size (Milestone 1)
- Uses vanilla `minecraft:scale` attribute (1.20.5+), so hitbox, rendering and riding follow automatically. No new models needed.
- Defaults: female ~0.90, male ~1.10, plus small random variation (e.g. ±0.05). Per-species overrides in config.
- Babies: once grown, they use their own sex/size roll (later: inherited from parents, see Traits).
- Test in vanilla: `/attribute @e[type=cow,limit=1,sort=nearest] minecraft:scale base set 1.15`

## 3. Breeding rules
- Hook vanilla breeding (`Animal#canMate` / love mode) via mixin: only MALE + FEMALE of the same species can mate.
- Optional: cooldowns, fertility (see Ideas).
- Config: per-species enable/disable; species with custom breeding code can be excluded or patched with compat.

## 4. Pregnancy
- On successful mating, cancel instant baby spawn. Mark the female pregnant and store: progress (0-100%), father species, father traits, litter size.
- Progress ticks periodically (not every tick; e.g. every N ticks or on day time), so it's cheap and survives chunk unload (compute elapsed game time on load).
- At 100% the female gives birth (litter size configurable per species).
- Edge cases: pregnant animal dies (no birth), chunk unloaded (catch up on load), female is moved to another dimension.
- Visual cue: optional slight size bump on the female, plus herd book / tooltip info.

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
- Global: enable mod, sex ratio, size defaults, pregnancy on/off.
- Per species: enabled, male/female size range, pregnancy duration, litter size, good/bad foods, texture/layer/model toggles.
- Format: data-driven JSON per species + a main config file (use a cross-loader config lib, or a simple own JSON config in `common`).

## 9. Ideas (from brainstorming)
- **Inherited traits:** babies get size, color, milk/wool yield between parents with random variation.
- **Age stages:** baby → young → adult → old. Old animals breed less and produce less.
- **Fertility & cooldown:** fertility drops if bred too often (TFC "worn out").
- **Familiarity:** feeding/petting builds trust; neglected or wild animals won't breed and may flee.
- **Seasonal breeding:** optional integration with Serene Seasons (some species breed only in spring).
- **Herd book item:** right-click an animal to view sex, age, pregnancy progress, parents, traits.
- **Name tag lineage:** named animals keep a family tree viewable in the herd book.
- **Male behavior:** males get mildly territorial with too many males in one pen.
- **Product differences:** only females give milk (optionally only after giving birth); males give more meat/leather.
- **Compatibility profiles:** presets for popular animal mods.

## 10. Inspiration
Classic TerraFirmaCraft (and modern TFC 1.18/1.20): sexes, familiarity, pregnancy, aging, nutrition.
