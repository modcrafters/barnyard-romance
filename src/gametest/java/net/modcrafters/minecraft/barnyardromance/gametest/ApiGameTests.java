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
import net.modcrafters.minecraft.barnyardromance.api.BarnyardEvent;
import net.modcrafters.minecraft.barnyardromance.api.event.DeathDrop;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
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
            new BreedingGameTests.Case("milking_result_can_be_changed", 20, ApiGameTests::milkingResultCanBeChanged),
            new BreedingGameTests.Case("death_drop_can_be_replaced", 20, ApiGameTests::deathDropCanBeReplaced),
            new BreedingGameTests.Case("a_huge_death_drop_splits_into_stacks", 20, ApiGameTests::aHugeDeathDropSplitsIntoStacks),
            new BreedingGameTests.Case("shear_yield_can_be_cancelled", 20, ApiGameTests::shearYieldCanBeCancelled),
            new BreedingGameTests.Case("a_throwing_listener_does_not_stop_milking", 20, ApiGameTests::aThrowingListenerDoesNotStopMilking),
            new BreedingGameTests.Case("disabled_species_fire_no_milking", 20, ApiGameTests::disabledSpeciesFireNoMilking)
    );

    static final Handlers<Milking> MILKINGS = new Handlers<>(BarnyardEvents.MILKING, event -> event.animal().getUUID());
    static final Handlers<DeathDrop> DEATH_DROPS = new Handlers<>(BarnyardEvents.DEATH_DROP, event -> event.animal().getUUID());
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
                Consumer<E> handler = byAnimal.get(key.apply(fired));
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
