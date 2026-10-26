# Barnyard Romance: Roadmap

> **Active plan:** [`docs/superpowers/plans/2026-10-24-live-birth-roadmap.md`](superpowers/plans/2026-10-24-live-birth-roadmap.md) covers the remaining work for live-birth animals in stages. Stages 1–3 are done. Stages 4–8 are outlined; each gets a detailed plan when it starts.

## Done

### Milestone 1: Skeleton + Sex + Size
- [x] Multi-loader Gradle project (common / fabric / neoforge), IntelliJ-ready, run configs for both loaders.
- [x] Persistent synced `sex` attachment on all `Animal`s, random on spawn, lazy for old worlds.
- [x] Apply `minecraft:scale` by sex with small random variation; per-species config.
- [x] Debug command: `/barnyard info` gives the Barnyard Inspector; use it on an animal to open the detail dialog.

### Milestones 2–3: Breeding rules + Pregnancy
- [x] Only male + female can mate (mixin in common).
- [x] Pregnancy state, progress, catch-up after unload, birth, litter size.
- [x] Father info stored on mother.

### Milestone 6 (part): Depth
- [x] Familiarity from sneak-feeding. It gates love mode, breeding, milking, and shearing.

### Stage 1: Quick wins
- [x] Every tuning number (familiarity, growth, offspring improvement) lives in the config's `tuning` block.
- [x] Pregnancy length and litter size are min–max ranges, with presets per vanilla species.
- [x] NeoForge `BabyEntitySpawnEvent` fires at conception; cancelling it leaves no pregnancy.
- [x] Love mode and the breeding rest are synced, so the inspector shows "In love" and "Resting".
- [x] A pregnant animal that dies loses the pregnancy (documented).

### Stage 2: Inherited traits, weight, fertility
Plan: [`2026-10-25-live-birth-stage-2.md`](superpowers/plans/2026-10-25-live-birth-stage-2.md)

- [x] `weight` trait (0–1): a heavier mother picks higher in the litter range and shorter in the pregnancy range.
- [x] `fertility` (0–1, starts at 1), a state that recovers with the clock rather than an inherited trait.
- [x] One inheritance rule for every trait (`Inheritance.child`), folding in `Growth.child` and `OffspringScale.childScale`.
- [x] Father's traits (scale, growth, weight) stored in the pregnancy at conception.
- [x] Fertility cost per birth, daily recovery, and a minimum to conceive, all in `tuning`.
- [x] Inspector: weight meter, a fertility meter for adult females, and "Worn out" under breeding.
- [x] Old worlds: missing weight is 0.5, missing fertility is 1.0.

### Stage 3: Age stages
Plan: [`2026-10-25-live-birth-stage-3.md`](superpowers/plans/2026-10-25-live-birth-stage-3.md)

- [x] `bornTick` trait; unknown (-1) counts as adult forever and never dies.
- [x] Baby, young (can't breed yet), adult, old; `youngTicks`, `oldAfterTicks` and `deathAfterTicks` globally and per species.
- [x] Old age halves fertility recovery (`oldFertilityFactor`).
- [x] Animals die of old age after `deathAfterTicks` (default 60 days, ±10%; 0 turns it off), with their own `old_age` damage type.
- [x] Faint, rare dark hearts float over old animals.
- [x] Inspector shows the stage word, and "Too young" under breeding.

### Stage 4: Diets, food files and superfoods
Plan: [`2026-10-26-live-birth-stage-4.md`](superpowers/plans/2026-10-26-live-birth-stage-4.md)

- [x] Reusable diet files: the species each one covers, what it eats (with factors) and what it refuses. A species' config entry can pick another diet with `diet`.
- [x] Datapack food files: species or diets, items, and effects (familiarity, heal, pregnancy speed and progress, baby improvement). A bad food is a negative `pregnancyProgress`.
- [x] Food heals: half a player food's hunger points, or 1 for other food. Horses, llamas and camels keep vanilla's heal values. A hurt animal eats any time, but then only heals.
- [x] Animals follow a player holding any food they eat.
- [x] Breeder's Mash and Golden Bran, with vanilla recipes built from the mod's own item tags so mod packs can swap in other crops. They skip the once-a-day rule and only help a pregnant mother.
- [x] `barnyard_romance:contains_meat` (fish included), refused by grazers, herbivores, pandas and hoglins.
- [x] `/barnyard foods scan` writes ingredient tags from the recipe graph for mod packs.
- [x] A broken diet or food file is skipped and logged; the rest still load.

## Next

### Stage 5: Products
- [ ] Lactation: milk only after a birth, drying up after three unmilked days once the young are grown; males never give milk.
- [ ] Drops scale with sex, size, and age for meat, leather, wool, and rabbit hide (old age adds `oldYieldFactor`, moved here from Stage 3).
- [ ] Inspector shows "Milk: 2d left".

### Stage 6: Pregnancy belly
- [ ] Client-only render tweak that widens a pregnant female on its local X axis only.
- [ ] Per-species settings and a client toggle.
- [ ] First pass widens the whole model; body-only widening as a follow-up if the head looks wrong.

### Stage 7: Herd book and lineage
- [ ] Brainstorm the open questions with the user first (per player or per item, sharing, live or recorded data).
- [ ] Lineage traits: mother and father UUIDs and names, stored at birth.
- [ ] Herd book item: register animals, list them, show traits and a two-generation family tree.

### Stage 8: Translations
- [ ] Every literal text becomes a translation key in `en_us.json`.
- [ ] Pure text helpers return key + argument records, tested by key.

## Later
- [ ] Per-species enable/exclude list (for mods with custom breeding).
- [ ] Per-sex textures with vanilla fallback.
- [ ] Configurable render layers (horns, manes, wattles).
- [ ] Optional per-sex custom models (Blockbench).
- [ ] Seasons integration.
- [ ] Territorial males.
- [ ] Compatibility presets for popular animal mods.
- [ ] Egg layers (chickens and friends).
