package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvents;
import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.modcrafters.minecraft.barnyardromance.api.AnimalInfo;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardApi;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardPails;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.modcrafters.minecraft.barnyardromance.api.event.Befriended;
import net.modcrafters.minecraft.barnyardromance.api.event.Born;
import net.modcrafters.minecraft.barnyardromance.api.event.Hatched;
import net.modcrafters.minecraft.barnyardromance.core.age.Growth;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.eggs.EggData;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsComponents;
import net.modcrafters.minecraft.barnyardromance.eggs.FertilizedEggItem;
import net.modcrafters.minecraft.barnyardromance.eggs.NestingBoxBlockEntity;
import net.modcrafters.minecraft.barnyardromance.eggs.WildEggs;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
import net.modcrafters.minecraft.barnyardromance.api.event.FamiliarityChange;
import net.modcrafters.minecraft.barnyardromance.api.event.Taming;
import net.modcrafters.minecraft.barnyardromance.familiarity.Familiarity;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.herd.HerdStick;
import net.modcrafters.minecraft.barnyardromance.herd.HerdTool;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.api.event.Milking;
import net.modcrafters.minecraft.barnyardromance.api.event.ShearYield;
import net.modcrafters.minecraft.barnyardromance.core.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;
import net.modcrafters.minecraft.barnyardromance.stats.api.AnimalFacts;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.milk.MilkSlices.LACTATION;

