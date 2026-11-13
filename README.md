# Barnyard Romance

A Minecraft Java mod for Fabric and NeoForge that adds male/female animals, sex-based size and visuals, realistic breeding rules, pregnancy, and food-driven pregnancy progress, inspired by classic TerraFirmaCraft, working for vanilla and (most) modded animals.

Animals get a saved sex and a sex-based `minecraft:scale`. Wild animals are only S or M. A baby takes the average of its parents and has a chance to come out one band larger, so L and XL come from breeding. The tooltip shows S, M, L, or XL. A small animal drops 2–3 meat and a large one drops 8–9. A wolf bites and lasts by its size: the smallest female bites for 1 with half a vanilla wolf's health, a wolf halfway between the sizes is a vanilla wolf, and the largest male bites for 8 with half again the health; damage taken is unchanged. Looking at an animal also shows the tooltip above it. A male and a female breed into a pregnancy instead of an instant baby; only the female falls in love, and the mod finds her a male (see [Rival males](#rival-males)). The default lasts one Minecraft day (20 real minutes) and the litter size is 1; both are set in `config/barnyard_romance.json`. Time skipped by sleeping or with `/time add` counts towards a pregnancy too, even for an animal that was unloaded at the time. Food-driven progress is not in yet.

The sizes come from `femaleScale`, `maleScale` and `scaleVariation` in `config/barnyard_romance.json`, 0.87, 1.13 and 0.1 on a new install; an existing install keeps the numbers already in its file until you edit them. When the numbers change, every animal keeps its size band and grows or shrinks to match.

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

Familiarity, from sneak-feeding, doesn't last on its own. Each day an animal goes without food it loses 5%, and each milking or shearing costs 5%, since animals don't much enjoy them. Feeding every day never loses anything. A female that went hungry yesterday gives no milk today, and like any day she isn't milked it counts towards her drying up. **Dairy Mash** (a bowl, grains, a root and any milk bucket; you get the bowl back) starts a dry, familiar female's milk for three days (`milkKeepsFlowingTicks`); milking keeps it going from there. A pack can give any food file `"startsMilk": true` in its effects to do the same as an ordinary meal. Babies don't go hungry while they're nursing.

Tamed wolves, cats and horses never go wild again. Below the familiarity threshold they're **grumpy**: a horse rears instead of letting you on, a cat or wolf won't sit or stand when told, and a grumpy wolf sits down and watches while you're attacked within 16 blocks (`sulkRange` in `tuning`). A grumpy pet still follows you and still fights back when it's attacked itself. Feed it back above the threshold to win it back. However a pet is tamed, it starts a week's hunger above the threshold, and so do pets tamed before this change.

Animals that shy away from players, such as rabbits or Naturalist's deer, stop running from you once they're familiar. Before that, they hold still if you sneak up holding food they eat.

The losses are `familiarityHungerLoss`, `familiarityMilkingCost` and `familiarityShearingCost` in the config's `tuning` block (0 turns one off).

### Wild-caught animals

An animal first won over as an adult stays a little wild. Livestock top out at 40% familiarity, and wolves, cats and the horse family, who live closer to you, at 80%. Animals first fed while they're young, including every one born to your own animals, can reach 100%. Which kind an animal is gets settled the first time it gains any familiarity, and never changes after. The card above it shows "Wild: max 40%" under the familiarity bar, and the herd book notes it on the animal's page.

Below 50% familiarity an animal sometimes won't be milked or sheared: one try in five, it refuses. That try is spent until midnight, and it costs no familiarity. Shears from a dispenser get the same answer. Wild-caught livestock never get past 40%, so they always carry that risk.

| Key (`tuning`) | Default | Meaning |
|---|---|---|
| `wildFamiliarityCap` | 0.4 | Highest familiarity for wild-caught livestock (1 turns the cap off) |
| `wildPetFamiliarityCap` | 0.8 | Highest familiarity for wild-caught wolves, cats and the horse family |
| `refusalBelow` | 0.5 | Below this familiarity, milking and shearing can be refused |
| `refusalChance` | 0.2 | Chance of a refusal per try below that line (0 turns refusals off) |

A cap below `familiarityThreshold` (0.3) means wild-caught animals can never breed or give milk or wool.

## Rival males

Only females fall in love. Feed a familiar female and she goes into love as before; feeding a male never does. She is then matched with a grown, familiar male of her kind within 16 blocks who can walk to her (a fence or a wall keeps him out), and only he is put in love, quietly, with no hearts. Horses still pair with donkeys for mules.

