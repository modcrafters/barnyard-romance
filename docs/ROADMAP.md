# Barnyard Romance: Roadmap

> **Active plan:** [`docs/superpowers/plans/2026-10-24-live-birth-roadmap.md`](superpowers/plans/2026-10-24-live-birth-roadmap.md) covers the remaining work for live-birth animals in stages. Stages 1 and 2 are done. Stages 3–8 are outlined; each gets a detailed plan when it starts.

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

## Next

### Stage 3: Age stages
- [ ] `bornTick` trait; unknown (-1) counts as adult.
- [ ] Baby, young (can't breed yet), adult, old.
- [ ] Old age lowers fertility recovery and product yield; nothing dies of old age.
- [ ] Inspector shows the stage word.

### Stage 4: Data-driven foods and superfoods
- [ ] Datapack food files: species, items, and effects (familiarity, heal, horse temper, pregnancy speed and progress, baby improvement, bad food).
- [ ] Vanilla defaults in the mod's own data, restoring horse healing and temper.
- [ ] Superfood items with recipes only when Pam's HarvestCraft 2 is loaded.
- [ ] A broken food file is skipped and logged; the rest still load.

### Stage 5: Products
- [ ] Lactation: milk only after a birth, drying up after three unmilked days once the young are grown; males never give milk.
- [ ] Drops scale with sex, size, and age for meat, leather, wool, and rabbit hide.
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
