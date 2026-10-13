# Barnyard Romance: Roadmap

## Milestone 1: Skeleton + Sex + Size
- [ ] Multi-loader Gradle project (common / fabric / neoforge), IntelliJ-ready, run configs for both loaders.
- [ ] Persistent synced `sex` attachment on all `Animal`s, random on spawn, lazy for old worlds.
- [ ] Apply `minecraft:scale` by sex with small random variation; per-species config.
- [ ] Debug command: `/barnyard info` on the looked-at animal (sex, scale).

## Milestone 2: Breeding rules + Herd book
- [ ] Only male + female can mate (mixin in common).
- [ ] Per-species enable/exclude list.
- [ ] Herd book item showing sex, age, scale.

## Milestone 3: Pregnancy
- [ ] Pregnancy state, progress, catch-up after unload, birth, litter size.
- [ ] Father info stored on mother.
- [ ] Optional visual size bump.

## Milestone 4: Food-driven pregnancy
- [ ] Data-driven good/bad foods with item tags.
- [ ] Progress buffs and penalties; vanilla defaults.

## Milestone 5: Visuals
- [ ] Per-sex textures with vanilla fallback.
- [ ] Configurable render layers (horns, manes, wattles).
- [ ] Optional per-sex custom models (Blockbench).

## Milestone 6: Depth
- [ ] Inherited traits, age stages, fertility, familiarity.
- [ ] Product differences (milk/meat).
- [ ] Seasons integration, lineage, male territorial behavior.
- [ ] Compatibility presets for popular animal mods.
