# Barnyard Romance

A Minecraft Java mod (Fabric + NeoForge, one shared codebase) that adds male/female animals, sex-based size and visuals, realistic breeding rules, pregnancy, and food-driven pregnancy progress, inspired by classic TerraFirmaCraft, working for vanilla and (most) modded animals.

Animals get a saved sex and a sex-based `minecraft:scale`. Wild animals are only S or M. A baby takes the average of its parents and has a chance to come out one band larger, so L and XL come from breeding. The tooltip shows S, M, L, or XL. A small animal drops 2–3 meat and a large one drops 8–9. A wolf bites and lasts by its size: the smallest female bites for 1 with half a vanilla wolf's health, a wolf halfway between the sizes is a vanilla wolf, and the largest male bites for 8 with half again the health; damage taken is unchanged. `/barnyard info`, while you look at an animal, still includes the exact scale. Looking at an animal also shows the tooltip above it. A male and a female breed into a pregnancy instead of an instant baby. The default lasts one Minecraft day (20 real minutes) and the litter size is 1; both are set in `config/barnyard_romance.json`. Time skipped by sleeping or with `/time add` counts towards a pregnancy too, even for an animal that was unloaded at the time. Food-driven progress is not in yet.

## Bellies and horns

These change only how animals are drawn, on the client. Nothing new is saved or sent.

- **Pregnant belly:** a pregnant female gets wider as her pregnancy goes on. Her height, length and hitbox stay the same. She starts to widen at `bellyStart` (default 0.4, so 40% of the way through) and ends `bellyMaxWiden` wider (default 0.15). Both keys go in `config/barnyard_romance.json`, either at the top level or in a species entry.
- **Horns:** rams, bulls and billy goats have horns. Ewes, cows and nanny goats sometimes have small ones. Babies have none. Horns appear when an animal turns young and grow until it is an adult. Vanilla's own cow and goat horns are hidden on animals the mod draws horns for.
- **Switches:** `config/barnyard_romance-client.json` has `belly` and `horns`, both `true` by default. Turning `horns` off brings back exactly what vanilla draws. Changes need a restart.

### Horn files

Horn shapes are resource pack files at `assets/<namespace>/barnyard_horns/<name>.json`. The mod ships `cattle.json` (cows and mooshrooms), `sheep.json` and `goat.json`. To change one, put a file with the same namespace and name in a resource pack. To give a modded animal horns, add a new file that names it. The animal also needs to be tracked by the mod, so it must have a sex.

```json
{
  "entities": ["examplemod:yak"],
  "attach": "head",
  "offset": [0, 0, 0],
  "texture": "examplemod:textures/entity/yak_horns.png",
  "texture_size": [32, 32],
  "female_chance": 0.5,
  "hide": ["head/horns", { "part": "head", "size": [1, 3, 1] }],
  "male": [
    {
      "pivot": [-4, -3, -4], "rotation": [0, 0, 10], "side": "right", "mirror": true,
      "cubes": [ { "from": [-4, -1, -1], "size": [4, 2, 2], "uv": [0, 0], "inflate": 0 } ],
      "children": [ { "pivot": [-4, 0, 0], "rotation": [0, -25, 25], "cubes": [ { "from": [-3, -1, -1], "size": [3, 2, 2], "uv": [0, 4] } ] } ]
    }
  ],
  "female": []
}
```

