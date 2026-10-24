# Barnyard Romance: Roadmap

> **Active plan:** [`docs/superpowers/plans/2026-10-24-live-birth-roadmap.md`](superpowers/plans/2026-10-24-live-birth-roadmap.md) covers the remaining work for live-birth animals in stages. Stage 1 is detailed and ready to run. Stages 2–8 are outlined.

## Milestone 1: Skeleton + Sex + Size
- [x] Multi-loader Gradle project (common / fabric / neoforge), IntelliJ-ready, run configs for both loaders.
- [x] Persistent synced `sex` attachment on all `Animal`s, random on spawn, lazy for old worlds.
- [x] Apply `minecraft:scale` by sex with small random variation; per-species config.
- [x] Debug command: `/barnyard info` gives the Barnyard Inspector; use it on an animal to open the detail dialog.

## Milestone 2: Breeding rules + Herd book
- [x] Only male + female can mate (mixin in common).
- [ ] Per-species enable/exclude list.
- [ ] Herd book item showing sex, age, scale.

## Milestone 3: Pregnancy
- [x] Pregnancy state, progress, catch-up after unload, birth, litter size.
- [x] Father info stored on mother.
- [ ] Optional visual size bump.

## Milestone 4: Food-driven pregnancy
- [ ] Data-driven good/bad foods with item tags.
- [ ] Progress buffs and penalties; vanilla defaults.

## Milestone 5: Visuals
- [ ] Per-sex textures with vanilla fallback.
- [ ] Configurable render layers (horns, manes, wattles).
- [ ] Optional per-sex custom models (Blockbench).

## Milestone 6: Depth
- [x] Familiarity from sneak-feeding. It gates love mode, breeding, milking, and shearing.
- [ ] Inherited traits, age stages, fertility.
- [ ] Product differences (milk/meat).
- [ ] Seasons integration, lineage, male territorial behavior.
- [ ] Compatibility presets for popular animal mods.
