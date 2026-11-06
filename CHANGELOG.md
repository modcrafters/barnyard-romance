# Changelog

Each pull request adds its own bullets at the top of **Unreleased**, written for players. When a merge lands, the Discord message shows only the bullets that pull request added.

Push a version tag to publish. Alpha and beta releases use only **Unreleased**. Stable releases include those new notes under the tagged version, followed by every alpha/beta section since the previous stable release, newest first. The same notes go to CurseForge, Modrinth, and GitHub.

After publishing, the action prepares the next development version and archives the new notes under the released heading. It runs the full CI suite on a temporary branch before committing the update directly to **main**, without a follow-up PR. Earlier sections stay in place without duplication. A stable promotion may have no new notes; its empty heading still marks the next stable cutoff. Concurrent main changes are never overwritten, and unpublished notes changed since the tag are preserved for manual reconciliation.

## Unreleased

- Herd Sticks and their commands are off unless `herdSticks` is turned on in the config.
- The Barnyard Inspector is gone. Sneak while looking at an animal to see all its stats on the card above it, or read them in the herd book, which now opens at an animal's page when you use it on one in the book. With Jade installed, the stats show in Jade's tooltip instead.
- Only females fall in love now. Feed a familiar female and she's matched with a grown, familiar male nearby who can walk to her, so a fence keeps the next pen's bull out. Males get no breeding rest.
- Males have **Vigour**, shown on the card and in the herd book: full after a day they were fed, 30% for each mating and 15% for each egg a rooster fertilizes. A male who hasn't eaten since yesterday pays with health too, and won't mate if it would leave him under half a heart.
- Rival livestock males may fight over a female in love, butting heads until one dies, two at a time from the closest, while the first one left over quietly mates with her. Roosters fight now and then on their own, and wild males do too, rarely. Walking up with food in your hand stops a fight between two familiar males.
- **Stud Mash** (two grains, a root, a berry and a gunpowder) gives a male back half his Vigour.
- The herd book notes an animal killed in a fight.

## 0.0.2

- Animals that shy away from players, such as rabbits and Naturalist's deer, boar and turkeys, stop doing so once they're familiar with you. Before that, they hold still if you sneak up holding food they eat.

- With Naturalist installed, its deer, boar, ducks, turkeys and ostriches get sexes, sizes and familiarity. Its other creatures are left alone.
- Naturalist's deer and boar carry pregnancies. Its ducks and turkeys lay in nesting boxes and need a male nearby for eggs that hatch, the same as chickens. Its ostriches keep their own eggs.
- Naturalist's ducks and ostriches tame through familiarity, like wolves and cats.

- Cake made with sheep or goat milk, or with a fertilized egg, keeps working alongside other mods that change the cake recipe, such as Naturalist.

## 0.0.1-alpha

- Animals caught wild as adults never become fully familiar: livestock top out at 40%, and wolves, cats and horses at 80%. Animals raised from young, including wild babies you start feeding early, can still reach 100%.
- Below 50% familiarity, an animal refuses milking or shearing one time in five. That try is gone until tomorrow, but it costs nothing.
- The inspector and the herd book mark wild-caught animals and show how familiar they can get.

- An egg that can't hatch, say because its animal's mod was removed, stays in its nest instead of disappearing.
- A chick that hatched in a box while no one was nearby is as old as it would be if you had watched it hatch.
- The inspector counts only the eggs a hen really laid, not the ones she missed while her chunk was unloaded.

- A newly tamed wolf, cat or horse can go a week without food before it turns grumpy, instead of turning grumpy after its first missed meal. Pets from before familiarity faded get the same week.
- A grumpy wolf fights back properly when it's attacked, even while you're being attacked too, and only sulks when you're hurt nearby.
- Grumpy wolves and cats follow you again, unless they were told to sit. They're grumpy, not wild.
- An animal showing 30% familiarity is always familiar; some used to count as just under.
- Dairy Mash's tooltip gives the number of days from the config, and mod packs can give any food the power to start milk.

- Chickens lay eggs instead of breeding: S and M hens lay one a day, L and XL hens two, one more when they're familiar, and none after a hungry day. Seeds feed chickens but no longer breed them.
- New Nesting Box (planks round a wheat): hens walk to one to lay, and you can see a hen sitting in it through the hole. Its window shows sixteen straw nests with each egg's countdown.
- A rooster nearby makes the eggs laid in a box fertile. Fertilized eggs hatch in the box after about a day into chicks that take after both parents and join their mother's herd books. Eggs laid on the ground are always plain.
- Thrown eggs: plain ones never hatch, and fertilized ones sometimes hatch a small chick with no known parents.
- Roosters are bigger than hens and wear a red comb and big wattles.
- The inspector shows a hen's eggs for the day, and whether a rooster is fertile.
- The herd book shows what each hen will lay tomorrow (fertilized, plain or none) and why.

- Familiarity fades: an animal loses some each day it isn't fed, and a little each time it's milked or sheared. Feeding every day keeps it. A cow that went hungry gives no milk the next day.
- New Dairy Mash: feed it to a dry cow, goat or sheep and she gives milk for three days, without having to breed her first.
- Sheep and goat milk buckets look like the vanilla milk bucket with creamier or whiter milk, and the vanilla one is now called Bucket of Cow Milk.
- Tamed wolves, cats and horses never go wild, but if they lose too much familiarity they turn grumpy: horses won't be ridden, cats and wolves won't sit or follow, and a grumpy wolf just watches when you're attacked. Feed them to win them back.
- Mod packs can leave species out in bulk with the config's `exclude` list (`"somemod:*"` for a whole mod) or the `barnyard_romance:excluded` entity tag.
- Calves from vanilla breeding (with pregnancy turned off) remember their parents and join their mother's herd books.
- The herd book keeps its animals in the order they joined after a reload, no longer reopens itself after you close it, and dates deaths by the in-game day.

- Wolves fight by their size, wild and tamed: a small female bites like a stick with half the health, a big male bites like a netherite sword with half again the health. A big wolf gets all of its extra health when tamed.
- Using the herd book on a tamed cat or wolf adds it to the book instead of making it sit or stand up.
- The mouse wheel, touchpad and Page Up/Page Down turn the herd book's pages: down for the next page or animal, up for the one before. A touchpad swipe turns one page at a time instead of racing through the book.
- Sheep can be sheared once a day, like milking, and still need to eat grass to grow their wool back. The herd book shows which ones are ready to shear.
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