**Vigour** is a male's mating energy, shown on the card while you sneak, in the herd book and in Jade. A male fed yesterday starts the day at 100%, and one who missed yesterday is filled by his first meal today. Otherwise what he has left carries over. Each mating costs 30%, so a fed male can mate three times a day, and each fertilized egg costs a rooster 15%, about six eggs a day. A male who has eaten neither yesterday nor today can still mate on what he has left, but each mating also costs him a heart (half a heart per egg), and he won't mate at all if it would leave him with less than half a heart: nobody kills himself for love. Males get no breeding rest: Vigour is their only limit.

**Fights.** When two or more such males are around a female in love, the males are taken closest first, two at a time, and each pair has an even chance to fight. A pair that fights is out of it, and the next two get their turn. The first male left on his own, or the closer of a pair that doesn't fight, mates with her. So with three, two may fight while the third mates with her; with four, both pairs may fight and the winner who's first done gets her. Rivals butt heads until one of them dies, the bigger one hitting harder, and her love waits for the winner. Livestock fight: cows, mooshrooms, sheep, goats, pigs, rabbits and chickens, and Naturalist's deer, boar, ducks, turkeys and ostriches. Pets (wolves, cats, the horse family, llamas and camels) and everything else never do, unless a species entry says `"fights": true`.

- **Roosters** that can reach each other fight now and then without a hen, one day in four per pair, wild or yours. Other wild males fight too, one day in twenty per pair, but only when both are wild. Your own familiar males only fight over a female. Fights only happen near players, while the animals are loaded.
- **Stopping a fight:** walk up within 8 blocks with food either of them eats, in either hand, or take it out once you're close, and two familiar males stop and leave each other alone for an in-game hour, time to move one. Food you were already holding when the fight started doesn't count, so feeding a cow and standing by doesn't call off the fight over her; put it away and take it out again. A fight with a wild male can't be stopped.
- Nobody else is hurt, and the fighters ignore you unless you hit one. An animal killed in a fight is noted in the herd book as "Killed by a rival".

**Stud Mash** (two grains, a root, a berry and a gunpowder) gives a grown, familiar male back half his Vigour, up to 100%. Like the other superfoods it skips the once-a-day rule. It isn't a meal: it doesn't count as feeding him and doesn't heal him. A pack can give any food file `"vigour": 0.5` in its effects to do the same.

| Key (`tuning`) | Default | Meaning |
|---|---|---|
| `vigourPerMating` | 0.3 | Vigour one mating costs a male |
| `vigourPerEgg` | 0.15 | Vigour one fertilized egg costs a rooster |
| `hungryMatingHealth` | 2.0 | Health a mating costs a hungry male |
| `hungryEggHealth` | 1.0 | Health a fertilized egg costs a hungry rooster |
| `matingRange` | 16 | Blocks around a female in love where males count, and between roosters or wild males that fight on their own (older configs call it `rivalRange`, which still works) |
| `rivalFightChance` | 0.5 | Chance two males fight over a female (0 turns it off) |
| `roosterFightChance` | 0.25 | Chance a day that a pair of roosters fight |
| `wildFightChance` | 0.05 | Chance a day that a pair of wild males fight |
| `fightStopRange` | 8 | Blocks within which arriving with food in hand stops a fight between familiar males |
| `fightTruceTicks` | 1000 | Ticks two stopped fighters leave each other alone |

A species entry's `fights` (true or false) says whether its males fight at all.

## Egg layers

Chickens lay eggs instead of breeding. Seeds feed them and win familiarity like any other food, but never put them in love.