- Every number is in model pixels or degrees, like vanilla model code and Blockbench.
- `attach` is the model part the horns hang from, as a `/`-separated path from the model's root. `offset` moves the horns' centre, for heads that are not centred on x = 0 (a goat's head is at −0.5).
- A part has a `pivot` and a `rotation` (applied Z, then Y, then X), cubes placed relative to the pivot, and optional `children` placed relative to it. Each horn part grows from its pivot.
- `mirror: true` adds the same part mirrored onto the other side, so each pair is written once. `side` (`left` is +X, the animal's own left) lets a goat that has lost one horn skip that side.
- A female has horns when a fixed random number from her UUID is below `female_chance`, so she keeps the same horns for life.
- `hide` lists vanilla horns to hide whenever the mod draws this animal's horns. It takes either a part path or the cubes of one part matching `size` (and `from`, if given). Entries a model doesn't have are ignored, so one list can cover every variant on every version.
- Models built some other way, such as GeckoLib, can't take horns from these files. A file whose `attach` part is missing logs one warning and draws nothing.

## Looking after animals

Familiarity, from sneak-feeding, doesn't last on its own. Each day an animal goes without food it loses 5%, and each milking or shearing costs 5%, since animals don't much enjoy them. Feeding every day never loses anything. A female that went hungry yesterday gives no milk today, and like any day she isn't milked it counts towards her drying up. **Dairy Mash** (a bowl, grains, a root and any milk bucket; you get the bowl back) starts a dry, familiar female's milk for three days; milking keeps it going from there. Babies don't go hungry while they're nursing.

Tamed wolves, cats and horses never go wild again. Below the familiarity threshold they're **grumpy**: a horse rears instead of letting you on, a cat or wolf won't sit or stand when told and wanders instead of following you, and a grumpy wolf sits down and watches while you're attacked. Feed it back above the threshold to win it back. Pets tamed before this change start at the threshold.

The losses are `familiarityHungerLoss`, `familiarityMilkingCost` and `familiarityShearingCost` in the config's `tuning` block (0 turns one off).

## Egg layers

Chickens lay eggs instead of breeding. Seeds feed them and win familiarity like any other food, but never put them in love.

- **How many:** each day an adult hen lays 1 egg if she's S or M and 2 if she's L or XL, one more if she's familiar, and none on a day after she went hungry. The eggs come at random times between sunrise and sunset, and the count starts again at midnight.
- **Where:** when an egg is due, a hen looks for a **Nesting Box** within 16 blocks that she can walk to, that no other hen is sitting in and that has room. She walks over, goes in for an in-game hour, and leaves the egg in a nest. With no box, or while she's on a lead, she lays on the ground, like vanilla.
- **Fertilized eggs:** an egg laid in a box is a **Fertilized Egg** if an adult rooster within 16 blocks wasn't hungry yesterday; the closest one is the father. Eggs laid on the ground are always plain.
- **Hatching:** a fertilized egg left in a box hatches after about a day (sooner from a heavier hen). The chick inherits from both parents like a calf and joins its mother's herd books. Taking the egg out pauses its countdown; putting it back in any box carries on. Time skipped by sleeping or with `/time add` counts too.
- **Throwing:** plain eggs never hatch. A fertilized egg hatches one time in eight, into a small, light chick with no known parents.
- **Roosters** are bigger than hens and wear a red comb and big wattles. Hens have no comb.

The **Nesting Box** is planks in a U with wheat in the middle. Right-click it to see its ten nests: a fertilized egg has a nest to itself with its countdown underneath, plain eggs pile up to 16, and a sitting hen's nest is marked. You can take any egg out and put fertilized eggs back, but only hens add plain eggs. Hoppers underneath take plain eggs only. Breaking the box drops all its eggs, including the one a sitting hen was about to leave, and lets her out.

Fertilized eggs never stack. They count as eggs in recipes, so a cake can use one.

In the herd book, each adult hen has an egg icon for tomorrow: speckled for fertilized eggs, plain for plain ones and black for none. Its tooltip, and her page, say how many and why they're not better: not fed today, no rooster nearby, the rooster hasn't eaten today, no nesting box with room nearby, or not familiar (one egg fewer). The rooster and the box are checked around her when the book last saw her.

Each species entry can have an `eggs` block: `enabled`, `plainEgg` (an item id), `hatchTicksMin`, `hatchTicksMax`, `smallPerDay`, `largePerDay`, `happyBonus`, `boxRange`, `roosterRange` and `thrownHatchChance`. Chickens lay by default. Another bird lays once its entry has `"eggs": { "enabled": true, "plainEgg": "somemod:duck_egg" }`.

## Leaving species out

Set `"enabled": false` in a species entry to leave one species vanilla. To leave out many, list them in `exclude`, by id or a whole mod: `"exclude": ["minecraft:parrot", "somemod:*"]`. A mod or data pack can add its animals to the entity tag `barnyard_romance:excluded` instead. An explicit `"enabled": true` in a species entry wins over both. Excluding never wipes what the mod saved on an animal, so turning a species back on brings it all back.

## Herd book

Craft a **Herd Book** from a book and a wheat, then use it on familiar animals to add them. Use it in the air to read it: it keeps a live list of your herd, from anywhere, with feeding, pregnancy, milk and family at a glance. Newborns join their mother's books by themselves.

## Translations

All of the mod's text is in `assets/barnyard_romance/lang/en_us.json`. To translate it, copy that file to your language's code (for example `de_de.json`) and translate the values. Leave the keys and the `%s` placeholders as they are. The file can go in the mod or in a resource pack. `LangFileTest` fails if the code uses a key that `en_us.json` doesn't have, or if `en_us.json` has a key nothing uses.

## Versions

One codebase builds three jars:

| Minecraft | Loader | Java | Loader version |
| --- | --- | --- | --- |
| **26.3** (latest stable release with both Fabric and NeoForge) | Fabric | 25 | Fabric Loader 0.19.5, Fabric API 0.162.0+26.3 |
| **26.3** | NeoForge | 25 | NeoForge 26.3.0.76 |
| **1.21.1** (where most modpacks are) | NeoForge | 21 | NeoForge 21.1.265 |

Shared code keeps to Java 21. Every change must build and pass on all three before it can merge.

## Import and run in IntelliJ IDEA

IDEA 2025.2 or newer is required for Java 25.

1. Open this folder (the one that contains `gradlew.bat`) as a project.
2. Set the Project SDK and the Gradle JVM to Java 25:
   - `File > Project Structure > Project SDK`
   - `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`
3. Install the **Stonecutter Dev** plugin (optional; it highlights the `//?` version comments).
4. Refresh the Gradle project.
5. Run one of these configurations:
   - **Fabric Client** or **Fabric Server** (Gradle run configurations)
   - **NeoForge Client** or **NeoForge Server** (Application run configurations)

### How the build is laid out

The build uses [Stonecutter](https://stonecutter.kikugie.dev) so one codebase can build for several Minecraft versions:

- The common code is in `src/`. Fabric code is in `fabric/src`, and NeoForge code is in `neoforge/src`.
- Each Minecraft version is its own Gradle project: `:26.3` (common), `:fabric:26.3` and `:neoforge:26.3`. Each one's `gradle.properties` under `versions/<mc>` holds that version's Minecraft, Java and loader versions.
- Code that differs between versions is marked with comments such as `//? if >=1.21.6 {`. The IDE always shows one version, the *active* one.
- Switch with the Gradle task `Set active project to <mc>`. Run `Reset active project` before you commit: git must always hold the 26.3 state, and CI checks this.

`gradlew collectJars` builds every loader jar and copies them to `build/libs/<mod version>/`. Each jar's own build output is under `<loader>/versions/<mc>/build/libs`. The common jars are not mods you install.

## Tests

- `gradlew :26.3:test` runs the unit tests for the pure rules.
- `gradlew :fabric:26.3:runGametest` and `gradlew :neoforge:26.3:runGameTestServer` start a headless game test server, breed real animals in a small pen, and exit non-zero if any check fails. The shared checks live in `src/.../gametest/`. NeoForge adds three more for `BabyEntitySpawnEvent`. Fabric writes a JUnit report to `fabric/versions/26.3/build/gametest/junit.xml`.
- `gradlew :fabric:26.3:runClientGametest` opens a real client and takes screenshots of the inspector, of rows of animals showing horns and bellies, of the herd book's pages, and of a nesting box with a hen inside, its window, and a rooster beside a hen. It also clicks through the book: an animal's row, the return button, the mouse wheel, Page Up/Page Down, and remove.
- On 1.21.1 the same checks run with `gradlew :1.21.1:test` and `gradlew :neoforge:1.21.1:runGameTestServer` (NeoForge only). There each test runs in its own batch, and `GameTestCompat` covers the steps that differ, such as test positions counting from one block lower.
- CI (`.github/workflows/ci.yml`) runs all of these on every pull request. The `all-versions` check passes only when every version passed; it is the check for a ruleset on `main` to require.

### 1.21.1 differences

- Pigs eat silently: their eating sound only came after 1.21.1.
- The card above an animal is placed with the FOV setting and the camera, so it doesn't follow view bobbing or zoom mods.

### 1.21.1 test gaps

- Every shared game test runs on 1.21.1.
- The Fabric client game tests (`InspectorClientGameTest`, `LooksClientGameTest`, `HerdBookClientGameTest`, `NestingBoxClientGameTest`) run on 26.3 only, because there is no Fabric build for 1.21.1. On 1.21.1 the inspector, herd card, tooltip, bellies, horns and herd book are checked by hand. On 1.21.1 the herd book has no head close-ups, as designed. For the herd book, check:
  - using the book in the air opens it, and an empty book says how to add animals;
  - a familiar cow can be added, and a wild one is refused;
  - after breeding, the calf joins the book under Kids;
  - after the mother dies, the calf's Mother link still opens her page;
  - ✕ asks "Sure?" and then removes the animal;
  - the mouse wheel and Page Up/Page Down turn pages, on the contents and on an animal's page.

  For grumpy pets, let a tamed one go unfed (`/time add 24000` once per day) until the inspector shows "· Grumpy", then check on both versions:
  - a grumpy horse rears and won't let you on;
  - a grumpy wolf won't sit, and sits down to watch while a zombie attacks you;
  - the herd book says "Grumpy: won't take orders", and feeding it back above 30% ends it.

  For horns and bellies, check:
  - a ewe or cow late in pregnancy is wider;
  - bulls, rams and billies have horns;
  - a billy that has lost a horn shows only one;
  - calves and lambs have no horns;
  - hornless cows and nannies show no vanilla horns either;
  - with `horns` off, vanilla's horns come back.
  For the nesting box, check:
  - the box shows its round hole and hay, and a hen sitting in it shows through the hole;
  - its window shows the straw nests, egg countdowns, plain-egg counts and the sitting hen's nest;
  - roosters have a comb and big wattles, and hens don't.