// The public API, as another mod sees it.
public final class ApiGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("an_empty_milking_result_is_refused", 20, ApiGameTests::anEmptyMilkingResultIsRefused),
            new BreedingGameTests.Case("an_empty_death_drop_is_refused", 20, ApiGameTests::anEmptyDeathDropIsRefused),
            new BreedingGameTests.Case("an_empty_shear_yield_is_refused", 20, ApiGameTests::anEmptyShearYieldIsRefused),
            new BreedingGameTests.Case("info_agrees_with_the_stat_card", 20, ApiGameTests::infoAgreesWithTheStatCard),
            new BreedingGameTests.Case("untracked_animals_have_no_info", 20, ApiGameTests::untrackedAnimalsHaveNoInfo),
            new BreedingGameTests.Case("cancelled_milking_keeps_the_bucket", 20, ApiGameTests::cancelledMilkingKeepsTheBucket),
            new BreedingGameTests.Case("a_milking_result_emptied_in_place_keeps_the_bucket", 20,
                    ApiGameTests::aMilkingResultEmptiedInPlaceKeepsTheBucket),
            new BreedingGameTests.Case("milking_result_can_be_changed", 20, ApiGameTests::milkingResultCanBeChanged),
            new BreedingGameTests.Case("death_drop_can_be_replaced", 20, ApiGameTests::deathDropCanBeReplaced),
            new BreedingGameTests.Case("a_huge_death_drop_splits_into_stacks", 20, ApiGameTests::aHugeDeathDropSplitsIntoStacks),
            new BreedingGameTests.Case("shear_yield_can_be_cancelled", 20, ApiGameTests::shearYieldCanBeCancelled),
            new BreedingGameTests.Case("shear_yield_can_be_changed", 20, ApiGameTests::shearYieldCanBeChanged),
            new BreedingGameTests.Case("a_throwing_listener_does_not_stop_milking", 20, ApiGameTests::aThrowingListenerDoesNotStopMilking),
            new BreedingGameTests.Case("disabled_species_fire_no_milking", 20, ApiGameTests::disabledSpeciesFireNoMilking),
            new BreedingGameTests.Case("cancelled_familiarity_change_keeps_familiarity", 20, ApiGameTests::cancelledFamiliarityChangeKeepsFamiliarity),
            new BreedingGameTests.Case("cancelled_taming_leaves_a_wolf_wild", 20, ApiGameTests::cancelledTamingLeavesAWolfWild),
            new BreedingGameTests.Case("the_herd_stick_leaves_an_excluded_wolf_alone", 20, ApiGameTests::theHerdStickLeavesAnExcludedWolfAlone),
            new BreedingGameTests.Case("befriended_fires_once_when_a_cow_becomes_familiar", 20, ApiGameTests::befriendedFiresOnceWhenACowBecomesFamiliar),
            new BreedingGameTests.Case("born_fires_with_the_mother_and_her_young", 200, ApiGameTests::bornFiresWithTheMotherAndHerYoung),
            new BreedingGameTests.Case("hatched_fires_with_the_parents", 20, ApiGameTests::hatchedFiresWithTheParents),
            new BreedingGameTests.Case("a_wild_egg_hatches_with_no_parents", 40, ApiGameTests::aWildEggHatchesWithNoParents),
            new BreedingGameTests.Case("the_api_fills_and_drains_a_pail", 20, ApiGameTests::theApiFillsAndDrainsAPail),
            new BreedingGameTests.Case("the_api_obeys_the_tag", 20, ApiGameTests::theApiObeysTheTag),
            new BreedingGameTests.Case("the_api_says_a_stick_is_no_pail", 20, ApiGameTests::theApiSaysAStickIsNoPail),
            new BreedingGameTests.Case("the_api_drains_a_dropped_fluid", 20, ApiGameTests::theApiDrainsADroppedFluid)
    );

    public static final Handlers<Milking> MILKINGS = new Handlers<>(BarnyardEvents.MILKING, event -> event.animal().getUUID());
    static final Handlers<DeathDrop> DEATH_DROPS = new Handlers<>(BarnyardEvents.DEATH_DROP, event -> event.animal().getUUID());
    static final Handlers<FamiliarityChange> FAMILIARITY_CHANGES = new Handlers<>(BarnyardEvents.FAMILIARITY_CHANGE, event -> event.animal().getUUID());
    static final Handlers<Taming> TAMINGS = new Handlers<>(BarnyardEvents.TAMING, event -> event.animal().getUUID());
    static final Handlers<Befriended> BEFRIENDS = new Handlers<>(BarnyardEvents.BEFRIENDED, event -> event.animal().getUUID());
    static final Handlers<Born> BIRTHS = new Handlers<>(BarnyardEvents.BORN, event -> event.mother().getUUID());
    // A wild egg's chick has no parent to key on, so every hatch is kept and a test finds its own by the chick.
    static final List<Hatched> HATCHES = new CopyOnWriteArrayList<>();

    static {
        BarnyardEvents.HATCHED.register(HATCHES::add);
    }

    static final Handlers<ShearYield> SHEAR_YIELDS = new Handlers<>(BarnyardEvents.SHEAR_YIELD, event -> event.animal().getUUID());

    private static final String GIVE_NOTHING = "use cancel() to give nothing";

    private ApiGameTests() {
    }

    // "Nothing" has one meaning, cancel(); an empty stack is refused rather than quietly meaning the same.
    public static void anEmptyMilkingResultIsRefused(GameTestHelper helper) {
        Milking milking = new Milking(null, null, new ItemStack(Items.MILK_BUCKET));
        assertRefusesEmpty(helper, () -> milking.setResult(ItemStack.EMPTY));
        helper.succeed();
    }

    public static void anEmptyDeathDropIsRefused(GameTestHelper helper) {
        DeathDrop drop = new DeathDrop(null, new ItemStack(Items.BEEF));
        assertRefusesEmpty(helper, () -> drop.setStack(ItemStack.EMPTY));
        helper.succeed();
    }

    public static void anEmptyShearYieldIsRefused(GameTestHelper helper) {
        ShearYield yield = new ShearYield(null, new ItemStack(Items.STRING));
        assertRefusesEmpty(helper, () -> yield.setStack(ItemStack.EMPTY));
        helper.succeed();
    }

    // What another mod would read about a familiar pregnant cow is what her stat card shows.
    public static void infoAgreesWithTheStatCard(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        long now = helper.getLevel().getGameTime();
        long clock = helper.getLevel().getOverworldClockTime();

        AnimalFacts facts = LiveFacts.of(mother, now, clock).orElseThrow();
        AnimalInfo info = BarnyardApi.animals().info(mother).orElseThrow(() -> GameTestCompat.failure(helper, "a tracked cow has no info"));

        helper.assertTrue(BarnyardApi.animals().tracked(mother), "a tracked cow isn't tracked");
        helper.assertTrue(info.sex() == traits(helper, mother).sex(), "info's sex is " + info.sex());
        helper.assertTrue(info.sizeClass().name().equals(facts.sizeLetter()), "info's size is " + info.sizeClass() + ", the card's " + facts.sizeLetter());
        helper.assertTrue(info.ageStage() == facts.stage(), "info's stage is " + info.ageStage() + ", the card's " + facts.stage());
        helper.assertTrue(info.familiarity() == facts.familiarity(), "info's familiarity is " + info.familiarity() + ", the card's " + facts.familiarity());
        helper.assertTrue(info.pregnancy().isPresent() == facts.pregnant(), "info's pregnancy is " + info.pregnancy());
        helper.assertTrue(info.pregnancy().map(p -> p.ticksUntilDue() == facts.dueTicks()).orElse(false),
                "info's due ticks are " + info.pregnancy() + ", the card's " + facts.dueTicks());
        helper.assertTrue(info.pregnancy().map(p -> p.fatherSpecies().equals("minecraft:cow")).orElse(false), "the father isn't a cow");
        helper.succeed();
    }

    public static void untrackedAnimalsHaveNoInfo(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        BarnyardConfig config = BarnyardConfig.current();
        boolean tracked;
        Optional<AnimalInfo> info;
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:cow\"]}"));
            tracked = BarnyardApi.animals().tracked(cow);
            info = BarnyardApi.animals().info(cow);
        } finally {
            BarnyardConfig.set(config);
        }

        helper.assertTrue(!tracked, "an excluded cow is tracked");
        helper.assertTrue(info.isEmpty(), "an excluded cow has info " + info);
        helper.succeed();
    }

    public static void cancelledMilkingKeepsTheBucket(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Lactation before = traits(helper, cow).get(LACTATION);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.add(cow.getUUID(), Milking::cancel);
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "a cancelled milking left " + player.getMainHandItem());
        helper.assertTrue(traits(helper, cow).get(LACTATION).equals(before), "a cancelled milking counted as milked");
        helper.succeed();
    }

    // setResult refuses an empty stack, but a listener can still empty the one it's given; that gives nothing too.
    public static void aMilkingResultEmptiedInPlaceKeepsTheBucket(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Lactation before = traits(helper, cow).get(LACTATION);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.add(cow.getUUID(), milking -> milking.result().setCount(0));
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "an emptied milking left " + player.getMainHandItem());
        helper.assertTrue(traits(helper, cow).get(LACTATION).equals(before), "an emptied milking counted as milked");
        helper.succeed();
    }

    public static void milkingResultCanBeChanged(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.add(cow.getUUID(), milking -> milking.setResult(new ItemStack(Items.HONEY_BOTTLE)));
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.HONEY_BOTTLE), "a changed milking gave " + player.getMainHandItem());
        helper.succeed();
    }

    // Beef becomes three bones, and everything else is cancelled, so only the bones lie in the pen.
    public static void deathDropCanBeReplaced(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        List<ItemEntity> dropped = dieWith(helper, cow, drop -> {
            if (drop.stack().is(Items.BEEF)) {
                drop.setStack(new ItemStack(Items.BONE, 3));
            } else {
                drop.cancel();
            }
        });

        helper.assertTrue(dropped.stream().allMatch(item -> item.getItem().is(Items.BONE)), "the cow dropped " + names(dropped));
        helper.assertTrue(dropped.stream().mapToInt(item -> item.getItem().getCount()).sum() == 3, "the cow dropped " + names(dropped));
        helper.succeed();
    }

    public static void aHugeDeathDropSplitsIntoStacks(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        List<ItemEntity> dropped = dieWith(helper, cow, drop -> {
            if (drop.stack().is(Items.BEEF)) {
                drop.setStack(new ItemStack(Items.BEEF, 100));
            } else {
                drop.cancel();
            }
        });

        List<Integer> counts = dropped.stream().map(item -> item.getItem().getCount()).sorted().toList();
        helper.assertTrue(counts.equals(List.of(36, 64)), "100 beef dropped as " + names(dropped));
        helper.succeed();
    }

    // The wool is cancelled, but the shearing still counts: the shears wait for tomorrow.
    public static void shearYieldCanBeCancelled(GameTestHelper helper) {
        Sheep sheep = (Sheep) female(helper, EntityTypes.SHEEP);
        SHEAR_YIELDS.add(sheep.getUUID(), ShearYield::cancel);
        try {
            GameTestCompat.useOn(sheep, FoodGameTests.holding(helper, Items.SHEARS));
        } finally {
            SHEAR_YIELDS.remove(sheep.getUUID());
        }
        List<ItemEntity> wool = itemsAround(helper, sheep.blockPosition());

        helper.assertTrue(sheep.isSheared(), "the sheep wasn't sheared");
        helper.assertTrue(wool.isEmpty(), "a cancelled yield dropped " + names(wool));
        sheep.setSheared(false);
        GameTestCompat.useOn(sheep, FoodGameTests.holding(helper, Items.SHEARS));
        helper.assertTrue(!sheep.isSheared(), "a cancelled yield didn't count as today's shearing");
        helper.succeed();
    }

    // The first wool stack becomes 100 string, past a stack's size, and any other is cancelled: 100 string and no
    // wool lie in the pen. On 1.21.1 this is the path where the mod spawns the changed stack itself.
    public static void shearYieldCanBeChanged(GameTestHelper helper) {
        Sheep sheep = (Sheep) female(helper, EntityTypes.SHEEP);
        AtomicBoolean changed = new AtomicBoolean();
        SHEAR_YIELDS.add(sheep.getUUID(), yield -> {
            if (changed.compareAndSet(false, true)) {
                yield.setStack(new ItemStack(Items.STRING, 100));
            } else {
                yield.cancel();
            }
        });
        try {
            GameTestCompat.useOn(sheep, FoodGameTests.holding(helper, Items.SHEARS));
        } finally {
            SHEAR_YIELDS.remove(sheep.getUUID());
        }
        List<ItemEntity> dropped = itemsAround(helper, sheep.blockPosition());

        helper.assertTrue(sheep.isSheared(), "the sheep wasn't sheared");
        helper.assertTrue(dropped.stream().allMatch(item -> item.getItem().is(Items.STRING)), "the shearing dropped " + names(dropped));
        int string = dropped.stream().mapToInt(item -> item.getItem().getCount()).sum();
        helper.assertTrue(string == 100, "the shearing dropped " + string + " string: " + names(dropped));
        helper.succeed();
    }

    public static void aThrowingListenerDoesNotStopMilking(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        Player player = FoodGameTests.holding(helper, Items.BUCKET);
        MILKINGS.add(cow.getUUID(), milking -> {
            throw new IllegalStateException("a broken listener, on purpose");
        });
        try {
            GameTestCompat.interact(cow, player);
        } finally {
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "a throwing listener left " + player.getMainHandItem());
        helper.succeed();
    }

    public static void disabledSpeciesFireNoMilking(GameTestHelper helper) {
        Animal cow = ProductGameTests.mother(helper, EntityTypes.COW, 0.5);
        AtomicBoolean heard = new AtomicBoolean();
        BarnyardConfig config = BarnyardConfig.current();
        MILKINGS.add(cow.getUUID(), milking -> heard.set(true));
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:cow\"]}"));
            GameTestCompat.interact(cow, FoodGameTests.holding(helper, Items.BUCKET));
        } finally {
            BarnyardConfig.set(config);
            MILKINGS.remove(cow.getUUID());
        }

        helper.assertTrue(!heard.get(), "an excluded cow fired a Milking");
        helper.succeed();
    }

    public static void cancelledFamiliarityChangeKeepsFamiliarity(GameTestHelper helper) {
        Animal cow = CareGameTests.withFamiliarity(helper, female(helper, EntityTypes.COW), 0.3);
        FAMILIARITY_CHANGES.add(cow.getUUID(), FamiliarityChange::cancel);
        try {
            feed(helper, cow, Items.WHEAT);
        } finally {
            FAMILIARITY_CHANGES.remove(cow.getUUID());
        }

        double familiarity = FeaturesRepository.get(FamiliarityApi.class).familiarity(traits(helper, cow));
        helper.assertTrue(familiarity == 0.3, "a cancelled change left familiarity at " + familiarity);
        helper.succeed();
    }

    // The meal brings the wolf to the familiar mark, which would tame it.
    public static void cancelledTamingLeavesAWolfWild(GameTestHelper helper) {
        TamableAnimal wolf = (TamableAnimal) CareGameTests.withFamiliarity(helper, female(helper, EntityTypes.WOLF), Familiarity.threshold() - 0.05);
        TAMINGS.add(wolf.getUUID(), Taming::cancel);
        try {
            feed(helper, wolf, Items.BEEF);
        } finally {
            TAMINGS.remove(wolf.getUUID());
        }

        helper.assertTrue(FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits(helper, wolf)), "the wolf didn't reach the mark, so this proves nothing");
        helper.assertTrue(!wolf.isTame(), "a cancelled taming tamed the wolf");
        helper.succeed();
    }

    // An excluded species is left alone, by the creative herd stick too: no events, no familiarity, no taming.
    public static void theHerdStickLeavesAnExcludedWolfAlone(GameTestHelper helper) {
        TamableAnimal wolf = (TamableAnimal) CareGameTests.withFamiliarity(helper, female(helper, EntityTypes.WOLF), 0.2);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        AtomicInteger heard = new AtomicInteger();
        BarnyardConfig config = BarnyardConfig.current();
        FAMILIARITY_CHANGES.add(wolf.getUUID(), change -> heard.incrementAndGet());
        TAMINGS.add(wolf.getUUID(), taming -> heard.incrementAndGet());
        try {
            BarnyardConfig.set(BarnyardConfig.parse("{\"exclude\": [\"minecraft:wolf\"]}"));
            HerdTool tool = HerdTool.familiaritySet(100);
            HerdStick.use(HerdStick.create(tool), tool, player, wolf);
        } finally {
            BarnyardConfig.set(config);
            FAMILIARITY_CHANGES.remove(wolf.getUUID());
            TAMINGS.remove(wolf.getUUID());
        }

        helper.assertTrue(heard.get() == 0, "the stick fired " + heard.get() + " events for an excluded wolf");
        double familiarity = traits(helper, wolf).get(CARE).familiarity();
        helper.assertTrue(Math.abs(familiarity - 0.2) < 1.0e-9, "the stick set an excluded wolf to " + familiarity);
        helper.assertTrue(!wolf.isTame(), "the stick tamed an excluded wolf");
        helper.succeed();
    }

    public static void befriendedFiresOnceWhenACowBecomesFamiliar(GameTestHelper helper) {
        Animal cow = CareGameTests.withFamiliarity(helper, female(helper, EntityTypes.COW), Familiarity.threshold() - 0.05);
        AtomicInteger heard = new AtomicInteger();
        BEFRIENDS.add(cow.getUUID(), befriended -> heard.incrementAndGet());
        try {
            feed(helper, cow, Items.WHEAT);
        } finally {
            BEFRIENDS.remove(cow.getUUID());
        }

        helper.assertTrue(heard.get() == 1, "Befriended fired " + heard.get() + " times");
        helper.succeed();
    }

    // One hand meal, sneaking, so an animal that isn't familiar yet takes it instead of shying away.
    public static void feed(GameTestHelper helper, Animal animal, Item food) {
        Player player = FoodGameTests.holding(helper, food);
        player.setShiftKeyDown(true);
        FamiliarityActions.interact(animal, player, InteractionHand.MAIN_HAND);
    }

    // The litter is due now, so she gives birth on her next tick.
    public static void bornFiresWithTheMotherAndHerYoung(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        mother.spawnChildFromBreeding(helper.getLevel(), father);
        Pregnancy pregnancy = BreedingGameTests.pregnancy(helper, mother);
        long now = helper.getLevel().getGameTime();
        Services.DATA.set(mother, PregnancyFeature.PREGNANCY, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        AtomicReference<Born> heard = new AtomicReference<>();
        BIRTHS.add(mother.getUUID(), heard::set);

        helper.succeedWhen(() -> {
            Born born = heard.get();
            helper.assertTrue(born != null, "no Born was heard");
            List<UUID> piglets = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).map(Animal::getUUID).sorted().toList();
            List<UUID> young = born.young().stream().map(Animal::getUUID).sorted().toList();
            helper.assertTrue(!young.isEmpty() && young.equals(piglets), "Born named " + young + ", the pen has " + piglets);
            BIRTHS.remove(mother.getUUID());
        });
    }

    public static void hatchedFiresWithTheParents(GameTestHelper helper) {
        NestingBoxBlockEntity box = EggGameTests.box(helper, EggGameTests.BOX);
        EggData data = new EggData("minecraft:chicken", UUID.randomUUID(), UUID.randomUUID(), EggGameTests.largeHen(), Growth.wild(), 1.0, 0.5,
                1.3, Growth.wild(), 1.0, "", 1_000L, 0L);
        box.setItem(0, FertilizedEggItem.create(data));

        NestingBoxBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(EggGameTests.BOX), helper.getBlockState(EggGameTests.BOX), box);

        Hatched hatched = hatchOf(helper, EggGameTests.chickIn(helper, EggGameTests.BOX.north()));
        helper.assertTrue(data.mother().equals(hatched.mother()) && data.father().equals(hatched.father()),
                "Hatched named " + hatched.mother() + " and " + hatched.father());
        helper.succeed();
    }

    public static void aWildEggHatchesWithNoParents(GameTestHelper helper) {
        NestingBoxBlockEntity box = EggGameTests.box(helper, EggGameTests.BOX);
        ItemStack wild = WildEggs.taint(FertilizedEggItem.create(EggGameTests.eggData(EggGameTests.largeHen(), 1.2, 1_000L)));
        wild.set(EggsComponents.WILD_EGG, wild.get(EggsComponents.WILD_EGG).withTicks(5L));
        box.setItem(0, wild);

        EggGameTests.onceHatched(helper, chick -> {
            Hatched hatched = hatchOf(helper, chick);
            helper.assertTrue(hatched.mother() == null && hatched.father() == null,
                    "a wild egg's Hatched named " + hatched.mother() + " and " + hatched.father());
            helper.succeed();
        });
    }

    private static Hatched hatchOf(GameTestHelper helper, Animal chick) {
        return HATCHES.stream().filter(hatched -> hatched.chick().getUUID().equals(chick.getUUID())).findFirst()
                .orElseThrow(() -> GameTestCompat.failure(helper, "no Hatched was heard for the chick"));
    }

    private static List<ItemEntity> dieWith(GameTestHelper helper, Animal animal, Consumer<DeathDrop> handler) {
        BlockPos at = animal.blockPosition();
        DEATH_DROPS.add(animal.getUUID(), handler);
        try {
            animal.die(helper.getLevel().damageSources().generic());
        } finally {
            DEATH_DROPS.remove(animal.getUUID());
        }
        return itemsAround(helper, at);
    }

    private static List<ItemEntity> itemsAround(GameTestHelper helper, BlockPos at) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(3.0));
    }

    private static List<String> names(List<ItemEntity> items) {
        return items.stream().map(item -> item.getItem().toString()).toList();
    }

    // Listeners on the public events are global and last as long as the server, so each test hears only its own
    // animals: one listener per event, registered once, hands each event to the handler a test added for that animal.
    public static final class Handlers<E> {
        private final Map<UUID, Consumer<E>> byAnimal = new ConcurrentHashMap<>();

        public Handlers(BarnyardEvent<E> event, Function<E, UUID> key) {
            event.register(fired -> {
                UUID animal = key.apply(fired);
                Consumer<E> handler = animal == null ? null : byAnimal.get(animal);
                if (handler != null) {
                    handler.accept(fired);
                }
            });
        }

        public void add(UUID animal, Consumer<E> handler) {
            byAnimal.put(animal, handler);
        }

        public void remove(UUID animal) {
            byAnimal.remove(animal);
        }
    }

    public static void theApiFillsAndDrainsAPail(GameTestHelper helper) {
        BarnyardPails pails = BarnyardApi.pails();
        ItemStack pail = new ItemStack(PailItems.IRON_PAIL);

        helper.assertTrue(pails.isPail(pail) && pails.capacity(pail) == 5000, "the iron pail reads as " + pails.capacity(pail));
        helper.assertTrue(pails.fill(pail, MilkFluids.GOAT.value(), 1000, true) == 1000 && pails.amount(pail) == 0, "a simulated fill changed the pail");
        helper.assertTrue(pails.fill(pail, MilkFluids.GOAT.value(), 1000, false) == 1000, "the fill didn't take a thousand");
        helper.assertTrue(pails.fluid(pail).equals(Optional.of(MilkFluids.GOAT.value())), "the pail holds " + pails.fluid(pail));
        helper.assertTrue(pails.drain(pail, 400, false) == 400, "the drain didn't give four hundred");
        helper.assertTrue(pails.amount(pail) == 600, "the pail holds " + pails.amount(pail));
        helper.succeed();
    }

    public static void theApiObeysTheTag(GameTestHelper helper) {
        ItemStack pail = new ItemStack(PailItems.WOODEN_PAIL);

        helper.assertTrue(BarnyardApi.pails().fill(pail, Fluids.WATER, 1000, false) == 0, "a pail took water");
        helper.assertTrue(BarnyardApi.pails().amount(pail) == 0, "the pail holds " + BarnyardApi.pails().amount(pail));
        helper.succeed();
    }

    public static void theApiSaysAStickIsNoPail(GameTestHelper helper) {
        ItemStack stick = new ItemStack(Items.STICK);

        helper.assertTrue(!BarnyardApi.pails().isPail(stick) && BarnyardApi.pails().capacity(stick) == 0, "a stick reads as a pail");
        helper.assertTrue(BarnyardApi.pails().fluid(stick).isEmpty() && BarnyardApi.pails().amount(stick) == 0, "a stick holds something");
        helper.assertTrue(BarnyardApi.pails().fill(stick, MilkFluids.GOAT.value(), 1000, false) == 0, "a stick took milk");
        helper.succeed();
    }

    // A pail of a fluid that's gone reports no fluid but its amount, and still drains.
    public static void theApiDrainsADroppedFluid(GameTestHelper helper) {
        BarnyardPails pails = BarnyardApi.pails();
        ItemStack pail = PailGameTests.pail(PailItems.IRON_PAIL, Identifier.parse("gone:whey"), 1500);

        helper.assertTrue(pails.fluid(pail).isEmpty() && pails.amount(pail) == 1500, "the dropped fluid reads as " + pails.fluid(pail) + " " + pails.amount(pail));
        helper.assertTrue(pails.fill(pail, MilkFluids.GOAT.value(), 1000, false) == 0, "a pail of gone:whey took goat milk");
        helper.assertTrue(pails.drain(pail, 1000, false) == 1000 && pails.amount(pail) == 500, "the drain gave the wrong amount, leaving " + pails.amount(pail));
        helper.succeed();
    }

    private static void assertRefusesEmpty(GameTestHelper helper, Runnable setEmpty) {
        try {
            setEmpty.run();
        } catch (IllegalArgumentException refused) {
            helper.assertTrue(GIVE_NOTHING.equals(refused.getMessage()), "refused with the wrong message: " + refused.getMessage());
            return;
        }
        helper.fail("an empty stack was accepted");
    }
}