- **How many:** each day an adult hen lays 1 egg if she's S or M and 2 if she's L or XL, one more if she's familiar, and none on a day after she went hungry. She lays them all together at a random time between sunrise and sunset, and the count starts again at midnight.
- **Where:** when her eggs are due, a hen looks for a **Nesting Box** within 16 blocks that she can walk to, that no other hen is sitting in and that has room. She walks over and goes in for a quarter of an in-game hour, plus five minutes for each egg after the first, and leaves them all in the nests. Any that don't fit are left in front of the box. With no box, or while she's on a lead, she lays on the ground, like vanilla.
- **Fertilized eggs:** an egg laid in a box is a **Fertilized Egg** if an adult rooster within 16 blocks can walk to her and has the Vigour for it (15% an egg; see [Rival males](#rival-males)). The closest one is the father, and pays when she goes to the box. A spent rooster leaves the rest of her eggs plain. Eggs laid on the ground are always plain and cost him nothing.
- **Hatching:** a fertilized egg left in a box hatches after about a day (sooner from a heavier hen). The chick inherits from both parents like a calf and joins its mother's herd books. Time skipped by sleeping or with `/time add` counts too.
- **Wild eggs:** taking a fertilized egg out of its box, any way at all, makes it a **Wild Fertilized Egg**. It forgets its parents and its countdown, and stacks to 16 with other wild eggs of its kind. Put back in any box, it starts a fresh countdown, as if from an average hen, and hatches a chick sized like one met in the wild, with no parents and no herd book. The tooltip of an egg still in its box warns you.
- **Throwing:** plain eggs never hatch. A fertilized egg, wild or not, hatches one time in eight, into a small, light chick with no known parents.
- **Roosters** are bigger than hens and wear a red comb and big wattles. Hens have no comb.

The **Nesting Box** is planks in a U with wheat in the middle. Right-click it to see its sixteen nests: a fertilized egg has a nest to itself with its countdown underneath, plain eggs pile up to 16, and a sitting hen's nest is marked. You can take any egg out and put fertilized eggs back, but only hens add plain eggs. Hoppers underneath take plain eggs only. Breaking the box drops all its eggs, including the ones a sitting hen was about to leave, and lets her out.

Fertilized eggs in a box never stack, as each remembers its own parents; wild ones stack to 16. Both count as eggs in recipes, so a cake can use one.

In the herd book, each adult hen has an egg icon for tomorrow: speckled for fertilized eggs, plain for plain ones and black for none. Its tooltip, and her page, say how many and why they're not better: not fed today, no rooster nearby, the rooster hasn't eaten today, no nesting box with room nearby, or not familiar (one egg fewer). The rooster and the box are checked around her when the book last saw her.

Each species entry can have an `eggs` block: `enabled`, `plainEgg` (an item id), `hatchTicksMin`, `hatchTicksMax`, `smallPerDay`, `largePerDay`, `happyBonus`, `boxRange`, `roosterRange` and `thrownHatchChance`. Chickens lay by default. Another bird lays once its entry has `"eggs": { "enabled": true, "plainEgg": "somemod:duck_egg" }`.

## Feeding blocks

Five blocks feed your animals when you can't:

| Block | Feeds | Made from |
|---|---|---|
| **Bird Feeder** | chickens and parrots, and Naturalist's ducks and turkeys | a stick, seeds and planks |
| **Trough** | rabbits | wooden slabs |
| **Feeder** | pigs and goats, and Naturalist's boar | planks around a hay bale |
| **Manger** | cows, mooshrooms, sheep, the horse family, llamas and camels, and Naturalist's deer and ostriches | slabs and planks around a hay bale |
| **Pet Bowl** | wolves, cats and ocelots | bricks |

Each takes any ordinary food one of its animals eats, by hand, from a hopper or from pipes; superfoods are for feeding by hand. Its herd is the familiar animals of its kinds within 16 blocks (`feederRange`) that can walk to it. Each morning from sunrise they come over a few at a time, nearest first, and each eats one item: it stands a few seconds with its head down, crumbs at its mouth, then wanders off to make room. A pet told to sit within two blocks of its bowl eats where it sits.

A meal from a block is the animal's meal for the day. It heals, keeps hunger away and keeps milk, eggs and Vigour coming, but it never wins familiarity, puts an animal in love or does what a superfood does: that takes your hand. The herd book says which block fed an animal today, with a half drumstick in its list.

While you're away the block keeps feeding. When you come back, the days you missed are settled in order, the food it held shared evenly among its herd day by day, and the days it ran out count as hungry ones.

You can see how full a block is: its food rises and falls through four levels, full at two days' food for its herd (one item is one animal's meal). A comparator reads it too, as it reads a chest. All but the Bird Feeder face you when placed. The Manger and the Feeder are as tall as a fence to livestock, so nobody climbs onto them; you can step over a Pet Bowl, and a chicken may hop onto its feeder.

**Going hungry.** An animal you've won over loses half a heart for each day it goes without food, but never drops below half a heart, and loses 5% familiarity as before. One neglected down to under 10% familiarity may jump the fence one day, one chance in ten, and run off: its herd book page says it ran away, and you can win it back with food. Tamed pets never run off, and nor does an animal on a lead or being ridden.

| Key | Default | Meaning |
|---|---|---|
| `hunger.hurts` | true | Whether hungry animals lose health |
| `hunger.dailyDamage` | 1.0 | Health lost for each hungry day |
| `hunger.minHealth` | 1.0 | Hunger never takes an animal below this |
| `hunger.catchUp` | `EVERY_DAY` | What days away from a pen do: all of them count, `ONE_DAY` only one, or `NONE` |
| `tuning.feederRange` | 16 | Blocks a feeding block's herd can be from it |
| `tuning.escapeBelow` | 0.10 | Familiarity under which a neglected animal may run off |
| `tuning.escapeChance` | 0.10 | Chance a day that it does (0 turns it off) |
| `tuning.birdFeederFullDays`, `troughFullDays`, `feederFullDays`, `mangerFullDays`, `petBowlFullDays` | 2 | Days of food for its herd that show as full (1 to 365); only the look, not how much it holds |

A pack can move a species to another block with the entity tags `barnyard_romance:feeds_from/<block>`.

## Naturalist

With [Naturalist](https://modrinth.com/mod/naturalist) installed, five of its creatures count as livestock. They get sexes, sizes and familiarity, and males are only different in size.

| Creature | How young arrive |
| --- | --- |
| Deer, boar | Pregnancy, like cows and pigs |
| Duck, turkey | Eggs in a Nesting Box, like chickens |
| Ostrich | Naturalist's own eggs, but it takes a male and a female |

Ducks and ostriches tame through familiarity, like wolves and cats, instead of Naturalist's chance when feeding their young. Food doesn't breed ducks or turkeys. To raise more, you need a Nesting Box and a male nearby, as with chickens. Ducks lay duck eggs and turkeys lay ordinary eggs. A thrown duck egg still follows Naturalist's rules.

Naturalist's other creatures are left alone, including any it adds later. To include one, give it a species entry with `"enabled": true`. To leave one of the five out, list it in `exclude`.

## Leaving species out

Set `"enabled": false` in a species entry to leave one species vanilla. To leave out many, list them in `exclude`, by id or a whole mod: `"exclude": ["minecraft:parrot", "somemod:*"]`. A mod or data pack can add its animals to the entity tag `barnyard_romance:excluded` instead. An explicit `"enabled": true` in a species entry wins over both. Excluding never wipes what the mod saved on an animal, so turning a species back on brings it all back.

## Herd book

Craft a **Herd Book** from a book and a wheat, then use it on familiar animals to add them. Use it in the air to read it: it keeps a live list of your herd, from anywhere, with feeding, pregnancy, milk and family at a glance. Newborns join their mother's books by themselves.

**Copies:** a herd book with a herd, a book, a feather and an ink sac, anywhere in the crafting grid, make a copy, and the herd book stays in the grid. The copy is the same herd, not a snapshot: an animal added or removed with either book shows in both. Give one to someone you farm with, or keep a spare in a chest. Each book's tooltip shows its herd's mark, such as "Herd 3FA2C1", which its copies share. A new book with no animals yet has nothing to copy.

Sneak while looking at an animal to see all its stats on the card above it. Using the book on an animal that's already in it opens the book at that animal's page, which runs over several leaves for a long entry. With [Jade](https://modrinth.com/mod/jade) installed, the stats show in Jade's tooltip instead of the card.

**Herd Sticks:** `herdSticks` in `config/barnyard_romance.json` is `false` by default. Turned on, it shows the Herd Sticks in the creative tab and the `/barnyard familiarity` and `/barnyard pregnancy` commands that hand them out. Needs a restart.

## Translations

All of the mod's text is in `assets/barnyard_romance/lang/en_us.json`. To translate it, copy that file to your language's code (for example `de_de.json`) and translate the values. Leave the keys and the `%s` placeholders as they are. The file can go in the mod or in a resource pack.

## Versions

| Minecraft | Loader |
| --- | --- |
| **26.3** | Fabric (needs Fabric API) or NeoForge |
| **1.21.1** | NeoForge |

### 1.21.1 differences

- Pigs eat silently: their eating sound only came after 1.21.1.
- The card above an animal is placed with the FOV setting and the camera, so it doesn't follow view bobbing or zoom mods.
- The herd book has no head close-ups.

## Contributing

To build the mod or send a change, see [CONTRIBUTING.md](CONTRIBUTING.md).
