# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

To release, rename **Unreleased** to the version (for example `## 0.2.0`), use that section as the CurseForge changelog, and start a new empty **Unreleased** above it.

## Unreleased

- The herd book shows a close-up of each animal's head, hunger-bar style icons for feeding, pregnancy and milk, and paper bookmarks down both sides that fit the species' name.
- Newborn animals remember their mother and father.
- New Herd Book (a book and a wheat): use it on familiar animals to keep a live list of your herd, with feeding, pregnancy and milk at a glance, family links, and calves added automatically.
- All of the mod's text can be translated: the inspector, chat and action-bar messages, commands, herd sticks and tag names all come from the lang file. Add a translation as `assets/barnyard_romance/lang/<language>.json`, in the mod or in a resource pack.
- Pregnant females get wider as the birth gets closer. Set `bellyStart` and `bellyMaxWiden` in the config, globally or per species.
- Bulls, rams and billy goats have horns. Some cows, ewes and nanny goats have small ones. Babies have none, and horns grow in while an animal is young.
- Resource packs can reshape horns or give modded animals horns, using files in `assets/<namespace>/barnyard_horns/`.
- New client config `barnyard_romance-client.json` turns the belly and the horns off.
- Builds for NeoForge 1.21.1, as well as Fabric and NeoForge 26.3.
- Cows, sheep and goats give milk only after giving birth, and dry up if nobody milks them. Large females can be milked twice a day.
- Sheep and goats give their own milk buckets, and cake takes any of them.
- Meat, leather, wool and shearing yields depend on an animal's sex, size and age.
- Feeding tames wolves and cats and earns an ocelot's trust: wolves eat meat, cats and ocelots eat fish.
- Diets and foods are data pack files. Each species eats what its diet allows and refuses the rest, and food heals hurt animals.
- Breeder's Mash and Golden Bran help a pregnant mother. Their recipes use item tags, so mod packs can swap in other crops.
- `/barnyard foods scan` builds ingredient tags from the recipe list for mod packs.
- Animals go from baby to young, adult and old, and in the end die of old age. All of these lengths can be set in the config.
- Babies take after both parents in size, growth and weight. Mothers lose some fertility with each birth and get it back over time.
- Pregnancy length and litter size are ranges, with presets for each vanilla species.
- Every animal is male or female with a matching size. Only a male and a female can breed, and the female carries a pregnancy instead of making an instant baby.
- Sneak-feed an animal to build familiarity. Breeding, milking and shearing need it.
- The Barnyard Inspector shows an animal's sex, size, age, weight, fertility, familiarity, love, pregnancy and milk. Looking at an animal shows a short card above it.
